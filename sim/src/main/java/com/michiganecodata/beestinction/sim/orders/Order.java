package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

/**
 * OVERVIEW:      A player action that changes the game (the Command pattern). The UI creates an order and calls
 *                Simulation.submit, which stamps it with the current tick and queues it. At that tick Simulation
 *                calls validate; if it returns true, apply; and records the result in the GameLog.
 *                Because every change goes through orders, seed + orders replays the whole game.
 * INVARIANTS:    tick is -1 until Simulation.submit stamps it, then >= 0. validate never modifies anything.
 *                apply is only called right after validate returned true, in the same tick.
 * COLLABORATORS: Simulation, OrderQueue, GameState, GameStateView, GameLog, JsonWriter.
 */
public abstract class Order {
    private long tick = -1;

    /** The tick this order was issued at (-1 if it has not been submitted yet). */
    public final long tick() {
        return tick;
    }

    /**
     * REQUIRES: tick >= 0; only Simulation.submit (and ReplayRunner through it) calls this
     * MODIFIES: this
     * EFFECTS:  Stamps the order with the tick it applies at.
     */
    public final void stampTick(long tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must be >= 0: " + tick);
        }
        this.tick = tick;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the kind of order.
     */
    public abstract OrderType type();

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if the order can be applied to the current state.
     */
    public abstract boolean validate(GameStateView s);

    /**
     * REQUIRES: validate(s) just returned true
     * MODIFIES: s
     * EFFECTS:  Applies the order.
     */
    public abstract void apply(GameState s);

    /**
     * REQUIRES: w is inside an open JSON object
     * MODIFIES: w
     * EFFECTS:  Writes this order's parameters as name/value pairs (for the GameLog).
     */
    public abstract void writeParams(JsonWriter w);

    @Override
    public String toString() {
        JsonWriter w = new JsonWriter();
        w.beginObject();
        writeParams(w);
        w.endObject();
        return type() + "@" + tick + w;
    }
}
