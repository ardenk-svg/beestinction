package com.michiganecodata.beestinction.sim.colony;
import java.util.Arrays;
/**
 * OVERVIEW:      Worker bees stored as counts by age in days (never as individual objects). Used for the bees
 *                in the hive and for each forager group. Ages run from 0 to lifespanDays-1. At the start of each
 *                day everyone ages by one day; bees that reach lifespanDays move to a "dying today" pool and are
 *                removed gradually over the day's ticks (smoothed old-age deaths, see IntSmoothing).
 *                DECISION: bees in the dying pool are still alive (they count in total()) but belong to no age
 *                cohort, so they are never counted as available foragers and never sent out.
 * INVARIANTS:    counts.length == lifespanDays > 0; no count is negative; dying >= 0;
 *                total() == sum of counts + dying.
 * COLLABORATORS: Hive, ForagerGroup, ColonySystem, ForagingSystem, IntSmoothing.
 */
public final class AgeHistogram implements AgeHistogramView {
    private final int[] counts;
    /** Bees that reached their lifespan at the start of today and have not died yet. */
    private int dying;
    /** The size of the dying pool right after beginDay(), used to smooth deaths over the day. */
    private int dyingAtDayStart;
    // TODO(human): add fields if your implementation needs them.

    /**
     * REQUIRES: lifespanDays > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates an empty histogram with lifespanDays age slots.
     */
    public AgeHistogram(int lifespanDays) {
        if (lifespanDays <= 0) {
            throw new IllegalArgumentException("lifespanDays must be positive: " + lifespanDays);
        }
        this.counts = new int[lifespanDays];
    }

    @Override
    public int lifespanDays() {
        return counts.length;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of living bees: the sum of every age cohort plus the dying pool.
     */
    @Override
    // complete
    public int total() {
        return Arrays.stream(counts).sum() + dying;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of bees exactly `age` days old, or 0 if age is outside 0..lifespanDays-1.
     *           The dying pool is not included.
     */
    @Override
    // complete
    public int count(int age) {

        // safety net
        if (counts.length - 1 < age || age < 0){
            return 0;
        }
        // returns number of bees of that age
        return counts[age];
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of bees in the dying pool (they die over the course of today).
     */
    @Override
    public int dying() {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n bees to the age-0 cohort.
     */
    public void addNewborns(int n) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: 0 <= age < lifespanDays, n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n bees to the cohort aged `age` days. (Used to set up the starting colony.)
     */
    public void addAtAge(int age, int n) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Starts a new day. Every cohort ages by one day: the bees aged lifespanDays-1 move into the
     *           dying pool, every other cohort moves to the next age, and the age-0 cohort becomes empty.
     *           Bees still in the dying pool from yesterday stay in it. Records the pool size for
     *           removeDyingSlice and returns the number of bees in the dying pool.
     */
    public int beginDay() {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: 0 <= tickOfDay < ticksPerDay
     * MODIFIES: this
     * EFFECTS:  Removes this tick's share of today's old-age deaths from the dying pool and returns how many
     *           bees died. The share is IntSmoothing.sliceForTick(poolSizeAtDayStart, tickOfDay, ticksPerDay),
     *           but never more than the bees still in the pool (other causes may have removed some).
     *           Calling it for every tick 0..ticksPerDay-1 of a day removes exactly the pool's size at the
     *           start of the day (if nothing else touched the pool).
     */
    public int removeDyingSlice(int tickOfDay, int ticksPerDay) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of bees aged `age` days or older (ages up to lifespanDays-1). The dying
     *           pool is not included. age <= 0 counts every cohort; age >= lifespanDays returns 0.
     */
    @Override
    public int countAtLeastAge(int age) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of bees younger than `age` days. age <= 0 returns 0; age >= lifespanDays
     *           counts every cohort. The dying pool is not included.
     */
    @Override
    public int countBelowAge(int age) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: n >= 0, 0 <= minAge
     * MODIFIES: this
     * EFFECTS:  Removes the n oldest bees whose age is at least minAge (taking whole cohorts from the oldest
     *           age downward) and returns them as a new AgeHistogram with the same lifespan and the same
     *           ages. If fewer than n such bees exist, takes all of them. The dying pool is never taken.
     */
    public AgeHistogram takeOldest(int n, int minAge) {
        // TODO(human): implement per RME above.
        return new AgeHistogram(counts.length);
    }

    /**
     * REQUIRES: other != null, other.lifespanDays() == lifespanDays()
     * MODIFIES: this
     * EFFECTS:  Adds every bee of other to this histogram at the same age; other's dying pool is added to
     *           this dying pool. other is not changed. Afterwards total() has grown by other.total().
     */
    public void merge(AgeHistogram other) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: 0 <= pct <= 100
     * MODIFIES: this
     * EFFECTS:  Kills exactly floor(total() * pct / 100) bees, spread across the cohorts (and the dying pool)
     *           in proportion to their sizes: each loses floor(count * pct / 100), then any remaining kills
     *           are taken one bee at a time starting from the oldest non-empty cohort. Uses no randomness.
     *           No count becomes negative. Returns the number of bees killed.
     */
    public int killPercent(int pct) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Removes every bee (all cohorts and the dying pool) and returns how many there were.
     */
    public int killAll() {
        // TODO(human): implement per RME above.
        return 0;
    }

    @Override
    public String toString() {
        return "AgeHistogram[total=" + total() + ", dying=" + dying() + "]";
    }
}
