package com.michiganecodata.beestinction.sim.colony;

/**
 * OVERVIEW:      The hive's nectar, the only resource. Storage is capped by the comb; nectar delivered beyond
 *                the capacity is lost.
 * INVARIANTS:    0 <= amount <= capacity.
 * COLLABORATORS: Hive, Comb (sets the capacity), ColonySystem (feeding), ForagingSystem (deposits), orders.
 */
public final class NectarStore {
    private int amount;
    private int capacity;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates an empty store with capacity 0 (the colony setup sets the real capacity).
     */
    public NectarStore() {
        this.amount = 0;
        this.capacity = 0;
    }

    /** Nectar currently stored. */
    public int amount() {
        return amount;
    }

    /** Maximum nectar that can be stored. */
    public int capacity() {
        return capacity;
    }

    /**
     * REQUIRES: c >= 0
     * MODIFIES: this
     * EFFECTS:  Sets the storage capacity to c. If more than c nectar is stored, the excess is lost.
     */
    public void setCapacity(int c) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Stores as much of n as fits and returns the amount accepted (min(n, capacity - amount)).
     *           The rest is lost.
     */
    public int deposit(int n) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Removes up to n nectar and returns the amount actually withdrawn (min(n, amount)).
     *           Never makes the amount negative.
     */
    public int withdraw(int n) {
        // TODO(human): implement per RME above.
        return 0;
    }
}
