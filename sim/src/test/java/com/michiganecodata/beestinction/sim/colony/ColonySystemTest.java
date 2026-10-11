package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until ColonySystem (and the colony classes it uses) is implemented. Remove @Tag("pending") when it is.
 * These tests drive ColonySystem directly on a bare GameState, one tick at a time.
 */
@Tag("pending")
class ColonySystemTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();
    private final int ticksPerDay = cfg.ticksPerDay();

    /** Runs ColonySystem only, for n ticks (day start first when the clock is at a day start). */
    private static void runColony(GameState s, int n) {
        ColonySystem colony = s.rules().colony();
        for (int i = 0; i < n; i++) {
            if (s.clock().isDayStart()) {
                colony.onDayStart(s);
            }
            colony.onTick(s);
            s.clock().advance();
        }
    }

    private GameState state(int nectar, int workerAge, int workers) {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        int capacity = s.hive().comb().nectarCapacity(cfg);
        TestFixtures.setNectar(s, capacity, nectar);
        TestFixtures.addWorkers(s, workerAge, workers);
        return s;
    }

    @Test
    void initializeColonySetsUpTheStartingColony() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        s.rules().colony().initializeColony(s);
        assertEquals(cfg.startingWorkers(), s.workersInHive());
        assertEquals(Math.min(cfg.startingNectar(), s.nectarCapacity()), s.nectar());
        assertEquals(s.hive().comb().nectarCapacity(cfg), s.nectarCapacity());
        assertTrue(s.availableForagers() > 0, "some starting workers can forage on day 1");
        assertEquals(0, s.totalBirths());
    }

    @Test
    void theQueenIsFedBeforeWorkersWhenNectarIsShort() {
        GameState s = state(cfg.queenDailyRation(), 5, 50);
        runColony(s, ticksPerDay + 1); // day 0, then the start of day 1 records how day 0 went
        assertEquals(0, s.hive().queen().consecutiveUnfedDays(), "the queen got her full ration");
        assertEquals(0, s.nectar());
    }

    @Test
    void unfedWorkersDie() {
        GameState s = state(0, 5, 100);
        runColony(s, 2 * ticksPerDay + 1);
        assertTrue(s.workersInHive() < 100);
        assertTrue(s.deaths(DeathCause.STARVATION) > 0);
        assertEquals(100 - s.workersInHive(), s.totalDeaths());
    }

    @Test
    void oldAgeDeathsAreSpreadAcrossTheDay() {
        GameState s = state(1000, cfg.lifespanDays() - 1, ticksPerDay); // 35 bees reach their lifespan today
        runColony(s, 1);
        assertEquals(ticksPerDay - 1, s.workersInHive(), "one bee dies on the first tick, not all at once");
        runColony(s, ticksPerDay - 1);
        assertEquals(0, s.workersInHive());
        assertEquals(ticksPerDay, s.deaths(DeathCause.OLD_AGE));
    }

    @Test
    void emergedBeesJoinAgeZero() {
        GameState s = state(1000, 5, 10);
        s.hive().brood().layEggs(12, 0);
        s.hive().brood().batches().get(0).addPoints(cfg.broodThresholdPoints());
        runColony(s, 1);
        assertEquals(12, s.hiveWorkers().count(0));
        assertEquals(12, s.totalBirths());
        assertTrue(s.broodCount() < 12, "the emerged batch left the nursery");
    }

    @Test
    void populationNeverExceedsCombCapacity() {
        GameState s = state(1000, 5, 190);
        s.hive().brood().layEggs(50, 0);
        s.hive().brood().batches().get(0).addPoints(cfg.broodThresholdPoints());
        for (int i = 0; i < 2 * ticksPerDay; i++) {
            runColony(s, 1);
            assertTrue(s.population() <= s.populationCapacity(),
                    "population " + s.population() + " > capacity " + s.populationCapacity());
        }
    }

    @Test
    void theQueenStarvingEndsTheGame() {
        GameState s = state(0, 5, 0);
        runColony(s, 3 * ticksPerDay + 1);
        assertTrue(s.isGameOver());
        assertEquals(DeathCause.QUEEN_STARVED, s.gameOverCause());
    }
}
