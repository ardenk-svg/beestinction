package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      Decides, once per day, which random events happen today. There are no random events during the
 *                grace period at the start of a run or in winter. Chances come from Difficulty; targets (such as
 *                which patch is sprayed) are picked with the game's GameRandom.
 * INVARIANTS:    Uses no randomness except s.random().
 * COLLABORATORS: GameEvent and subclasses, Difficulty, GameState, Simulation (applies the events).
 */
public final class EventScheduler {
    private final BalanceConfig cfg;

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the scheduler.
     */
    public EventScheduler(BalanceConfig cfg) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    /**
     * REQUIRES: s.clock().isDayStart()
     * MODIFIES: s.random()
     * EFFECTS:  Returns today's events in a deterministic order (possibly empty, never null). Returns an empty
     *           list during the grace period (s.difficulty().inGracePeriod(day)) and in WINTER. Otherwise, for
     *           each EventType rolls s.random().chanceBasisPoints(s.difficulty().eventChanceBasisPoints(type, day));
     *           a pesticide spray targets a patch picked with s.random().
     */
    public List<GameEvent> rollDailyEvents(GameState s) {
        // TODO(human): implement per RME above.
        return new ArrayList<>();
    }
}
