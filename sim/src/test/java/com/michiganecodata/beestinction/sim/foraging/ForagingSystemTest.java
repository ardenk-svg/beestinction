package com.michiganecodata.beestinction.sim.foraging;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.NotificationType;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.orders.RecallOrder;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.world.TilePos;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until ForagingSystem (and the classes it uses: AgeHistogram, NectarStore, FlowerPatch, FogOfWar,
 * DiscoveryLog, ScalingDifficulty, RecallOrder) is implemented. Remove @Tag("pending") when it is.
 * Most tests run only the foraging system (no colony rules) on the tiny map in spring.
 */
@Tag("pending")
class ForagingSystemTest {
    private static final int MAX_TICKS = 20 * 35;

    private static GameState state(BalanceConfig cfg, int nectarCapacity, int nectar) {
        GameState s = TestFixtures.newState(TestFixtures.setup(TestFixtures.SEED, TestFixtures.tinyMap(),
                DifficultyLevel.MEDIUM, cfg));
        TestFixtures.setNectar(s, nectarCapacity, nectar);
        TestFixtures.addWorkers(s, 25, 100); // all foragers
        return s;
    }

    /** Runs only the foraging system for one tick (day start included). Returns the notifications posted. */
    private static List<Notification> foragingTick(GameState s) {
        ForagingSystem f = s.rules().foraging();
        if (s.clock().isDayStart()) {
            f.onDayStart(s);
        }
        for (ForagerGroup g : s.groups()) {
            g.snapshotPosition();
        }
        f.onTick(s);
        List<Notification> posted = s.drainOutbox();
        s.clock().advance();
        return posted;
    }

    private static List<Notification> runForagingUntilNoGroups(GameState s) {
        List<Notification> posted = new ArrayList<>();
        for (int i = 0; i < MAX_TICKS && !s.groups().isEmpty(); i++) {
            posted.addAll(foragingTick(s));
        }
        assertTrue(s.groups().isEmpty(), "groups never came home");
        return posted;
    }

    private static long distanceSquaredToHome(GameState s, ForagerGroup g) {
        long dx = g.posXMilli() - s.map().hivePos().centerXMilli();
        long dy = g.posYMilli() - s.map().hivePos().centerYMilli();
        return dx * dx + dy * dy;
    }

    @Test
    void travelTimeGrowsWithDistance() {
        BalanceConfig cfg = TestFixtures.balance();
        ForagingSystem f = new ForagingSystem(cfg);
        TilePos hive = TestFixtures.TINY_HIVE;
        int near = f.travelTicks(hive, TestFixtures.NEAR_POS, cfg);
        int mid = f.travelTicks(hive, TestFixtures.MID_POS, cfg);
        int far = f.travelTicks(hive, TestFixtures.FAR_POS, cfg);
        assertTrue(near < mid && mid < far, near + " " + mid + " " + far);
        assertTrue(f.travelTicks(hive, hive, cfg) >= 1);
        assertEquals(f.travelTicks(hive, TestFixtures.MID_POS, cfg), f.travelTicks(TestFixtures.MID_POS, hive, cfg));
    }

    @Test
    void aRoundTripToAMidDistancePatchTakesAboutTwoDays() {
        BalanceConfig cfg = TestFixtures.travelBalance();
        ForagingSystem f = new ForagingSystem(cfg);
        int roundTrip = 2 * f.travelTicks(TestFixtures.TINY_HIVE, TestFixtures.MID_POS, cfg);
        int day = cfg.ticksPerDay();
        assertTrue(roundTrip >= day * 3 / 2 && roundTrip <= day * 5 / 2, "round trip " + roundTrip + " ticks");
    }

    @Test
    void dispatchTakesForagersFromTheHive() {
        GameState s = state(TestFixtures.travelBalance(), 1000, 0);
        ForagerGroup g = s.rules().foraging().dispatch(s, 30, TestFixtures.NEAR_POS, false);
        assertNotNull(g);
        assertEquals(30, g.bees().total());
        assertEquals(70, s.workersInHive());
        assertEquals(1, s.groups().size());
        assertEquals(GroupState.OUTBOUND, g.state());
        assertEquals(TestFixtures.NEAR_PATCH_ID, g.targetPatch().id());
        assertTrue(g.travelTicksTotal() >= 1);
    }

