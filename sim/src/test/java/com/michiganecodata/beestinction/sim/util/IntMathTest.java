package com.michiganecodata.beestinction.sim.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntMathTest {

    @Test
    void isqrtIsExactFloorForSmallNumbers() {
        for (long n = 0; n <= 100_000; n++) {
            long r = IntMath.isqrt(n);
            assertEquals(true, r * r <= n && (r + 1) * (r + 1) > n, "isqrt(" + n + ") = " + r);
        }
    }

    @Test
    void isqrtHandlesLargeNumbers() {
        assertEquals(3_037_000_499L, IntMath.isqrt(Long.MAX_VALUE));
        assertEquals(1_000_000_000L, IntMath.isqrt(1_000_000_000_000_000_000L));
        assertEquals(999_999_999L, IntMath.isqrt(1_000_000_000_000_000_000L - 1));
    }

    @Test
    void isqrtRejectsNegatives() {
        assertThrows(IllegalArgumentException.class, () -> IntMath.isqrt(-1));
    }

    @Test
    void clampLimitsToRange() {
        assertEquals(0, IntMath.clamp(-5, 0, 10));
        assertEquals(10, IntMath.clamp(50, 0, 10));
        assertEquals(7, IntMath.clamp(7, 0, 10));
    }

    @Test
    void percentOfRoundsDown() {
        assertEquals(0, IntMath.percentOf(1, 99));
        assertEquals(50, IntMath.percentOf(100, 50));
        assertEquals(33, IntMath.percentOf(100, 33));
        assertEquals(3, IntMath.percentOf(7, 50));
        assertEquals(-4, IntMath.percentOf(-7, 50));
        assertEquals(2_000_000_000, IntMath.percentOf(2_000_000_000, 100)); // no overflow
    }

    @Test
    void basisPointsOfRoundsDown() {
        assertEquals(1, IntMath.basisPointsOf(100, 100));
        assertEquals(0, IntMath.basisPointsOf(99, 100));
        assertEquals(7500, IntMath.basisPointsOf(10_000, 7500));
    }
}
