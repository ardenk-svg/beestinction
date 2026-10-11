package com.michiganecodata.beestinction.sim.log;

import com.michiganecodata.beestinction.sim.orders.OrderType;

import java.util.Objects;

/**
 * OVERVIEW:      One logged order: the tick it was applied at, its type, whether it was accepted, and its
 *                parameters as a JSON object string (written by Order.writeParams).
 * INVARIANTS:    Immutable. tick >= 0; type and paramsJson non-null; paramsJson is a JSON object.
 * COLLABORATORS: GameLog, Order, OrderType.
 */
public final class OrderRecord {
    private final long tick;
    private final OrderType type;
    private final boolean accepted;
    private final String paramsJson;

    /**
     * REQUIRES: tick >= 0, type != null, paramsJson is a JSON object
     * MODIFIES: nothing
     * EFFECTS:  Creates the record.
     */
    public OrderRecord(long tick, OrderType type, boolean accepted, String paramsJson) {
        this.tick = tick;
        this.type = Objects.requireNonNull(type, "type");
        this.accepted = accepted;
        this.paramsJson = Objects.requireNonNull(paramsJson, "paramsJson");
    }

    public long tick() {
        return tick;
    }

    public OrderType type() {
        return type;
    }

    public boolean accepted() {
        return accepted;
    }

    /** The order's parameters as a JSON object, for example {"beeCount":10,"x":3,"y":4,"standing":false}. */
    public String paramsJson() {
        return paramsJson;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OrderRecord)) {
            return false;
        }
        OrderRecord r = (OrderRecord) o;
        return tick == r.tick && type == r.type && accepted == r.accepted && paramsJson.equals(r.paramsJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tick, type, accepted, paramsJson);
    }
}