    @Test
    void dispatchFailsWithoutEnoughForagers() {
        GameState s = state(TestFixtures.travelBalance(), 1000, 0);
        assertNull(s.rules().foraging().dispatch(s, 101, TestFixtures.NEAR_POS, false));
        assertEquals(100, s.workersInHive());
        assertTrue(s.groups().isEmpty());
    }

    @Test
    void capacityIsSharedAmongGroupsAtOnePatch() {
        GameState s = state(TestFixtures.travelBalance(), 100_000, 0);
        ForagingSystem f = s.rules().foraging();
        f.dispatch(s, 40, TestFixtures.NEAR_POS, false);
        f.dispatch(s, 40, TestFixtures.NEAR_POS, false);
        TreeSet<Integer> foragingDays = new TreeSet<>();
        for (int i = 0; i < MAX_TICKS && !s.groups().isEmpty(); i++) {
            for (ForagerGroup g : s.groups()) {
                if (g.state() == GroupState.FORAGING) {
                    foragingDays.add(s.dayOfRun());
                }
            }
            foragingTick(s);
        }
        int dailyCapacity = 10 * 4; // clover: 10 nectar per size per day, size 4, MEDIUM yield 100%
        long collected = s.stats().totalNectarCollected();
        assertTrue(collected > 0, "the groups brought nectar home");
        assertTrue(collected <= (long) dailyCapacity * Math.max(1, foragingDays.size()),
                "two groups collected " + collected + " from a patch with capacity " + dailyCapacity + " per day");
    }

    @Test
    void yieldStaysWithinYieldRandomPercent() {
        BalanceConfig cfg = TestFixtures.travelBalance();
        int y = cfg.yieldRandomPercent();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (long seed = 1; seed <= 30; seed++) {
            GameState s = TestFixtures.newState(TestFixtures.setup(seed, TestFixtures.tinyMap(),
                    DifficultyLevel.MEDIUM, cfg));
            TestFixtures.setNectar(s, 100_000, 0);
            TestFixtures.addWorkers(s, 25, 10);
            ForagerGroup g = s.rules().foraging().dispatch(s, 1, TestFixtures.BIG_POS, false); // plenty of capacity
            int carried = -1;
            for (int i = 0; i < MAX_TICKS && carried < 0; i++) {
                foragingTick(s);
                if (g.state() == GroupState.RETURNING) {
                    carried = g.nectarCarried();
                }
            }
            assertTrue(carried > 0, "seed " + seed + ": the bee collected nothing");
            min = Math.min(min, carried);
            max = Math.max(max, carried);
        }
        assertTrue((long) max * (100 - y) <= (long) (min + 1) * (100 + y),
                "collections ranged from " + min + " to " + max + ", more than +/-" + y + "%");
    }

    @Test
    void depositsRespectStorageCapacity() {
        GameState s = state(TestFixtures.travelBalance(), 100, 99);
        s.rules().foraging().dispatch(s, 50, TestFixtures.BIG_POS, false);
        for (int i = 0; i < MAX_TICKS && !s.groups().isEmpty(); i++) {
            foragingTick(s);
            assertTrue(s.nectar() <= s.nectarCapacity());
        }
        assertEquals(100, s.nectar());
        assertEquals(100, s.workersInHive(), "the bees came home");
    }

    @Test
    void standingOrdersReDispatchSurvivorsAndNeverRefill() {
        GameState s = state(TestFixtures.travelBalance(), 100_000, 0);
        ForagerGroup g = s.rules().foraging().dispatch(s, 10, TestFixtures.NEAR_POS, true);
        g.bees().killPercent(50); // 5 survivors
        boolean sawReturn = false;
        boolean sawSecondTrip = false;
        for (int i = 0; i < 3 * 35 && !sawSecondTrip; i++) {
            foragingTick(s);
            for (ForagerGroup x : s.groups()) {
                if (x.state() == GroupState.RETURNING) {
                    sawReturn = true;
                } else if (sawReturn && x.state() == GroupState.OUTBOUND) {
                    sawSecondTrip = true;
                }
            }
            assertTrue(s.beesOut() <= 5, "a standing order is never refilled");
        }
        assertTrue(sawSecondTrip, "the standing group set out again");
        assertEquals(5, s.beesOut());
        assertEquals(90, s.workersInHive());
        assertTrue(s.stats().totalNectarCollected() > 0, "the first trip's nectar was deposited");
    }

