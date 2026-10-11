package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;

/**
 * OVERVIEW:      The simulation's only notion of time: a tick counter, plus the calendar derived from it
 *                (day, week, season, year). 35 ticks = 1 day, 7 days = 1 week, 28 days = 1 season,
 *                4 seasons = 1 year (with the default BalanceConfig). The run starts at tick 0, which is the
 *                first tick of day 0 of SPRING in year 0.
 * INVARIANTS:    tick >= 0; ticksPerDay > 0; daysPerSeason > 0. All derived values are 0-based.
 *                Only Simulation advances the clock during a game.
 * COLLABORATORS: BalanceConfig (ticks per day, days per season), Season, Simulation, WinterSystem.
 */
public final class GameClock {
    /** Days in a week. Fixed by design (CLAUDE.md B.3). */
    public static final int DAYS_PER_WEEK = 7;

    private final int ticksPerDay;
    private final int daysPerSeason;
    private long tick;

    /**
     * REQUIRES: ticksPerDay > 0, daysPerSeason > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates a clock at tick 0. Throws IllegalArgumentException on bad arguments.
     */
    public GameClock(int ticksPerDay, int daysPerSeason) {
        if (ticksPerDay <= 0 || daysPerSeason <= 0) {
            throw new IllegalArgumentException("ticksPerDay and daysPerSeason must be positive");
        }
        this.ticksPerDay = ticksPerDay;
        this.daysPerSeason = daysPerSeason;
    }

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a clock at tick 0 using cfg's ticksPerDay and daysPerSeason.
     */
    public static GameClock fromConfig(BalanceConfig cfg) {
        return new GameClock(cfg.ticksPerDay(), cfg.daysPerSeason());
    }

    /** Returns the current tick (the tick that is running, or will run next). */
    public long tick() {
        return tick;
    }

    /**
     * REQUIRES: only Simulation.tick() (and tests that set up a GameState directly) may call this
     * MODIFIES: this
     * EFFECTS:  Moves to the next tick.
     */
    public void advance() {
        tick++;
    }

    /** Returns the 0-based day of the run (0 during the first 35 ticks). */
    public int dayOfRun() {
        return (int) (tick / ticksPerDay);
    }

    /** Returns the 0-based tick within the current day, in [0, ticksPerDay). */
    public int tickOfDay() {
        return (int) (tick % ticksPerDay);
    }

    /** Returns true if the current tick is the first tick of a day. */
    public boolean isDayStart() {
        return tickOfDay() == 0;
    }

    /** Returns the 0-based week of the run. */
    public int weekOfRun() {
        return dayOfRun() / DAYS_PER_WEEK;
    }

    /** Returns the current season. The run starts in SPRING; seasons then cycle SUMMER, FALL, WINTER. */
    public Season season() {
        int seasonIndex = (dayOfRun() / daysPerSeason) % Season.values().length;
        return Season.values()[seasonIndex];
    }

    /** Returns the 0-based day within the current season, in [0, daysPerSeason). */
    public int dayOfSeason() {
        return dayOfRun() % daysPerSeason;
    }

    /** Returns the 0-based year of the run. */
    public int year() {
        return dayOfRun() / (daysPerSeason * Season.values().length);
    }

    /** Returns true if the current tick is the first tick of a season (including tick 0). */
    public boolean isSeasonStart() {
        return isDayStart() && dayOfSeason() == 0;
    }

    public int ticksPerDay() {
        return ticksPerDay;
    }

    public int daysPerSeason() {
        return daysPerSeason;
    }

    /** Returns the number of ticks in one season. */
    public long ticksPerSeason() {
        return (long) ticksPerDay * daysPerSeason;
    }

    /** Returns the number of ticks in one year. */
    public long ticksPerYear() {
        return ticksPerSeason() * Season.values().length;
    }

    @Override
    public String toString() {
        return "GameClock[tick=" + tick + ", year=" + year() + ", " + season() + " day " + dayOfSeason() + "]";
    }
}
