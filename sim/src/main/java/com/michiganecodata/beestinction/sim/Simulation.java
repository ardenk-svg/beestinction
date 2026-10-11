package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.events.GameEvent;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.log.GameLog;
import com.michiganecodata.beestinction.sim.log.UiEventType;
import com.michiganecodata.beestinction.sim.orders.Order;
import com.michiganecodata.beestinction.sim.orders.OrderQueue;
import com.michiganecodata.beestinction.sim.stats.RunSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      One run of the game. Owns the GameState (which holds the rule systems), the order queue, the
 *                GameLog, and the listeners. tick() advances the game by one tick, calling the systems in the
 *                exact order of CLAUDE.md D.5; the UI never changes the state directly, it submits Orders.
 *                Systems post notifications to the GameState outbox; Simulation dispatches them to listeners
 *                after the systems have run.
 *                DECISION: the systems still run for the rest of the tick in which the game ends; from the next
 *                tick on, tick() does nothing, so the state no longer changes.
 * INVARIANTS:    The same GameSetup and the same orders (at the same ticks) always produce the same game.
 *                Once isGameOver() is true, tick() changes nothing and onGameOver has been sent exactly once.
 * COLLABORATORS: GameSetup, GameState, GameRules and the systems, Order, OrderQueue, GameLog, GameListener,
 *                RunSummary, TickDriver and GameScreen (core), ReplayRunner, tests.
 */
public final class Simulation {
    private final GameSetup setup;
    private final GameState state;
    private final GameRules rules;
    private final OrderQueue orderQueue = new OrderQueue();
    private final GameLog log;
    private final List<GameListener> listeners = new ArrayList<>();
    private RunSummary finalSummary;

    private Simulation(GameSetup setup) {
        this.setup = setup;
        this.state = new GameState(setup);
        this.rules = state.rules();
        this.log = new GameLog(setup);
    }

