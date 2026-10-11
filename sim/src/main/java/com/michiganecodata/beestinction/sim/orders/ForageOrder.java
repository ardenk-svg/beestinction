package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;
import com.michiganecodata.beestinction.sim.world.TilePos;

import java.util.Objects;

/**
 * OVERVIEW:      Send foragers to a tile or patch. A one-trip order (standing = false) makes one trip; a standing
 *                order (standing = true) repeats trips to a patch until cancelled, shrinking as bees die (it is
 *                never refilled) and continuing into winter until the player cancels it.
 *                Sending bees to an empty, unexplored tile is valid ("scouting"; Part I assumption 6).
 * INVARIANTS:    beeCount and target are fixed after construction; target is non-null.
 * COLLABORATORS: ForagingSystem.dispatch, GameState, GameStateView.
 */
public final class ForageOrder extends Order {
    private final int beeCount;
    private final TilePos target;
    private final boolean standing;

    /**
     * REQUIRES: target != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the order (validity is checked later, by validate).
     */
    public ForageOrder(int beeCount, TilePos target, boolean standing) {
        this.beeCount = beeCount;
        this.target = Objects.requireNonNull(target, "target");
        this.standing = standing;
    }

    public int beeCount() {
        return beeCount;
    }

    public TilePos target() {
        return target;
    }

    public boolean isStanding() {
        return standing;
    }

    @Override
    public OrderType type() {
        return OrderType.FORAGE;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if the game is not over, beeCount > 0, beeCount <= s.availableForagers(), the target
     *           is inside the map, it is not WINTER (foraging is impossible in winter), and, for a standing order,
     *           there is a patch on the target tile.
     */
    @Override
    public boolean validate(GameStateView s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: s
     * EFFECTS:  Dispatches the group: s.rules().foraging().dispatch(s, beeCount, target, standing).
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }

    @Override
    public void writeParams(JsonWriter w) {
        w.name("beeCount").value(beeCount);
        w.name("x").value(target.x());
        w.name("y").value(target.y());
        w.name("standing").value(standing);
    }
}
