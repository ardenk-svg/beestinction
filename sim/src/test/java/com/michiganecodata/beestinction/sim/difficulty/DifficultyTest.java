package com.michiganecodata.beestinction.sim.difficulty;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.events.EventType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until ScalingDifficulty is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class DifficultyTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();
    private final Difficulty easy = ScalingDifficulty.forLevel(DifficultyLevel.EASY, cfg);
    private final Difficulty medium = ScalingDifficulty.forLevel(DifficultyLevel.MEDIUM, cfg);
    private final Difficulty hard = ScalingDifficulty.forLevel(DifficultyLevel.HARD, cfg);
    private static final int[] DAYS = {0, 7, 14, 30, 60, 112, 300, 1000};

    @Test
    void escalationRisesOverTimeForEveryLevel() {
        for (Difficulty d : new Difficulty[] {easy, medium, hard}) {
            assertEquals(0, d.escalation(0));
            int previous = 0;
            for (int day : DAYS) {
                assertTrue(d.escalation(day) >= previous, "escalation must never decrease");
                previous = d.escalation(day);
            }
            assertTrue(d.escalation(70) > d.escalation(0));
        }
    }

    @Test
    void hardYieldsLessThanEasy() {
        for (int day : DAYS) {
            assertTrue(hard.nectarYieldPercent(day) < easy.nectarYieldPercent(day), "day " + day);
        }
        assertEquals(cfg.difficultyProfile(DifficultyLevel.MEDIUM).yieldPercent(), medium.nectarYieldPercent(0));
    }

    @Test
    void scoreMultiplierIsHigherOnHarderLevelsAndRisesOverTime() {
        for (int day : DAYS) {
            assertTrue(easy.scoreMultiplierPercent(day) < medium.scoreMultiplierPercent(day), "day " + day);
            assertTrue(medium.scoreMultiplierPercent(day) < hard.scoreMultiplierPercent(day), "day " + day);
        }
        for (Difficulty d : new Difficulty[] {easy, medium, hard}) {
            assertTrue(d.scoreMultiplierPercent(70) > d.scoreMultiplierPercent(0));
        }
        assertEquals(cfg.difficultyProfile(DifficultyLevel.EASY).baseMultiplierPercent(),
                easy.scoreMultiplierPercent(0));
    }

    @Test
    void gracePeriodCoversTheFirstDays() {
        assertTrue(medium.inGracePeriod(0));
        assertTrue(medium.inGracePeriod(cfg.gracePeriodDays() - 1));
        assertFalse(medium.inGracePeriod(cfg.gracePeriodDays()));
    }

    @Test
    void eventChancesAreZeroDuringGraceAndValidAfter() {
        for (EventType t : EventType.values()) {
            assertEquals(0, hard.eventChanceBasisPoints(t, 0));
            int later = hard.eventChanceBasisPoints(t, cfg.gracePeriodDays() + 7);
            assertTrue(later > 0 && later <= 10_000);
            assertTrue(hard.eventChanceBasisPoints(t, 1000) <= 10_000);
        }
    }

    @Test
    void severityShiftIsAPercentage() {
        for (int day : DAYS) {
            int s = hard.severityShiftPercent(day);
            assertTrue(s >= 0 && s <= 100, "day " + day + ": " + s);
        }
    }
}
