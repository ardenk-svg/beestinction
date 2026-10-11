package com.michiganecodata.beestinction.sim.score;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until ScoreKeeper (and AgeHistogram, Hive.population, ScalingDifficulty) are implemented.
 * Remove @Tag("pending") when they are.
 */
@Tag("pending")
class ScoreKeeperTest {

    @Test
    void dailyPointsArePopulationTimesMultiplierAndTheSlicesSumToThem() {
        GameState s = TestFixtures.newState(TestFixtures.SEED); // MEDIUM
        TestFixtures.addWorkers(s, 5, 100);
        ScoreKeeper score = s.scoreKeeper();
        int ticksPerDay = s.balance().ticksPerDay();
        long expected = (long) s.population() * s.difficulty().scoreMultiplierPercent(0) / 100;
        assertTrue(expected > 0);

        score.onDayStart(s);
        long previous = score.points();
        long minSlice = Long.MAX_VALUE;
        long maxSlice = Long.MIN_VALUE;
        for (int t = 0; t < ticksPerDay; t++) {
            score.onTick(s);
            long slice = score.points() - previous;
            minSlice = Math.min(minSlice, slice);
            maxSlice = Math.max(maxSlice, slice);
            previous = score.points();
            s.clock().advance();
        }
        assertEquals(expected, score.points());
        assertTrue(minSlice >= 0);
        assertTrue(maxSlice - minSlice <= 1, "points accrue smoothly, every tick");
    }

    @Test
    void anEmptyColonyScoresNothing() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        ScoreKeeper score = s.scoreKeeper();
        score.onDayStart(s);
        for (int t = 0; t < s.balance().ticksPerDay(); t++) {
            score.onTick(s);
            s.clock().advance();
        }
        assertEquals(0, score.points());
    }
}
