package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;

/**
 * OVERVIEW:      The hive's comb. Its size caps both the population and nectar storage. The player can order
 *                an expansion: the nectar cost is paid up front (by BuildCombOrder) and house bees then build
 *                for a while; when enough bee-ticks of work are done, the size grows by one.
 *                DECISION: Part I assumption 2. Work is measured in bee-ticks: `bees` builders working for
 *                `ticks` ticks contribute bees * ticks. The expansion completes when the work reaches
 *                combExpansionBeeTicks. Only one expansion runs at a time.
 * INVARIANTS:    size >= 1; beesAssigned >= 0; 0 <= beeTicksDone < beeTicksRequired while expanding;
 *                isExpanding() == (beesAssigned > 0).
 * COLLABORATORS: Hive, BalanceConfig, ColonySystem (calls progress), BuildCombOrder (starts expansions).
 */
public final class Comb {
    private int size;
    private final int beeTicksRequired;
    private int beesAssigned;
    private long beeTicksDone;

    /**
     * REQUIRES: startSize >= 1, beeTicksRequired > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates a comb of the given size that is not expanding. beeTicksRequired is the work needed
     *           for one expansion (BalanceConfig.combExpansionBeeTicks()).
     */
    public Comb(int startSize, int beeTicksRequired) {
        this.size = startSize;
        this.beeTicksRequired = beeTicksRequired;
        this.beesAssigned = 0;
        this.beeTicksDone = 0;
    }

    /** Current comb size. */
    public int size() {
        return size;
    }

    /** Bees building the current expansion (0 if not expanding). */
    public int beesAssigned() {
        return beesAssigned;
    }

    /** Work done on the current expansion, in bee-ticks. */
    public long beeTicksDone() {
        return beeTicksDone;
    }

    /** Work needed for one expansion, in bee-ticks. */
    public int beeTicksRequired() {
        return beeTicksRequired;
    }

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the maximum population: size * cfg.combPopulationPerSize().
     */
    public int populationCapacity(BalanceConfig cfg) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the nectar storage capacity: size * cfg.combNectarPerSize().
     */
    public int nectarCapacity(BalanceConfig cfg) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: bees > 0, !isExpanding()
     * MODIFIES: this
     * EFFECTS:  Starts an expansion built by `bees` house bees, with no work done yet.
     *           (The nectar cost is paid by the caller, BuildCombOrder.apply.)
     */
    public void startExpansion(int bees) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: ticks >= 0
     * MODIFIES: this
     * EFFECTS:  If expanding, adds ticks * beesAssigned bee-ticks of work. When the work reaches
     *           beeTicksRequired, the size grows by 1 and the expansion ends (beesAssigned and work reset to 0).
     *           Does nothing if not expanding.
     */
    public void progress(int ticks) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns true while an expansion is in progress.
     */
    public boolean isExpanding() {
        // TODO(human): implement per RME above.
        return false;
    }
}
