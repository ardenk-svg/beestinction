package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.colony.AgeHistogram;
import com.michiganecodata.beestinction.sim.colony.AgeHistogramView;
import com.michiganecodata.beestinction.sim.colony.BroodNursery;
import com.michiganecodata.beestinction.sim.colony.Comb;
import com.michiganecodata.beestinction.sim.colony.Hive;
import com.michiganecodata.beestinction.sim.colony.NectarStore;
import com.michiganecodata.beestinction.sim.colony.Queen;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.difficulty.Difficulty;
import com.michiganecodata.beestinction.sim.difficulty.ScalingDifficulty;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.score.ScoreKeeper;
import com.michiganecodata.beestinction.sim.stats.StatsRecorder;
import com.michiganecodata.beestinction.sim.util.GameRandom;
import com.michiganecodata.beestinction.sim.world.DiscoveryLog;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.FogOfWar;
import com.michiganecodata.beestinction.sim.world.GameMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * OVERVIEW:      All the state of one run: the clock, hive, map (the run's own copy), fog, discoveries, groups
 *                away, difficulty, random generator, score, statistics, the notification outbox, and the
 *                game-over flag and cause. It also holds the run's GameRules. The rules read and change it;
 *                the UI sees it only through GameStateView.
 *                A freshly constructed GameState has an empty hive: Simulation.create then calls
 *                ColonySystem.initializeColony and reveals the starting area.
 * INVARIANTS:    Every component is non-null and belongs to this run only. Group ids are unique and increasing.
 *                Once gameOver is true it never becomes false, and gameOverCause never changes.
 * COLLABORATORS: Simulation, GameRules and the systems, Hive, GameMap, FogOfWar, DiscoveryLog, ForagerGroup,
 *                Difficulty, GameRandom, ScoreKeeper, StatsRecorder, Notification.
 */
public final class GameState implements GameStateView {
    private final GameSetup setup;
    private final BalanceConfig cfg;
    private final GameRules rules;
    private final GameClock clock;
    private final Hive hive;
    private final GameMap map;
    private final FogOfWar fog;
    private final DiscoveryLog discoveries = new DiscoveryLog();
    private final List<ForagerGroup> groups = new ArrayList<>();
    private final List<ForagerGroup> groupsView = Collections.unmodifiableList(groups);
    private final Difficulty difficulty;
    private final GameRandom random;
    private final ScoreKeeper scoreKeeper = new ScoreKeeper();
    private final StatsRecorder stats = new StatsRecorder();
    private final List<Notification> outbox = new ArrayList<>();
    private boolean gameOver;
    private DeathCause gameOverCause;
    private int nextGroupId = 1;

    /**
     * REQUIRES: setup != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the state for a new run at tick 0: an empty hive (queen, no workers, comb of the starting
     *           size), a fresh copy of the map, full fog, the difficulty for the chosen level, and a GameRandom
     *           seeded with setup.seed().
     */
    public GameState(GameSetup setup) {
        this.setup = Objects.requireNonNull(setup, "setup");
        this.cfg = setup.balance();
        this.rules = new GameRules(cfg);
        this.clock = GameClock.fromConfig(cfg);
        this.hive = new Hive(new Queen(), new AgeHistogram(cfg.lifespanDays()), new BroodNursery(),
                new NectarStore(), new Comb(cfg.combStartSize(), cfg.combExpansionBeeTicks()));
        this.map = setup.map().freshCopy();
        this.fog = new FogOfWar(map.width(), map.height());
        this.difficulty = ScalingDifficulty.forLevel(setup.difficulty(), cfg);
        this.random = new GameRandom(setup.seed());
    }

    // ===================== Mutable access for the rules =====================

    public GameSetup setup() {
        return setup;
    }

    public GameRules rules() {
        return rules;
    }

    public GameClock clock() {
        return clock;
    }

    public Hive hive() {
        return hive;
    }

    public FogOfWar fog() {
        return fog;
    }

    public DiscoveryLog discoveries() {
        return discoveries;
    }

    /** The live, modifiable list of groups away. For the rules only; the UI uses groups(). */
    public List<ForagerGroup> mutableGroups() {
        return groups;
    }

    /**
     * REQUIRES: g != null, g.id() is not used by another group in this state
     * MODIFIES: this
     * EFFECTS:  Adds g to the end of the groups list.
     */
    public void addGroup(ForagerGroup g) {
        Objects.requireNonNull(g, "group");
        if (findGroup(g.id()) != null) {
            throw new IllegalArgumentException("duplicate group id " + g.id());
        }
        groups.add(g);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Removes g from the groups list. Returns true if it was there.
     */
    public boolean removeGroup(ForagerGroup g) {
        return groups.remove(g);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Returns a new group id (1, 2, 3, ... never reused within the run).
     */
    public int nextGroupId() {
        return nextGroupId++;
    }

    /** The game's random generator. All randomness in the rules goes through it. */
    public GameRandom random() {
        return random;
    }

    public ScoreKeeper scoreKeeper() {
        return scoreKeeper;
    }

    public StatsRecorder stats() {
        return stats;
    }

    /**
     * REQUIRES: n != null
     * MODIFIES: this
     * EFFECTS:  Adds n to the outbox. Simulation dispatches the outbox to listeners at the end of the tick.
     */
    public void postNotification(Notification n) {
        outbox.add(Objects.requireNonNull(n, "notification"));
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Returns the posted notifications in order and empties the outbox. (Used by Simulation.)
     */
    public List<Notification> drainOutbox() {
        if (outbox.isEmpty()) {
            return Collections.emptyList();
        }
        List<Notification> posted = new ArrayList<>(outbox);
        outbox.clear();
        return posted;
    }

    /** Notifications posted this tick that have not been dispatched yet (read-only). */
    public List<Notification> pendingNotifications() {
        return Collections.unmodifiableList(outbox);
    }

    /**
     * REQUIRES: nothing (cause may be null when the player ends the run early)
     * MODIFIES: this
     * EFFECTS:  Ends the game with the given cause and posts a GAME_OVER notification. Does nothing if the game
     *           is already over (the first cause wins).
     */
    public void endGame(DeathCause cause) {
        if (gameOver) {
            return;
        }
        gameOver = true;
        gameOverCause = cause;
        postNotification(Notification.popup(NotificationType.GAME_OVER, "Game over", gameOverMessage(cause)));
    }

    private String gameOverMessage(DeathCause cause) {
        if (cause == null) {
            return "The run was ended early.";
        }
        if (cause == DeathCause.QUEEN_STARVED) {
            return "The queen went " + cfg.queenStarvationDays()
                    + " days in a row without her full ration. The colony has collapsed.";
        }
        return "The colony has collapsed (" + cause.name() + ").";
    }

    // ===================== GameStateView =====================

    @Override
    public BalanceConfig balance() {
        return cfg;
    }

    @Override
    public DifficultyLevel difficultyLevel() {
        return setup.difficulty();
    }

    @Override
    public Difficulty difficulty() {
        return difficulty;
    }

    @Override
    public GameMap map() {
        return map;
    }

    @Override
    public long tick() {
        return clock.tick();
    }

    @Override
    public int dayOfRun() {
        return clock.dayOfRun();
    }

    @Override
    public int tickOfDay() {
        return clock.tickOfDay();
    }

    @Override
    public int weekOfRun() {
        return clock.weekOfRun();
    }

    @Override
    public Season season() {
        return clock.season();
    }

    @Override
    public int dayOfSeason() {
        return clock.dayOfSeason();
    }

    @Override
    public int year() {
        return clock.year();
    }

    @Override
    public int nectar() {
        return hive.nectar().amount();
    }

    @Override
    public int nectarCapacity() {
        return hive.nectar().capacity();
    }

    @Override
    public int population() {
        return hive.population(groupsView);
    }

    @Override
    public int populationCapacity() {
        return hive.comb().populationCapacity(cfg);
    }

    @Override
    public int workersInHive() {
        return hive.workersInHive();
    }

    @Override
    public int houseBees() {
        return hive.houseBees(cfg.forageAgeDays());
    }

    @Override
    public int availableForagers() {
        return hive.availableForagers(cfg.forageAgeDays());
    }

    @Override
    public int beesOut() {
        int sum = 0;
        for (ForagerGroup g : groups) {
            sum += g.bees().total();
        }
        return sum;
    }

    @Override
    public int broodCount() {
        return hive.brood().total();
    }

    @Override
    public AgeHistogramView hiveWorkers() {
        return hive.workers();
    }

    @Override
    public int feedingLevel() {
        return hive.queen().feedingLevel();
    }

    @Override
    public int combSize() {
        return hive.comb().size();
    }

    @Override
    public boolean isCombExpanding() {
        return hive.comb().isExpanding();
    }

    @Override
    public List<ForagerGroup> groups() {
        return groupsView;
    }

    @Override
    public ForagerGroup findGroup(int groupId) {
        for (ForagerGroup g : groups) {
            if (g.id() == groupId) {
                return g;
            }
        }
        return null;
    }

    @Override
    public boolean isRevealed(int x, int y) {
        return fog.isRevealed(x, y);
    }

    @Override
    public boolean isPatchDiscovered(FlowerPatch p) {
        return fog.isPatchDiscovered(p);
    }

    @Override
    public Set<String> discoveredSpeciesIds() {
        return discoveries.discoveredIds();
    }

    @Override
    public long score() {
        return scoreKeeper.points();
    }

    @Override
    public int totalBirths() {
        return stats.totalBirths();
    }

    @Override
    public int totalDeaths() {
        return stats.totalDeaths();
    }

    @Override
    public int deaths(DeathCause cause) {
        return stats.deaths(cause);
    }

    @Override
    public boolean isConversionWindowOpen() {
        return rules.winter().isConversionWindowOpen(clock);
    }

    @Override
    public boolean isSkipAvailable() {
        return rules.winter().isSkipAvailable(this);
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public DeathCause gameOverCause() {
        return gameOverCause;
    }
}
