package com.michiganecodata.beestinction.sim.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntSmoothingTest {
    private static final int TICKS_PER_DAY = 35;

    @Test
    void slicesSumExactlyToTheTotalForTotals0To10000() {
        for (long total = 0; total <= 10_000; total++) {
            long sum = 0;
            for (int t = 0; t < TICKS_PER_DAY; t++) {
                sum += IntSmoothing.sliceForTick(total, t, TICKS_PER_DAY);
            }
            assertEquals(total, sum, "total " + total);
        }
    }

    @Test
    void everySliceIsNonNegative() {
        for (long total = 0; total <= 10_000; total++) {
            for (int t = 0; t < TICKS_PER_DAY; t++) {
                assertTrue(IntSmoothing.sliceForTick(total, t, TICKS_PER_DAY) >= 0);
            }
        }
    }

    @Test
    void slicesDifferByAtMostOne() {
        for (long total = 0; total <= 10_000; total++) {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (int t = 0; t < TICKS_PER_DAY; t++) {
                long s = IntSmoothing.sliceForTick(total, t, TICKS_PER_DAY);
                min = Math.min(min, s);
                max = Math.max(max, s);
            }
            assertTrue(max - min <= 1, "total " + total + ": slices range from " + min + " to " + max);
        }
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> IntSmoothing.sliceForTick(-1, 0, 35));
        assertThrows(IllegalArgumentException.class, () -> IntSmoothing.sliceForTick(10, 35, 35));
        assertThrows(IllegalArgumentException.class, () -> IntSmoothing.sliceForTick(10, -1, 35));
        assertThrows(IllegalArgumentException.class, () -> IntSmoothing.sliceForTick(10, 0, 0));
    }
}
