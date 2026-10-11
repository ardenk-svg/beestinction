package com.michiganecodata.beestinction.sim.util;

/**
 * OVERVIEW:      Spreads a daily total (deaths, consumption, eggs, points) evenly across the ticks of a day using
 *                integer math (CLAUDE.md C.4), so on-screen numbers change every tick without losing remainders.
 * INVARIANTS:    Stateless. For any total >= 0 the slices for t = 0..ticksPerDay-1 are each >= 0, differ from
 *                each other by at most 1, and sum exactly to the total.
 * COLLABORATORS: AgeHistogram, ColonySystem, ScoreKeeper.
 */
public final class IntSmoothing {
    private IntSmoothing() {
    }

    /**
     * REQUIRES: dailyTotal >= 0, ticksPerDay > 0, 0 <= tickOfDay < ticksPerDay
     * MODIFIES: nothing
     * EFFECTS:  Returns the part of dailyTotal that belongs to tick `tickOfDay`:
     *           (dailyTotal * (t + 1)) / ticksPerDay - (dailyTotal * t) / ticksPerDay.
     *           Throws IllegalArgumentException if the arguments break the REQUIRES clause.
     */
    public static long sliceForTick(long dailyTotal, int tickOfDay, int ticksPerDay) {
        if (dailyTotal < 0 || ticksPerDay <= 0 || tickOfDay < 0 || tickOfDay >= ticksPerDay) {
            throw new IllegalArgumentException("bad slice arguments: total=" + dailyTotal
                    + " tick=" + tickOfDay + " ticksPerDay=" + ticksPerDay);
        }
        long t = tickOfDay;
        return (dailyTotal * (t + 1)) / ticksPerDay - (dailyTotal * t) / ticksPerDay;
    }
}
