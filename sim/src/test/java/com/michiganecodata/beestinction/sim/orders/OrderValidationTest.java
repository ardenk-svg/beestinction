package com.michiganecodata.beestinction.sim.orders;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.foraging.GroupState;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.world.TilePos;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until the orders' validate() and apply() are implemented. Remove @Tag("pending") when they are. */
@Tag("pending")
class OrderValidationTest {
    private final BalanceConfig cfg = TestFixtures.balance();

    /** 10 foragers (age 25) and 20 house bees (age 5) on the tiny map, day 20 of spring. */
    private GameState state() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        TestFixtures.setNectar(s, 5000, 1000);
        TestFixtures.addWorkers(s, 25, 10);
        TestFixtures.addWorkers(s, 5, 20);
        TestFixtures.advanceToDay(s, 20);
        return s;
    }

    @Test
    void rejectsCountsGreaterThanAvailableForagers() {
        GameState s = state();
        assertEquals(10, s.availableForagers());
        assertTrue(new ForageOrder(10, TestFixtures.NEAR_POS, false).validate(s));
        assertFalse(new ForageOrder(11, TestFixtures.NEAR_POS, false).validate(s));
        assertFalse(new ForageOrder(0, TestFixtures.NEAR_POS, false).validate(s));
        assertFalse(new ForageOrder(-3, TestFixtures.NEAR_POS, false).validate(s));
    }

    @Test
    void forageTargetsMustBeOnTheMapAndStandingOrdersNeedAPatch() {
        GameState s = state();
        assertFalse(new ForageOrder(5, new TilePos(-1, 0), false).validate(s));
        assertFalse(new ForageOrder(5, new TilePos(TestFixtures.TINY_SIZE, 0), false).validate(s));
        assertTrue(new ForageOrder(5, TestFixtures.EMPTY_POS, false).validate(s), "scouting is allowed");
        assertFalse(new ForageOrder(5, TestFixtures.EMPTY_POS, true).validate(s));
        assertTrue(new ForageOrder(5, TestFixtures.NEAR_POS, true).validate(s));
    }

    @Test
    void noForagingInWinter() {
        GameState s = state();
        TestFixtures.advanceToDay(s, TestFixtures.WINTER_DAY);
        assertFalse(new ForageOrder(5, TestFixtures.NEAR_POS, false).validate(s));
    }

    @Test
    void rejectsConversionOutsideTheWindow() {
        GameState s = state(); // spring
        assertFalse(new ConvertBeesOrder(5).validate(s));
        TestFixtures.advanceToDay(s, TestFixtures.FALL_DAY + cfg.conversionWindowStartDayOfFall());
        assertTrue(new ConvertBeesOrder(5).validate(s));
        assertFalse(new ConvertBeesOrder(31).validate(s), "more bees than are in the hive");
        assertFalse(new ConvertBeesOrder(0).validate(s));
    }

    @Test
    void conversionTurnsBeesIntoNectar() {
        GameState s = state();
        TestFixtures.advanceToDay(s, TestFixtures.FALL_DAY + cfg.conversionWindowStartDayOfFall());
        int nectarBefore = s.nectar();
        ConvertBeesOrder order = new ConvertBeesOrder(10);
        assertTrue(order.validate(s));
        order.apply(s);
        assertEquals(20, s.workersInHive());
        assertEquals(10, s.deaths(com.michiganecodata.beestinction.sim.DeathCause.CONVERTED));
        int gained = s.nectar() - nectarBefore;
        assertTrue(gained >= 10 * cfg.raiseCostPerBee() * cfg.conversionValuePercentMin() / 100
                && gained <= 10 * cfg.raiseCostPerBee() * cfg.conversionValuePercentMax() / 100);
    }

    @Test
    void rejectsUnknownGroupIds() {
        GameState s = state();
        assertFalse(new RecallOrder(999).validate(s));
        assertFalse(new CancelStandingOrder(999).validate(s));
    }

    @Test
    void recallAndCancelRules() {
        GameState s = state();
        ForagerGroup oneTrip = TestFixtures.addGroup(s, 5, 25, TestFixtures.FAR_POS, false);
        ForagerGroup standing = TestFixtures.addGroup(s, 5, 25, TestFixtures.NEAR_POS, true);
        assertTrue(new RecallOrder(oneTrip.id()).validate(s));
        assertFalse(new CancelStandingOrder(oneTrip.id()).validate(s), "only standing groups can be cancelled");
        assertTrue(new CancelStandingOrder(standing.id()).validate(s));
        new CancelStandingOrder(standing.id()).apply(s);
        assertTrue(standing.isCancelRequested());
        assertFalse(new CancelStandingOrder(standing.id()).validate(s), "already cancelled");
        oneTrip.setState(GroupState.RETURNING);
        assertFalse(new RecallOrder(oneTrip.id()).validate(s), "already coming home");
    }

    @Test
    void feedingLevelMustBeInRange() {
        GameState s = state();
        assertFalse(new SetFeedingLevelOrder(-1).validate(s));
        for (int level = 0; level <= cfg.maxFeedingLevel(); level++) {
            assertTrue(new SetFeedingLevelOrder(level).validate(s));
        }
        assertFalse(new SetFeedingLevelOrder(cfg.maxFeedingLevel() + 1).validate(s));
    }

    @Test
    void acceptedOrdersChangeStateOnlyAtTheirTick() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.runTicks(3);
        sim.submit(new SetFeedingLevelOrder(2));
        assertEquals(0, sim.view().feedingLevel(), "nothing changes when the order is submitted");
        sim.tick();
        assertEquals(2, sim.view().feedingLevel());
        assertTrue(sim.log().orders().get(0).accepted());
        assertEquals(3, sim.log().orders().get(0).tick());

        int foragers = sim.view().availableForagers();
        assertTrue(foragers > 0);
        sim.submit(new ForageOrder(foragers, TestFixtures.NEAR_POS, false));
        assertEquals(0, sim.view().beesOut());
        sim.tick();
        assertEquals(foragers, sim.view().beesOut());
    }
}
