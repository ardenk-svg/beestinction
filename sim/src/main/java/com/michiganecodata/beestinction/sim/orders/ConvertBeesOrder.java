package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      Convert workers (any age, including old ones) into nectar during the conversion window, from
 *                mid-fall until winter starts. A converted bee is worth 10-30% of its raise cost. This is an
 *                intentional abstraction for playability, not real bee behavior.
 * INVARIANTS:    beeCount is fixed after construction.
 * COLLABORATORS: WinterSystem (window, conversionValue), Hive, StatsRecorder, GameState, GameStateView.
 */
public final class ConvertBeesOrder extends Order {
    private final int beeCount;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the order.
     */
    public ConvertBeesOrder(int beeCount) {
        this.beeCount = beeCount;
    }

    public int beeCount() {
        return beeCount;
    }

    @Override
    public OrderType type() {
        return OrderType.CONVERT_BEES;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if s.isConversionWindowOpen(), beeCount > 0, and beeCount <= s.workersInHive().
     *           Rejects conversion outside the window.
     */
    @Override
    public boolean validate(GameStateView s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: s (hive workers and nectar, stats, random)
     * EFFECTS:  Removes beeCount workers from the hive (oldest first), records them as CONVERTED deaths, and
     *           deposits s.rules().winter().conversionValue(beeCount, s.random(), cfg) nectar (capped by storage).
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
