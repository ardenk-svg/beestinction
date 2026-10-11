package com.michiganecodata.beestinction.sim.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      Orders waiting for their tick. Simulation.submit enqueues orders stamped with the current tick;
 *                Simulation.tick drains the orders for the tick being run.
 * INVARIANTS:    Orders are kept in submission order. Every queued order has tick >= 0.
 * COLLABORATORS: Order, Simulation.
 */
public final class OrderQueue {
    private final List<Order> pending = new ArrayList<>();

    /**
     * REQUIRES: o != null, o.tick() >= 0
     * MODIFIES: this
     * EFFECTS:  Adds o to the end of the queue. Throws IllegalArgumentException if o has not been stamped.
     */
    public void enqueue(Order o) {
        Objects.requireNonNull(o, "order");
        if (o.tick() < 0) {
            throw new IllegalArgumentException("order has not been stamped with a tick: " + o);
        }
        pending.add(o);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Removes and returns, in submission order, every order whose tick is <= `tick`. Orders stamped
     *           for a later tick stay queued. (Simulation stamps orders with the tick about to run, so in practice
     *           every drained order has exactly this tick.)
     *           DECISION: an order stamped in the past is applied late rather than silently lost.
     */
    public List<Order> drainForTick(long tick) {
        List<Order> due = new ArrayList<>();
        List<Order> later = new ArrayList<>();
        for (Order o : pending) {
            if (o.tick() <= tick) {
                due.add(o);
            } else {
                later.add(o);
            }
        }
        pending.clear();
        pending.addAll(later);
        return due;
    }

    /** Number of queued orders. */
    public int size() {
        return pending.size();
    }

    public boolean isEmpty() {
        return pending.isEmpty();
    }
}
