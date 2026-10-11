package com.michiganecodata.beestinction.sim.orders;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderQueueTest {

    private static Order at(long tick) {
        Order o = new SetFeedingLevelOrder(1);
        o.stampTick(tick);
        return o;
    }

    @Test
    void ordersAreDrainedOnlyAtTheirTick() {
        OrderQueue q = new OrderQueue();
        Order o = at(5);
        q.enqueue(o);
        assertTrue(q.drainForTick(4).isEmpty());
        assertEquals(Collections.singletonList(o), q.drainForTick(5));
        assertTrue(q.drainForTick(6).isEmpty());
        assertTrue(q.isEmpty());
    }

    @Test
    void ordersAreDrainedInSubmissionOrder() {
        OrderQueue q = new OrderQueue();
        Order a = at(3);
        Order b = at(4);
        Order c = at(3);
        Order d = at(3);
        q.enqueue(a);
        q.enqueue(b);
        q.enqueue(c);
        q.enqueue(d);
        assertEquals(Arrays.asList(a, c, d), q.drainForTick(3));
        assertEquals(Collections.singletonList(b), q.drainForTick(4));
    }

    @Test
    void unstampedOrdersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new OrderQueue().enqueue(new SetFeedingLevelOrder(1)));
    }
}
