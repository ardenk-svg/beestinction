package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      Cancel a standing forage order: the group finishes its current trip and then stops (its bees
 *                rejoin the hive).
 * INVARIANTS:    groupId is fixed after construction.
 * COLLABORATORS: ForagerGroup, ForagingSystem, GameState, GameStateView.
 */
public final class CancelStandingOrder extends Order {
    private final int groupId;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the order.
     */
    public CancelStandingOrder(int groupId) {
        this.groupId = groupId;
    }

    public int groupId() {
        return groupId;
    }

    @Override
    public OrderType type() {
        return OrderType.CANCEL_STANDING;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if a group with this id exists, is standing, and has not already been cancelled.
     *           Unknown group ids are rejected.
     */
    @Override
    public boolean validate(GameStateView s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: the group
     * EFFECTS:  Marks the group cancelRequested; ForagingSystem stops it when it gets home.
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }

    @Override
    public void writeParams(JsonWriter w) {
        w.name("groupId").value(groupId);
    }
}
