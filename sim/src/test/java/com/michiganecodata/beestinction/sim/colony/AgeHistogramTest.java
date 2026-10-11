package com.michiganecodata.beestinction.sim.colony;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until AgeHistogram is implemented. Remove @Tag("pending") when it is, then run ./gradlew test. */
@Tag("pending")
class AgeHistogramTest {
    private static final int LIFESPAN = 42;
    private static final int TICKS_PER_DAY = 35;

    @Test
    void newHistogramIsEmpty() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        assertEquals(0, h.total());
        assertEquals(0, h.dying());
        for (int age = 0; age < LIFESPAN; age++) {
            assertEquals(0, h.count(age));
        }
    }

    @Test
    void addNewbornsAndAddAtAge() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addNewborns(5);
        h.addAtAge(30, 3);
        assertEquals(5, h.count(0));
        assertEquals(3, h.count(30));
        assertEquals(8, h.total());
        assertEquals(0, h.count(-1));
        assertEquals(0, h.count(LIFESPAN));
    }

    @Test
    void beginDayShiftsEveryAgeByOne() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(0, 5);
        h.addAtAge(10, 3);
        h.beginDay();
        assertEquals(0, h.count(0));
        assertEquals(5, h.count(1));
        assertEquals(0, h.count(10));
        assertEquals(3, h.count(11));
        assertEquals(8, h.total());
    }

    @Test
    void beginDayMovesTheOverAgeCohortToDying() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(LIFESPAN - 1, 7);
        h.addAtAge(LIFESPAN - 2, 2);
        int dying = h.beginDay();
        assertEquals(7, dying);
        assertEquals(7, h.dying());
        assertEquals(2, h.count(LIFESPAN - 1));
        assertEquals(9, h.total(), "dying bees are still alive until removed");
    }

    @Test
    void dyingSlicesSumToTheCohortSizeOverADay() {
        int[] sizes = {0, 1, 7, 34, 35, 36, 100, 1234};
        for (int size : sizes) {
            AgeHistogram h = new AgeHistogram(LIFESPAN);
            h.addAtAge(LIFESPAN - 1, size);
            h.beginDay();
            int sum = 0;
            for (int t = 0; t < TICKS_PER_DAY; t++) {
                int died = h.removeDyingSlice(t, TICKS_PER_DAY);
                assertTrue(died >= 0);
                assertTrue(died <= (size + TICKS_PER_DAY - 1) / TICKS_PER_DAY, "slices are spread over the day");
                sum += died;
            }
            assertEquals(size, sum, "cohort of " + size);
            assertEquals(0, h.dying());
            assertEquals(0, h.total());
        }
    }

    @Test
    void oldAgeDeathsAreNotAllAtOnce() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(LIFESPAN - 1, 70);
        h.beginDay();
        assertEquals(2, h.removeDyingSlice(0, TICKS_PER_DAY));
        assertEquals(68, h.total());
    }

    @Test
    void takeOldestTakesTheOldestFirst() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(5, 10);
        h.addAtAge(25, 5);
        h.addAtAge(30, 5);
        AgeHistogram taken = h.takeOldest(7, 21);
        assertEquals(7, taken.total());
        assertEquals(5, taken.count(30));
        assertEquals(2, taken.count(25));
        assertEquals(0, h.count(30));
        assertEquals(3, h.count(25));
        assertEquals(10, h.count(5));
        assertEquals(LIFESPAN, taken.lifespanDays());
    }

    @Test
    void takeOldestRespectsMinAgeAndTakesWhatIsThere() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(20, 10);
        h.addAtAge(21, 3);
        AgeHistogram taken = h.takeOldest(10, 21);
        assertEquals(3, taken.total());
        assertEquals(10, h.count(20));
        assertEquals(10, h.total());
    }

    @Test
    void takeOldestNeverTakesTheDyingPool() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(LIFESPAN - 1, 4);
        h.beginDay(); // 4 bees now dying
        h.addAtAge(30, 2);
        AgeHistogram taken = h.takeOldest(10, 0);
        assertEquals(2, taken.total());
        assertEquals(4, h.dying());
    }

    @Test
    void countAtLeastAndBelowAge() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(3, 4);
        h.addAtAge(21, 6);
        h.addAtAge(40, 1);
        assertEquals(7, h.countAtLeastAge(21));
        assertEquals(4, h.countBelowAge(21));
        assertEquals(11, h.countAtLeastAge(0));
        assertEquals(0, h.countBelowAge(0));
        assertEquals(0, h.countAtLeastAge(LIFESPAN));
        assertEquals(11, h.countBelowAge(LIFESPAN));
    }

    @Test
    void mergeConservesTotals() {
        AgeHistogram a = new AgeHistogram(LIFESPAN);
        a.addAtAge(3, 4);
        a.addAtAge(30, 6);
        AgeHistogram b = new AgeHistogram(LIFESPAN);
        b.addAtAge(3, 5);
        b.addAtAge(41, 1);
        a.merge(b);
        assertEquals(16, a.total());
        assertEquals(9, a.count(3));
        assertEquals(1, a.count(41));
        assertEquals(6, b.total(), "merge does not change the other histogram");
    }

    @Test
    void killPercentStaysWithinRounding() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(1, 10);
        h.addAtAge(2, 7);
        h.addAtAge(3, 3);
        int killed = h.killPercent(50);
        assertEquals(10, killed, "exactly floor(20 * 50 / 100)");
        assertEquals(10, h.total());
        int[] before = {10, 7, 3};
        for (int i = 0; i < before.length; i++) {
            int after = h.count(i + 1);
            int lost = before[i] - after;
            assertTrue(after >= 0);
            assertTrue(lost <= (before[i] * 50 + 99) / 100, "cohort " + (i + 1) + " lost too many: " + lost);
        }
    }

    @Test
    void killPercentNeverGoesNegative() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(1, 1);
        h.addAtAge(2, 1);
        h.addAtAge(3, 1);
        assertEquals(2, h.killPercent(99));
        assertEquals(1, h.total());
        for (int age = 0; age < LIFESPAN; age++) {
            assertTrue(h.count(age) >= 0);
        }
        assertEquals(0, h.killPercent(0));
        assertEquals(1, h.killPercent(100));
        assertEquals(0, h.total());
    }

    @Test
    void killAllEmptiesEverythingIncludingTheDyingPool() {
        AgeHistogram h = new AgeHistogram(LIFESPAN);
        h.addAtAge(LIFESPAN - 1, 3);
        h.beginDay();
        h.addAtAge(10, 4);
        assertEquals(7, h.killAll());
        assertEquals(0, h.total());
        assertEquals(0, h.dying());
    }
}
