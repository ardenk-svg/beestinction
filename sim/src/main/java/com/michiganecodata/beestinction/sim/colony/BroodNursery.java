package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OVERVIEW:      All developing brood, as batches in the order they were laid. Each tick every batch gains
 *                development points: a base amount plus a bonus that grows with the number of nurse bees.
 *                More nurses means faster reproduction.
 * INVARIANTS:    batches are ordered by layDay (oldest first); every batch has count >= 0.
 * COLLABORATORS: BroodBatch, Hive, ColonySystem, BalanceConfig.
 */
public final class BroodNursery {
    private final List<BroodBatch> batches = new ArrayList<>();

    /** Returns an unmodifiable view of the batches, oldest first. */
    public List<BroodBatch> batches() {
        return Collections.unmodifiableList(batches);
    }

    /**
     * REQUIRES: count >= 0, day >= 0, day >= the layDay of every existing batch
     * MODIFIES: this
     * EFFECTS:  Lays `count` eggs on `day`. If the newest batch was laid on the same day, the eggs join it;
     *           otherwise a new batch with 0 points is added. count == 0 changes nothing.
     *           DECISION: one batch per day (eggs laid in later ticks of a day join that day's batch).
     */
    public void layEggs(int count, int day) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: nurseCount >= 0, cfg != null
     * MODIFIES: this (every batch)
     * EFFECTS:  Adds cfg.broodBasePointsPerTick() + nurseCount * cfg.broodNurseBonusPer100Nurses() / 100 (floor)
     *           development points to every batch. With zero nurses brood still develops at the base rate.
     */
    public void develop(int nurseCount, BalanceConfig cfg) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: threshold > 0
     * MODIFIES: this
     * EFFECTS:  Removes every batch that isReady(threshold) and returns the total number of bees in them.
     *           Batches that are not ready stay, in order.
     */
    public int collectEmerged(int threshold) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of eggs in all batches.
     */
    public int total() {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Removes every batch and returns the number of eggs that were in them.
     */
    public int killAll() {
        // TODO(human): implement per RME above.
        return 0;
    }
}
