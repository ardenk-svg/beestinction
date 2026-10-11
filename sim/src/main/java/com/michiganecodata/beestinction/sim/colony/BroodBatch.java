package com.michiganecodata.beestinction.sim.colony;

/**
 * OVERVIEW:      A batch of eggs laid on the same day. It collects development points every tick; when it has
 *                at least the threshold, its bees emerge as age-0 workers.
 * INVARIANTS:    count >= 0; devPoints >= 0; layDay >= 0.
 * COLLABORATORS: BroodNursery.
 */
public final class BroodBatch {
    private int count;
    private int devPoints;
    private final int layDay;

    /**
     * REQUIRES: count >= 0, layDay >= 0
     * MODIFIES: nothing
     * EFFECTS:  Creates a batch of `count` eggs laid on day `layDay`, with 0 development points.
     */
    public BroodBatch(int count, int layDay) {
        this.count = count;
        this.devPoints = 0;
        this.layDay = layDay;
    }

    /** Number of eggs (future bees) in this batch. */
    public int count() {
        return count;
    }

    /** Development points collected so far. */
    public int devPoints() {
        return devPoints;
    }

    /** The day of the run this batch was laid. */
    public int layDay() {
        return layDay;
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n more eggs to this batch (eggs laid later on the same day join the day's batch).
     */
    public void addEggs(int n) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: p >= 0
     * MODIFIES: this
     * EFFECTS:  Adds p development points.
     */
    public void addPoints(int p) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: threshold > 0
     * MODIFIES: nothing
     * EFFECTS:  Returns true if devPoints >= threshold.
     */
    public boolean isReady(int threshold) {
        // TODO(human): implement per RME above.
        return false;
    }
}
