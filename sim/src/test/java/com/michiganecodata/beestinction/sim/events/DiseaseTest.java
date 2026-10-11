package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.NotificationType;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until DiseaseOutbreakEvent and EventScheduler are implemented. Remove @Tag("pending") when they are.
 */
@Tag("pending")
class DiseaseTest {

    @Test
    void kills40To70PercentOfWorkersInTheHiveAndAway() {
        for (long seed : TestFixtures.SEEDS) {
            GameState s = TestFixtures.newState(seed);
            TestFixtures.addWorkers(s, 10, 100);
            ForagerGroup away = TestFixtures.addGroup(s, 100, 30, TestFixtures.FAR_POS, false);
            TestFixtures.advanceToDay(s, 30);
            new DiseaseOutbreakEvent().apply(s);
            int hiveLeft = s.workersInHive();
            int awayLeft = away.bees().total();
            assertTrue(hiveLeft >= 30 && hiveLeft <= 60, "seed " + seed + ": hive kept " + hiveLeft + " of 100");
            assertTrue(awayLeft >= 30 && awayLeft <= 60, "seed " + seed + ": group kept " + awayLeft + " of 100");
            assertEquals(200 - hiveLeft - awayLeft, s.deaths(DeathCause.DISEASE));
            boolean posted = false;
            for (Notification n : s.pendingNotifications()) {
                posted |= n.type() == NotificationType.DISEASE_OUTBREAK;
            }
            assertTrue(posted, "a DISEASE_OUTBREAK popup is posted");
        }
    }

    @Test
    void theQueenAndBroodAreUnaffected() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        TestFixtures.addWorkers(s, 10, 100);
        s.hive().brood().layEggs(20, 0);
        new DiseaseOutbreakEvent().apply(s);
        assertFalse(s.isGameOver());
        assertEquals(0, s.hive().queen().consecutiveUnfedDays());
        assertEquals(20, s.broodCount());
    }

    @Test
    void neverDuringTheGracePeriodOrWinter() {
        BalanceConfig cfg = TestFixtures.alwaysEventsBalance(); // 100% event chance whenever events are allowed
        GameState s = TestFixtures.newState(TestFixtures.setup(TestFixtures.SEED, TestFixtures.tinyMap(),
                DifficultyLevel.HARD, cfg));
        EventScheduler scheduler = s.rules().scheduler();
        for (int day = 0; day < cfg.gracePeriodDays(); day++) {
            TestFixtures.advanceToDay(s, day);
            assertTrue(scheduler.rollDailyEvents(s).isEmpty(), "no events on grace day " + day);
        }
        TestFixtures.advanceToDay(s, 20);
        List<GameEvent> spring = scheduler.rollDailyEvents(s);
        assertTrue(containsType(spring, EventType.DISEASE_OUTBREAK), "events happen once the grace period ends");
        for (int day = TestFixtures.WINTER_DAY; day < TestFixtures.WINTER_DAY + cfg.daysPerSeason(); day++) {
            TestFixtures.advanceToDay(s, day);
            assertTrue(scheduler.rollDailyEvents(s).isEmpty(), "no events on winter day " + day);
        }
    }

    @Test
    void pesticideSpraysTargetARealPatch() {
        BalanceConfig cfg = TestFixtures.alwaysEventsBalance();
        GameState s = TestFixtures.newState(TestFixtures.setup(TestFixtures.SEED, TestFixtures.tinyMap(),
                DifficultyLevel.HARD, cfg));
        TestFixtures.advanceToDay(s, 20);
        boolean sawSpray = false;
        for (GameEvent e : s.rules().scheduler().rollDailyEvents(s)) {
            if (e instanceof PesticideSprayEvent) {
                sawSpray = true;
                assertTrue(s.map().patchById(((PesticideSprayEvent) e).patchId()) != null);
            }
        }
        assertTrue(sawSpray);
    }

    private static boolean containsType(List<GameEvent> events, EventType type) {
        for (GameEvent e : events) {
            if (e.type() == type) {
                return true;
            }
        }
        return false;
    }
}
