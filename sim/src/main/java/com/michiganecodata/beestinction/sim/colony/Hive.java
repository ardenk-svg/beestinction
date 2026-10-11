package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;

import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      The colony at home: the queen, the workers in the hive (by age), the brood, the nectar store,
 *                and the comb. Workers younger than the forage age are house bees (nursing, building); workers at
 *                or above it are foragers available for orders. Workers away from the hive live in ForagerGroups.
 *                DECISION: "population" means adult workers (in the hive plus away). The queen and brood are not
 *                counted toward population or population capacity.
 * INVARIANTS:    queen, workers, brood, nectar, comb are non-null and never replaced.
 * COLLABORATORS: Queen, AgeHistogram, BroodNursery, NectarStore, Comb, ForagerGroup, ColonySystem.
 */
public final class Hive {
    private final Queen queen;
    private final AgeHistogram workers;
    private final BroodNursery brood;
    private final NectarStore nectar;
    private final Comb comb;

    /**
     * REQUIRES: all arguments non-null
     * MODIFIES: nothing
     * EFFECTS:  Creates a hive from its parts.
     */
    public Hive(Queen queen, AgeHistogram workers, BroodNursery brood, NectarStore nectar, Comb comb) {
        this.queen = Objects.requireNonNull(queen, "queen");
        this.workers = Objects.requireNonNull(workers, "workers");
        this.brood = Objects.requireNonNull(brood, "brood");
        this.nectar = Objects.requireNonNull(nectar, "nectar");
        this.comb = Objects.requireNonNull(comb, "comb");
    }

    public Queen queen() {
        return queen;
    }

    /** Workers currently inside the hive, by age. */
    public AgeHistogram workers() {
        return workers;
    }

    public BroodNursery brood() {
        return brood;
    }

    public NectarStore nectar() {
        return nectar;
    }

    public Comb comb() {
        return comb;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of workers inside the hive (all ages, including the dying pool).
     */
    public int workersInHive() {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: forageAge >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of workers in the hive younger than forageAge (house bees).
     */
    public int houseBees(int forageAge) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: forageAge >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the number of workers in the hive aged forageAge or older (not counting the dying
     *           pool): the bees an order can send out.
     */
    public int availableForagers(int forageAge) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: away != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the total adult population: workers in the hive plus the bees in every group in `away`.
     */
    public int population(List<ForagerGroup> away) {
        // TODO(human): implement per RME above.
        return 0;
    }
}
