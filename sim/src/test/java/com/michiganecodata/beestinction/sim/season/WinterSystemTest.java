package com.michiganecodata.beestinction.sim.season;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.NotificationType;
import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.colony.ColonySystem;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.util.GameRandom;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until WinterSystem (and the colony rules it relies on) is implemented. Remove @Tag("pending") when it is.
 * Days used below (0-based day of run, default config): spring 0-27, summer 28-55, fall 56-83, winter 84-111.
 */
@Tag("pending")
class WinterSystemTest {
    private final BalanceConfig cfg = TestFixtures.balance();
    private final WinterSystem winter = new WinterSystem(cfg);
    private final int dieOffDay = TestFixtures.WINTER_DAY + cfg.winterDieOffDay() - 1; // winter day 4

    private GameState stateOnDay(int day) {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        TestFixtures.advanceToDay(s, day);
        return s;
    }

    private static boolean posted(GameState s, NotificationType type) {
        for (Notification n : s.pendingNotifications()) {
            if (n.type() == type) {
                return true;
            }
        }
        return false;
    }

    @Test
    void theConversionWindowOpensOnlyFromMidFallUntilWinterStarts() {
        int start = TestFixtures.FALL_DAY + cfg.conversionWindowStartDayOfFall();
        assertFalse(winter.isConversionWindowOpen(stateOnDay(TestFixtures.SPRING_DAY).clock()));
        assertFalse(winter.isConversionWindowOpen(stateOnDay(TestFixtures.SUMMER_DAY + 20).clock()));
        assertFalse(winter.isConversionWindowOpen(stateOnDay(start - 1).clock()));
        assertTrue(winter.isConversionWindowOpen(stateOnDay(start).clock()));
        assertTrue(winter.isConversionWindowOpen(stateOnDay(TestFixtures.WINTER_DAY - 1).clock()));
        assertFalse(winter.isConversionWindowOpen(stateOnDay(TestFixtures.WINTER_DAY).clock()));
    }

    @Test
    void theConversionValueIs10To30PercentOfTheRaiseCost() {
        GameRandom rng = new GameRandom(TestFixtures.SEED);
        int low = 100 * cfg.raiseCostPerBee() * cfg.conversionValuePercentMin() / 100;
        int high = 100 * cfg.raiseCostPerBee() * cfg.conversionValuePercentMax() / 100;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 1000; i++) {
            int v = winter.conversionValue(100, rng, cfg);
            assertTrue(v >= low && v <= high, v + " not in [" + low + ", " + high + "]");
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        assertTrue(max > min, "the value is rolled, not fixed");
        assertEquals(0, winter.conversionValue(0, rng, cfg));
    }

    @Test
    void groupsOutsideMoreThanTwoWinterDaysFreeze() {
        GameState s = stateOnDay(TestFixtures.WINTER_DAY);
        TestFixtures.addGroup(s, 10, 30, TestFixtures.FAR_POS, false);
        WinterSystem w = s.rules().winter();
        w.onSeasonStart(s, Season.WINTER);
        int limit = cfg.freezeAfterWinterDays() * cfg.ticksPerDay();
        for (int i = 0; i < limit; i++) {
            w.onTick(s);
            s.clock().advance();
        }
        assertEquals(1, s.groups().size(), "still alive after exactly " + limit + " ticks outside");
        assertFalse(posted(s, NotificationType.FREEZE_DEATHS));
        w.onTick(s);
        assertTrue(s.groups().isEmpty(), "frozen once outside for more than " + limit + " ticks");
        assertEquals(10, s.deaths(DeathCause.FREEZING));
        assertTrue(posted(s, NotificationType.FREEZE_DEATHS));
    }

    @Test
    void groupsDoNotFreezeOutsideWinter() {
        GameState s = stateOnDay(TestFixtures.FALL_DAY);
        TestFixtures.addGroup(s, 10, 30, TestFixtures.FAR_POS, false);
        for (int i = 0; i < 5 * cfg.ticksPerDay(); i++) {
            s.rules().winter().onTick(s);
            s.clock().advance();
        }
        assertEquals(1, s.groups().size());
    }

    @Test
    void allWorkersDieOnWinterDay4() {
        GameState before = stateOnDay(dieOffDay - 1);
        TestFixtures.addWorkers(before, 10, 30);
        before.rules().winter().onDayStart(before);
        assertEquals(30, before.workersInHive(), "nothing happens on winter day 3");

        GameState s = stateOnDay(dieOffDay);
        TestFixtures.addWorkers(s, 10, 30);
        s.rules().winter().onDayStart(s);
        assertEquals(0, s.workersInHive());
        assertEquals(30, s.deaths(DeathCause.WINTER_DIE_OFF));
        assertTrue(posted(s, NotificationType.WINTER_DIE_OFF));
    }

    @Test
    void broodEmergingInWinterDies() {
        assertTrue(winter.broodDiesOnEmergence(Season.WINTER));
        assertFalse(winter.broodDiesOnEmergence(Season.SPRING));
        assertFalse(winter.broodDiesOnEmergence(Season.SUMMER));
        assertFalse(winter.broodDiesOnEmergence(Season.FALL));
    }

    @Test
    void thereIsNoEggLayingInWinter() {
        GameState s = stateOnDay(dieOffDay + 1);
        TestFixtures.setNectar(s, s.hive().comb().nectarCapacity(cfg), 1000);
        s.hive().queen().setFeedingLevel(cfg.maxFeedingLevel());
        runColonyDay(s);
        assertEquals(0, s.broodCount());
    }

    @Test
    void skipIsAvailableOnlyAfterTheDieOff() {
        assertFalse(winter.isSkipAvailable(stateOnDay(TestFixtures.SUMMER_DAY)));
        assertFalse(winter.isSkipAvailable(stateOnDay(TestFixtures.WINTER_DAY)));
        assertFalse(winter.isSkipAvailable(stateOnDay(dieOffDay - 1)));
        assertTrue(winter.isSkipAvailable(stateOnDay(dieOffDay)));
        assertTrue(winter.isSkipAvailable(stateOnDay(TestFixtures.WINTER_DAY + cfg.daysPerSeason() - 1)));
        assertFalse(winter.isSkipAvailable(stateOnDay(TestFixtures.WINTER_DAY + cfg.daysPerSeason())), "spring");
    }

    @Test
    void theQueenKeepsEatingThroughWinter() {
        GameState s = stateOnDay(dieOffDay + 1);
        TestFixtures.setNectar(s, s.hive().comb().nectarCapacity(cfg), 100);
        runColonyDay(s);
        assertEquals(100 - cfg.queenDailyRation(), s.nectar());
        s.rules().colony().onDayStart(s);
        assertEquals(0, s.hive().queen().consecutiveUnfedDays());
        assertFalse(s.isGameOver());
    }

    /** Runs only the colony rules for one full day. */
    private static void runColonyDay(GameState s) {
        ColonySystem colony = s.rules().colony();
        for (int t = 0; t < s.balance().ticksPerDay(); t++) {
            if (s.clock().isDayStart()) {
                colony.onDayStart(s);
            }
            colony.onTick(s);
            s.clock().advance();
        }
    }
}