    @Test
    void recallTurnsTheGroupAround() {
        GameState s = state(TestFixtures.balance(), 100_000, 0); // slow bees: 200 ticks to the far patch
        ForagerGroup g = s.rules().foraging().dispatch(s, 10, TestFixtures.FAR_POS, false);
        for (int i = 0; i < 40; i++) {
            foragingTick(s);
        }
        long before = distanceSquaredToHome(s, g);
        assertTrue(before > 0);
        RecallOrder recall = new RecallOrder(g.id());
        assertTrue(recall.validate(s));
        recall.apply(s);
        assertEquals(GroupState.RETURNING, g.state());
        long last = before;
        int ticks = 0;
        while (!s.groups().isEmpty()) {
            foragingTick(s);
            ticks++;
            if (!s.groups().isEmpty()) {
                long d = distanceSquaredToHome(s, g);
                assertTrue(d <= last, "the group keeps getting closer to home");
                last = d;
            }
            assertTrue(ticks < 100, "a recalled group comes home in about the time it flew out");
        }
        assertEquals(100, s.workersInHive());
    }

    @Test
    void beesAgeDuringTrips() {
        GameState s = state(TestFixtures.balance(), 1000, 1000);
        TestFixtures.runRules(s, 1); // day 0 starts: the hive's bees are now 26 days old
        ForagerGroup g = s.rules().foraging().dispatch(s, 10, TestFixtures.FAR_POS, false); // 200 ticks away
        assertNotNull(g);
        int ageAtDispatch = -1;
        for (int age = 0; age < s.balance().lifespanDays(); age++) {
            if (g.bees().count(age) == 10) {
                ageAtDispatch = age;
            }
        }
        assertTrue(ageAtDispatch >= 0);
        TestFixtures.runRules(s, s.balance().ticksPerDay()); // past the start of day 1
        assertEquals(10, g.bees().count(ageAtDispatch + 1), "the group's bees aged one day while away");
    }

    @Test
    void scoutingAnEmptyTileIsValidAndBringsNoNectar() {
        GameState s = state(TestFixtures.travelBalance(), 1000, 0);
        assertNull(s.map().patchAt(TestFixtures.EMPTY_POS));
        assertNotNull(s.rules().foraging().dispatch(s, 5, TestFixtures.EMPTY_POS, false));
        runForagingUntilNoGroups(s);
        assertEquals(0, s.stats().totalNectarCollected());
        assertEquals(100, s.workersInHive());
    }

    @Test
    void theFirstCollectionOfASpeciesPostsOneNewSpeciesPopup() {
        GameState s = state(TestFixtures.travelBalance(), 100_000, 0);
        s.rules().foraging().dispatch(s, 5, TestFixtures.NEAR_POS, false);
        List<Notification> posted = runForagingUntilNoGroups(s);
        s.rules().foraging().dispatch(s, 5, TestFixtures.NEAR_POS, false);
        posted.addAll(runForagingUntilNoGroups(s));
        Map<NotificationType, Integer> counts = new HashMap<>();
        for (Notification n : posted) {
            counts.merge(n.type(), 1, Integer::sum);
        }
        assertEquals(1, counts.getOrDefault(NotificationType.NEW_SPECIES, 0));
        assertTrue(s.discoveredSpeciesIds().contains("clover"));
    }

    @Test
    void fogIsRevealedAroundTravelingGroups() {
        GameState s = state(TestFixtures.travelBalance(), 1000, 0);
        TilePos far = TestFixtures.FAR_POS;
        assertTrue(!s.isRevealed(far.x(), far.y()));
        s.rules().foraging().dispatch(s, 5, far, false);
        runForagingUntilNoGroups(s);
        assertTrue(s.isRevealed(far.x(), far.y()));
        assertTrue(s.isRevealed(15, 10), "tiles along the way are revealed too");
    }
}
