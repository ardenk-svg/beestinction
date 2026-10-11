package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      Expand the hive's comb with `beeCount` house bees as builders. Expensive in nectar: the full
 *                cost (cfg.combExpansionNectarCost()) is paid up front (Part I assumption 2).
 * INVARIANTS:    beeCount is fixed after construction.
 * COLLABORATORS: Comb, NectarStore, GameState, GameStateView.
 */
public final class BuildCombOrder extends Order {
    private final int beeCount;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the order.
     */
    public BuildCombOrder(int beeCount) {
        this.beeCount = beeCount;
    }

    public int beeCount() {
        return beeCount;
    }

    @Override
    public OrderType type() {
        return OrderType.BUILD_COMB;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if beeCount > 0, beeCount <= s.houseBees(), no expansion is already running, and the
     *           hive holds at least cfg.combExpansionNectarCost() nectar.
     */
    @Override
    public boolean validate(GameStateView s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: s.hive() (nectar, comb)
     * EFFECTS:  Withdraws cfg.combExpansionNectarCost() nectar and starts the expansion with beeCount builders.
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }

    @Override
    public void writeParams(JsonWriter w) {
        w.name("beeCount").value(beeCount);
    }
}
