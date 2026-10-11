package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      Change the feeding level. Level 0 is baseline (survival); each extra level raises the queen's
 *                egg laying (and, by a rule still to be decided, consumption).
 * INVARIANTS:    level is fixed after construction.
 * COLLABORATORS: Queen, GameState, GameStateView.
 */
public final class SetFeedingLevelOrder extends Order {
    private final int level;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the order.
     */
    public SetFeedingLevelOrder(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    @Override
    public OrderType type() {
        return OrderType.SET_FEEDING_LEVEL;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if 0 <= level <= cfg.maxFeedingLevel().
     */
    @Override
    public boolean validate(GameStateView s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: the queen
     * EFFECTS:  Sets the queen's feeding level to `level`.
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }

    @Override
    public void writeParams(JsonWriter w) {
        w.name("level").value(level);
    }
}