    /**
     * REQUIRES: setup != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a new run at tick 0: builds the GameState, sets up the starting colony
     *           (ColonySystem.initializeColony), and reveals the fog within cfg.startingRevealRadiusTiles() of
     *           the hive. No tick has run yet.
     */
    public static Simulation create(GameSetup setup) {
        Objects.requireNonNull(setup, "setup");
        Simulation sim = new Simulation(setup);
        sim.rules.colony().initializeColony(sim.state);
        sim.state.fog().revealAround(sim.state.map().hivePos(), setup.balance().startingRevealRadiusTiles());
        return sim;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this, the GameState, listeners (through callbacks)
     * EFFECTS:  Runs one tick (CLAUDE.md D.5). Does nothing if the game is over:
     *             at a day start: season start (winter.onSeasonStart, onSeasonChanged), colony, foraging, winter,
     *               and score day starts, the day's random events, stats.recordDay, onDayStart;
     *             the orders for this tick: validate, apply if valid, record in the log;
     *             colony, foraging, winter, and score ticks;
     *             notifications dispatched in order (then the outbox is cleared);
     *             on game over: the summary is built and onGameOver sent;
     *             the clock advances.
     */
    public void tick() {
        if (state.isGameOver()) {
            return;
        }
        GameClock clock = state.clock();
        long t = clock.tick();

        if (clock.isDayStart()) {
            if (clock.isSeasonStart()) {
                rules.winter().onSeasonStart(state, clock.season());
                notifySeasonChanged(clock.season());
            }
            rules.colony().onDayStart(state);      // aging begins, daily rations, egg target, queen starvation
            rules.foraging().onDayStart(state);    // reset patch capacities
            rules.winter().onDayStart(state);      // winter day counting, day-4 die-off
            state.scoreKeeper().onDayStart(state); // today's point total
            List<GameEvent> events = rules.scheduler().rollDailyEvents(state);
            if (events != null) {
                for (GameEvent e : events) {
                    e.apply(state);
                }
            }
            state.stats().recordDay(state);
            notifyDayStart(clock.dayOfRun());
        }

        for (Order order : orderQueue.drainForTick(t)) {
            boolean accepted = order.validate(state);
            if (accepted) {
                order.apply(state);
            }
            log.recordOrder(order, accepted);
        }

        rules.colony().onTick(state);   // death slice, feeding (queen first), laying, brood, emergence, comb
        // DECISION: Simulation (not ForagingSystem) records each group's previous position for interpolation, so
        // the renderer stays correct whatever the movement rules do. This is plumbing, not a system call.
        snapshotGroupPositions();
        rules.foraging().onTick(state); // movement, foraging, pesticide, deposits, fog, discoveries
        rules.winter().onTick(state);   // freezing outside
        state.scoreKeeper().onTick(state);

        dispatchNotificationsAndGameOver();
        clock.advance();
    }

    /**
     * REQUIRES: maxTicks >= 0
     * MODIFIES: this, the GameState
     * EFFECTS:  Runs up to maxTicks ticks, stopping early if the game ends. Returns the number of ticks run.
     *           Running n ticks this way is identical to calling tick() n times.
     */
    public int runTicks(int maxTicks) {
        int ran = 0;
        while (ran < maxTicks && !state.isGameOver()) {
            tick();
            ran++;
        }
        return ran;
    }

    /**
     * REQUIRES: order != null and not submitted before
     * MODIFIES: this, order
     * EFFECTS:  Stamps the order with the current tick and queues it; it is validated and applied when that tick
     *           runs. Orders submitted after the game is over are ignored.
     */
    public void submit(Order order) {
        Objects.requireNonNull(order, "order");
        if (state.isGameOver()) {
            return;
        }
        order.stampTick(currentTick());
        orderQueue.enqueue(order);
    }

    /**
     * REQUIRES: type != null
     * MODIFIES: the GameLog
     * EFFECTS:  Logs a UI input that does not change the simulation (speed changes, pauses, skips, dismissed
     *           popups), stamped with the current tick.
     */
    public void recordUiEvent(UiEventType type) {
        log.recordUiEvent(currentTick(), Objects.requireNonNull(type, "type"));
    }

    /**
     * REQUIRES: listener != null
     * MODIFIES: this
     * EFFECTS:  Registers a listener. Listeners are called in registration order.
     */
    public void addListener(GameListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Unregisters a listener (no effect if it was not registered).
     */
    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this, the GameState, listeners
     * EFFECTS:  Ends the run immediately with no cause (the player or a debug key ended it): posts and dispatches
     *           the GAME_OVER notification, builds the summary, and sends onGameOver. Does nothing if the game is
     *           already over.
     *           DECISION: an early end has a null cause instead of a new DeathCause value, so the spec's enum is
     *           unchanged; the summary shows it as "Ended early".
     */
    public void endRunEarly() {
        if (state.isGameOver()) {
            return;
        }
        state.endGame(null);
        dispatchNotificationsAndGameOver();
    }

    /** Read-only view of the game state. */
    public GameStateView view() {
        return state;
    }

    public GameSetup setup() {
        return setup;
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    /** The tick that runs next (equivalently, the number of ticks run so far). */
    public long currentTick() {
        return state.clock().tick();
    }

    public GameLog log() {
        return log;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the final summary if the game is over, otherwise a summary of the run so far.
     */
    public RunSummary summary() {
        if (finalSummary != null) {
            return finalSummary;
        }
        return state.stats().buildSummary(state);
    }

    private void snapshotGroupPositions() {
        for (ForagerGroup g : state.groups()) {
            g.snapshotPosition();
        }
    }

    private void dispatchNotificationsAndGameOver() {
        List<Notification> posted = state.drainOutbox();
        if (!posted.isEmpty()) {
            List<GameListener> targets = new ArrayList<>(listeners);
            for (Notification n : posted) {
                for (GameListener l : targets) {
                    l.onNotification(n);
                }
            }
        }
        if (state.isGameOver() && finalSummary == null) {
            finalSummary = state.stats().buildSummary(state);
            log.setSummary(finalSummary);
            for (GameListener l : new ArrayList<>(listeners)) {
                l.onGameOver(finalSummary);
            }
        }
    }

    private void notifySeasonChanged(Season season) {
        for (GameListener l : new ArrayList<>(listeners)) {
            l.onSeasonChanged(season);
        }
    }

    private void notifyDayStart(int day) {
        for (GameListener l : new ArrayList<>(listeners)) {
            l.onDayStart(day);
        }
    }
}
