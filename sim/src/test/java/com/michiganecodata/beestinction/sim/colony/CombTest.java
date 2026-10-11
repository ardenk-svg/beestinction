package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.orders.BuildCombOrder;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pending until Comb (and BuildCombOrder, NectarStore, AgeHistogram, Hive) are implemented.
 * Remove @Tag("pending") when they are.
 */
@Tag("pending")
class CombTest {
    private final BalanceConfig cfg = BalanceConfig.defaults();

    @Test
    void capacitiesComeFromTheSize() {
        Comb comb = new Comb(1, cfg.combExpansionBeeTicks());
        assertEquals(cfg.combPopulationPerSize(), comb.populationCapacity(cfg));
        assertEquals(cfg.combNectarPerSize(), comb.nectarCapacity(cfg));
        assertFalse(comb.isExpanding());
    }

    @Test
    void capacitiesGrowWhenTheExpansionCompletes() {
        Comb comb = new Comb(1, 3500);
        comb.startExpansion(35);
        assertTrue(comb.isExpanding());
        comb.progress(99); // 3465 bee-ticks
        assertEquals(1, comb.size());
        assertTrue(comb.isExpanding());
        comb.progress(1); // 3500 bee-ticks: done
        assertEquals(2, comb.size());
        assertFalse(comb.isExpanding());
        assertEquals(2 * cfg.combPopulationPerSize(), comb.populationCapacity(cfg));
        assertEquals(2 * cfg.combNectarPerSize(), comb.nectarCapacity(cfg));
        comb.progress(1000);
        assertEquals(2, comb.size(), "progress does nothing when not expanding");
    }

    @Test
    void expansionCostsNectarUpFront() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        TestFixtures.setNectar(s, 5000, 1000);
        TestFixtures.addWorkers(s, 5, 50); // house bees
        BuildCombOrder order = new BuildCombOrder(10);
        assertTrue(order.validate(s));
        order.apply(s);
        assertEquals(1000 - cfg.combExpansionNectarCost(), s.nectar());
        assertTrue(s.isCombExpanding());
    }

    @Test
    void expansionNeedsEnoughNectarAndHouseBees() {
        GameState s = TestFixtures.newState(TestFixtures.SEED);
        TestFixtures.setNectar(s, 5000, cfg.combExpansionNectarCost() - 1);
        TestFixtures.addWorkers(s, 5, 50);
        assertFalse(new BuildCombOrder(10).validate(s), "not enough nectar");
        TestFixtures.setNectar(s, 5000, 5000);
        assertFalse(new BuildCombOrder(51).validate(s), "more builders than house bees");
        assertFalse(new BuildCombOrder(0).validate(s));
        assertTrue(new BuildCombOrder(50).validate(s));
    }
}
