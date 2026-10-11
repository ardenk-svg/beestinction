package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      Recall a group: it turns around and comes home.
 *                DECISION: recalling a standing group also cancels it (it will not set out again).
 * INVARIANTS:    groupId is fixed after construction.
 * COLLABORATORS: ForagerGroup, GroupState, GameState, GameStateView.
 */
public final class RecallOrder extends Order {
    private final int groupId;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the order.
     */
    public RecallOrder(int groupId) {
        this.groupId = groupId;
    }

    public int groupId() {
        return groupId;
    }

    @Override
    public OrderType type() {
        return OrderType.RECALL;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if a group with this id exists and it is not already RETURNING.
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
     * EFFECTS:  Turns the group around: state RETURNING, flying home from where it is now (the way home takes as
     *           long as the distance from its current position). A standing group is also cancelled.
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
