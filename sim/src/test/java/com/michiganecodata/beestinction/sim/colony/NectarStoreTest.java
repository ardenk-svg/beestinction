package com.michiganecodata.beestinction.sim.colony;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pending until NectarStore is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class NectarStoreTest {

    @Test
    void depositsAreCappedAndTheOverflowIsLost() {
        NectarStore s = new NectarStore();
        s.setCapacity(100);
        assertEquals(80, s.deposit(80));
        assertEquals(20, s.deposit(50));
        assertEquals(100, s.amount());
        assertEquals(0, s.deposit(10));
        assertEquals(100, s.amount());
    }

    @Test
    void withdrawalsNeverGoNegative() {
        NectarStore s = new NectarStore();
        s.setCapacity(100);
        s.deposit(30);
        assertEquals(30, s.withdraw(50));
        assertEquals(0, s.amount());
        assertEquals(0, s.withdraw(5));
        assertEquals(0, s.amount());
    }

    @Test
    void loweringTheCapacityDropsTheExcess() {
        NectarStore s = new NectarStore();
        s.setCapacity(100);
        s.deposit(90);
        s.setCapacity(60);
        assertEquals(60, s.capacity());
        assertEquals(60, s.amount());
    }

    @Test
    void newStoreIsEmptyWithNoCapacity() {
        NectarStore s = new NectarStore();
        assertEquals(0, s.amount());
        assertEquals(0, s.capacity());
        assertEquals(0, s.deposit(10));
    }
}
