package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until FlowerPatch is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class FlowerPatchTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();
    private final int toxicTicks = cfg.pesticideToxicDays() * cfg.ticksPerDay(); // 490

    private static FlowerPatch clover() {
        return new FlowerPatch(1, TestFixtures.flower("clover"), new TilePos(3, 4), 4); // spring + summer
    }

    @Test
    void bloomsOnlyInItsSeasons() {
        FlowerPatch p = clover();
        assertTrue(p.isBlooming(Season.SPRING));
        assertTrue(p.isBlooming(Season.SUMMER));
        assertFalse(p.isBlooming(Season.FALL));
        assertFalse(p.isBlooming(Season.WINTER));
    }

    @Test
    void neverSprayedIsNotToxic() {
        FlowerPatch p = clover();
        assertFalse(p.isToxic(0));
        assertFalse(p.isToxic(100_000));
    }

    @Test
    void toxicFor14DaysAfterASpray() {
        FlowerPatch p = clover();
        p.spray(100, toxicTicks);
        assertTrue(p.isToxic(100));
        assertTrue(p.isToxic(100 + toxicTicks - 1));
        assertFalse(p.isToxic(100 + toxicTicks));
    }

    @Test
    void aResprayResetsTheTimer() {
        FlowerPatch p = clover();
        p.spray(100, toxicTicks);
        p.spray(300, toxicTicks);
        assertTrue(p.isToxic(100 + toxicTicks));
        assertTrue(p.isToxic(300 + toxicTicks - 1));
        assertFalse(p.isToxic(300 + toxicTicks));
    }

    @Test
    void dailyCapacityDependsOnSpeciesSizeSeasonAndYield() {
        FlowerPatch p = clover(); // 10 nectar per size per day, size 4
        p.resetDailyCapacity(Season.SPRING, 100);
        assertEquals(40, p.nectarRemainingToday());
        p.resetDailyCapacity(Season.SUMMER, 85);
        assertEquals(34, p.nectarRemainingToday());
        p.resetDailyCapacity(Season.FALL, 100);
        assertEquals(0, p.nectarRemainingToday(), "nothing to collect out of season");
    }

    @Test
    void takeNeverExceedsTodaysRemainingCapacity() {
        FlowerPatch p = clover();
        p.resetDailyCapacity(Season.SPRING, 100);
        assertEquals(25, p.take(25));
        assertEquals(15, p.take(25));
        assertEquals(0, p.take(5));
        assertEquals(0, p.nectarRemainingToday());
        p.resetDailyCapacity(Season.SPRING, 100);
        assertEquals(40, p.nectarRemainingToday(), "a new day resets the capacity");
    }
}
