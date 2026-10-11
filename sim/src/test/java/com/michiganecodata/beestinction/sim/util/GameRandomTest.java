package com.michiganecodata.beestinction.sim.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRandomTest {

    @Test
    void sameSeedGivesSameSequence() {
        GameRandom a = new GameRandom(42);
        GameRandom b = new GameRandom(42);
        for (int i = 0; i < 1000; i++) {
            assertEquals(a.nextLong(), b.nextLong());
            assertEquals(a.nextInt(1000), b.nextInt(1000));
            assertEquals(a.chancePercent(50), b.chancePercent(50));
        }
    }

    @Test
    void differentSeedsGiveDifferentSequences() {
        List<Long> a = draw(new GameRandom(1), 20);
        List<Long> b = draw(new GameRandom(2), 20);
        assertNotEquals(a, b);
    }

    @Test
    void matchesSplitMix64ReferenceValues() {
        // Locks the algorithm so desktop, web (TeaVM), and tests all produce the same numbers.
        GameRandom r = new GameRandom(0);
        assertEquals(0xE220A8397B1DCDAFL, r.nextLong());
        assertEquals(0x6E789E6AA1B965F4L, r.nextLong());
        assertEquals(0x06C45D188009454FL, r.nextLong());
        GameRandom r42 = new GameRandom(42);
        assertEquals(0xBDD732262FEB6E95L, r42.nextLong());
        assertEquals(0x28EFE333B266F103L, r42.nextLong());
    }

    @Test
    void rangeInclusiveStaysInBoundsAndHitsBothEnds() {
        GameRandom r = new GameRandom(7);
        boolean sawMin = false;
        boolean sawMax = false;
        for (int i = 0; i < 10_000; i++) {
            int v = r.rangeInclusive(-5, 7);
            assertTrue(v >= -5 && v <= 7, "out of range: " + v);
            sawMin |= v == -5;
            sawMax |= v == 7;
        }
        assertTrue(sawMin && sawMax);
    }

    @Test
    void rangeInclusiveWithMinEqualToMaxReturnsMin() {
        GameRandom r = new GameRandom(3);
        for (int i = 0; i < 100; i++) {
            assertEquals(9, r.rangeInclusive(9, 9));
        }
    }

    @Test
    void rangeInclusiveRejectsMinAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> new GameRandom(1).rangeInclusive(5, 4));
    }

    @Test
    void nextIntStaysBelowBoundAndRejectsNonPositiveBounds() {
        GameRandom r = new GameRandom(11);
        for (int i = 0; i < 10_000; i++) {
            int v = r.nextInt(3);
            assertTrue(v >= 0 && v < 3);
        }
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(0));
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(-1));
    }

    @Test
    void chancePercentZeroIsAlwaysFalseAndHundredIsAlwaysTrue() {
        GameRandom r = new GameRandom(5);
        for (int i = 0; i < 10_000; i++) {
            assertFalse(r.chancePercent(0));
            assertTrue(r.chancePercent(100));
        }
    }

    @Test
    void chanceBasisPointsZeroIsAlwaysFalseAndTenThousandIsAlwaysTrue() {
        GameRandom r = new GameRandom(5);
        for (int i = 0; i < 10_000; i++) {
            assertFalse(r.chanceBasisPoints(0));
            assertTrue(r.chanceBasisPoints(10_000));
        }
    }

    @Test
    void chanceCallsConsumeOneDrawWhateverTheArgument() {
        GameRandom a = new GameRandom(99);
        GameRandom b = new GameRandom(99);
        a.chancePercent(0);
        b.chancePercent(100);
        assertEquals(a.nextLong(), b.nextLong());
    }

    private static List<Long> draw(GameRandom r, int n) {
        List<Long> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(r.nextLong());
        }
        return out;
    }
}
