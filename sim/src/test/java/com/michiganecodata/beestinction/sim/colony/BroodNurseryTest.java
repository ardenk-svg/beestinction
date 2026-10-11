package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until BroodBatch and BroodNursery are implemented. Remove @Tag("pending") when they are. */
@Tag("pending")
class BroodNurseryTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();
    private final int threshold = cfg.broodThresholdPoints();

    private int ticksUntilReady(int nurses) {
        BroodNursery n = new BroodNursery();
        n.layEggs(10, 0);
        int ticks = 0;
        while (!n.batches().get(0).isReady(threshold)) {
            n.develop(nurses, cfg);
            ticks++;
            assertTrue(ticks < 100_000, "brood never develops");
        }
        return ticks;
    }

    @Test
    void batchEmergesAtTheThreshold() {
        BroodNursery n = new BroodNursery();
        n.layEggs(10, 0);
        for (int i = 0; i < threshold - 1; i++) {
            n.develop(0, cfg); // base speed: 1 point per tick
        }
        assertEquals(0, n.collectEmerged(threshold));
        assertEquals(10, n.total());
        n.develop(0, cfg);
        assertEquals(10, n.collectEmerged(threshold));
        assertEquals(0, n.total());
        assertTrue(n.batches().isEmpty());
    }

    @Test
    void moreNursesMeansFewerTicksToEmerge() {
        assertTrue(ticksUntilReady(500) < ticksUntilReady(100));
        assertTrue(ticksUntilReady(100) < ticksUntilReady(0));
    }

    @Test
    void zeroNursesStillDevelopsAtBaseSpeed() {
        assertEquals(threshold / cfg.broodBasePointsPerTick(), ticksUntilReady(0));
    }

    @Test
    void collectEmergedRemovesOnlyReadyBatches() {
        BroodNursery n = new BroodNursery();
        n.layEggs(5, 0);
        for (int i = 0; i < threshold - 10; i++) {
            n.develop(0, cfg);
        }
        n.layEggs(3, 1);
        for (int i = 0; i < 10; i++) {
            n.develop(0, cfg);
        }
        assertEquals(5, n.collectEmerged(threshold));
        assertEquals(1, n.batches().size());
        assertEquals(3, n.total());
        assertFalse(n.batches().get(0).isReady(threshold));
    }

    @Test
    void eggsLaidOnTheSameDayJoinOneBatch() {
        BroodNursery n = new BroodNursery();
        n.layEggs(5, 0);
        n.layEggs(3, 0);
        assertEquals(1, n.batches().size());
        assertEquals(8, n.total());
        n.layEggs(2, 1);
        assertEquals(2, n.batches().size());
        n.layEggs(0, 2);
        assertEquals(2, n.batches().size(), "laying 0 eggs changes nothing");
    }

    @Test
    void batchCountsPointsAndReadiness() {
        BroodBatch b = new BroodBatch(4, 3);
        b.addPoints(699);
        assertFalse(b.isReady(700));
        b.addPoints(1);
        assertTrue(b.isReady(700));
        b.addEggs(2);
        assertEquals(6, b.count());
        assertEquals(3, b.layDay());
    }

    @Test
    void killAllRemovesEveryBatch() {
        BroodNursery n = new BroodNursery();
        n.layEggs(5, 0);
        n.layEggs(3, 1);
        assertEquals(8, n.killAll());
        assertEquals(0, n.total());
    }
}
