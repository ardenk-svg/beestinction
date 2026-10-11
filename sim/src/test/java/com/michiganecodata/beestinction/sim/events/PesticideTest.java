package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.NotificationType;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.foraging.ForagingSystem;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until PesticideSprayEvent and the pesticide part of ForagingSystem are implemented.
 * Remove @Tag("pending") when they are.
 */
@Tag("pending")
class PesticideTest {
    private final BalanceConfig cfg = TestFixtures.travelBalance();
    private final int toxicTicks = cfg.pesticideToxicDays() * cfg.ticksPerDay();

    private GameState state(long seed) {
        GameState s = TestFixtures.newState(TestFixtures.setup(seed, TestFixtures.tinyMap(), DifficultyLevel.MEDIUM,
                cfg));
        TestFixtures.setNectar(s, 100_000, 0);
        TestFixtures.addWorkers(s, 25, 100);
        return s;
    }

    /** Sends all 100 bees to the near patch and runs foraging until they are home. Returns PESTICIDE popups. */
    private static int forageAtNearPatch(GameState s) {
        ForagingSystem f = s.rules().foraging();
        f.dispatch(s, 100, TestFixtures.NEAR_POS, false);
        int popups = 0;
        for (int i = 0; i < 10 * 35 && !s.groups().isEmpty(); i++) {
            if (s.clock().isDayStart()) {
                f.onDayStart(s);
            }
            for (ForagerGroup g : s.groups()) {
                g.snapshotPosition();
            }
            f.onTick(s);
            for (Notification n : s.drainOutbox()) {
                if (n.type() == NotificationType.PESTICIDE_DEATHS) {
                    popups++;
                    assertTrue(s.deaths(DeathCause.PESTICIDE) > 0, "the popup comes after the deaths");
                }
            }
            if (s.deaths(DeathCause.PESTICIDE) == 0) {
                assertEquals(0, popups, "no warning before bees die");
            }
            s.clock().advance();
        }
        return popups;
    }

    @Test
    void aSprayMakesThePatchToxicFor14DaysWithNoWarning() {
        GameState s = state(TestFixtures.SEED);
        TestFixtures.advanceClockTo(s, 100);
        new PesticideSprayEvent(TestFixtures.NEAR_PATCH_ID).apply(s);
        FlowerPatch near = TestFixtures.patch(s, TestFixtures.NEAR_PATCH_ID);
        assertTrue(near.isToxic(100));
        assertTrue(near.isToxic(100 + toxicTicks - 1));
        assertFalse(near.isToxic(100 + toxicTicks));
        assertTrue(s.pendingNotifications().isEmpty(), "spraying posts no notification");
    }

    @Test
    void aResprayResetsTheTimer() {
        GameState s = state(TestFixtures.SEED);
        new PesticideSprayEvent(TestFixtures.NEAR_PATCH_ID).apply(s);
        TestFixtures.advanceClockTo(s, 200);
        new PesticideSprayEvent(TestFixtures.NEAR_PATCH_ID).apply(s);
        FlowerPatch near = TestFixtures.patch(s, TestFixtures.NEAR_PATCH_ID);
        assertTrue(near.isToxic(200 + toxicTicks - 1));
        assertFalse(near.isToxic(200 + toxicTicks));
    }

    @Test
    void foragingAtAToxicPatchKills70To80PercentOfTheBees() {
        for (long seed : TestFixtures.SEEDS) {
            GameState s = state(seed);
            new PesticideSprayEvent(TestFixtures.NEAR_PATCH_ID).apply(s);
            int popups = forageAtNearPatch(s);
            int killed = s.deaths(DeathCause.PESTICIDE);
            assertTrue(killed >= 70 && killed <= 80, "seed " + seed + ": killed " + killed + " of 100");
            assertEquals(1, popups, "one PESTICIDE_DEATHS popup, after the deaths");
            assertEquals(100 - killed, s.workersInHive());
        }
    }

    @Test
    void aNonToxicPatchKillsNone() {
        GameState s = state(TestFixtures.SEED);
        assertEquals(0, forageAtNearPatch(s));
        assertEquals(0, s.deaths(DeathCause.PESTICIDE));
        assertEquals(100, s.workersInHive());
    }

    @Test
    void sprayingAnUnknownPatchDoesNothing() {
        GameState s = state(TestFixtures.SEED);
        new PesticideSprayEvent(999).apply(s);
        for (FlowerPatch p : s.map().patches()) {
            assertFalse(p.isToxic(s.tick()));
        }
    }
}
