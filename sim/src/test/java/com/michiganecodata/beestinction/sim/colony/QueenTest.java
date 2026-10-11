package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until Queen is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class QueenTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();
    private final int limit = cfg.queenStarvationDays();

    @Test
    void starvesAfterExactlyTwoConsecutiveUnfedDays() {
        Queen q = new Queen();
        assertFalse(q.hasStarved(limit));
        q.recordDay(false);
        assertFalse(q.hasStarved(limit));
        q.recordDay(false);
        assertTrue(q.hasStarved(limit));
        assertEquals(2, q.consecutiveUnfedDays());
    }

    @Test
    void aFedDayResetsTheCounter() {
        Queen q = new Queen();
        q.recordDay(false);
        q.recordDay(true);
        assertEquals(0, q.consecutiveUnfedDays());
        q.recordDay(false);
        assertFalse(q.hasStarved(limit));
        assertEquals(1, q.consecutiveUnfedDays());
    }

    @Test
    void noEggsInWinter() {
        Queen q = new Queen();
        q.setFeedingLevel(cfg.maxFeedingLevel());
        assertEquals(0, q.dailyEggTarget(cfg, Season.WINTER));
    }

    @Test
    void higherFeedingLevelMeansMoreEggs() {
        Queen q = new Queen();
        assertEquals(cfg.eggsPerDayBase(), q.dailyEggTarget(cfg, Season.SPRING));
        q.setFeedingLevel(2);
        assertEquals(2, q.feedingLevel());
        assertEquals(cfg.eggsPerDayBase() + 2 * cfg.eggsPerDayPerFeedingLevel(), q.dailyEggTarget(cfg, Season.SUMMER));
        int level2 = q.dailyEggTarget(cfg, Season.FALL);
        q.setFeedingLevel(3);
        assertTrue(q.dailyEggTarget(cfg, Season.FALL) > level2);
    }
}
