package com.michiganecodata.beestinction.sim.score;

import com.michiganecodata.beestinction.sim.GameState;

/**
 * OVERVIEW:      The score. Points per day = population x difficulty multiplier (in percent, so divide by 100).
 *                The day's total is computed at the start of the day and added smoothly, a slice per tick
 *                (IntSmoothing), so the score on screen rises every tick.
 * INVARIANTS:    points >= 0 and never decreases.
 * COLLABORATORS: GameState, Difficulty.scoreMultiplierPercent, IntSmoothing, StatsRecorder (summary).
 */
public final class ScoreKeeper {
    private long points;
    // TODO(human): add a field for today's point total.

    /** Returns the points scored so far. */
    public long points() {
        return points;
    }

    /**
     * REQUIRES: s.clock().isDayStart()
     * MODIFIES: this
     * EFFECTS:  Computes today's point total: s.population() * s.difficulty().scoreMultiplierPercent(day) / 100
     *           (floor), to be added over the day's ticks by onTick.
     */
    public void onDayStart(GameState s) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: onDayStart(s) ran for the current day
     * MODIFIES: this
     * EFFECTS:  Adds this tick's slice of today's point total:
     *           IntSmoothing.sliceForTick(todayTotal, tickOfDay, ticksPerDay). Over a full day the slices add
     *           up exactly to the day's total.
     */
    public void onTick(GameState s) {
        // TODO(human): implement per RME above.
    }
}
