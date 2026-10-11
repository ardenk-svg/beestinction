package com.michiganecodata.beestinction.sim.replay;

import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.orders.Order;
import com.michiganecodata.beestinction.sim.stats.RunSummary;
import com.michiganecodata.beestinction.sim.testutil.RandomPlayer;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayDeterminismTest {
    private static final int TICKS = 3 * 980; // three seasons

    /** Plays a game with a RandomPlayer and returns the orders it issued. */
    private static List<Order> playAndRecordOrders(GameSetup setup, long playerSeed) {
        Simulation sim = Simulation.create(setup);
        RandomPlayer player = new RandomPlayer(playerSeed, 20);
        for (int i = 0; i < TICKS && !sim.isGameOver(); i++) {
            player.act(sim);
            sim.tick();
        }
        return new ArrayList<>(player.issued());
    }

    @Test
    void sameSetupAndOrdersGiveIdenticalSummaries() {
        for (long seed : TestFixtures.SEEDS) {
            GameSetup setup = TestFixtures.setup(seed, TestFixtures.tinyMap(), DifficultyLevel.HARD,
                    TestFixtures.balance());
            List<Order> orders = playAndRecordOrders(setup, seed * 31 + 7);

            Simulation first = ReplayRunner.run(setup, orders, TICKS);
            Simulation second = ReplayRunner.run(setup, orders, TICKS);
            RunSummary a = first.summary();
            RunSummary b = second.summary();
            assertEquals(a, b, "seed " + seed);
            assertEquals(first.log().toJson(), second.log().toJson(), "seed " + seed);
            assertEquals(fingerprint(first.view()), fingerprint(second.view()), "seed " + seed);
            assertEquals(a, ReplayRunner.replay(setup, orders, TICKS), "seed " + seed);
        }
    }

    @Test
    void replayReproducesTheOriginalGame() {
        GameSetup setup = TestFixtures.setup(TestFixtures.SEED);
        Simulation original = Simulation.create(setup);
        RandomPlayer player = new RandomPlayer(99, 20);
        for (int i = 0; i < TICKS && !original.isGameOver(); i++) {
            player.act(original);
            original.tick();
        }
        Simulation replayed = ReplayRunner.run(setup, new ArrayList<>(player.issued()), original.currentTick());
        assertEquals(original.summary(), replayed.summary());
        assertEquals(fingerprint(original.view()), fingerprint(replayed.view()));
    }

    @Test
    void runningTicksOneAtATimeOrInBatchesGivesIdenticalResults() {
        // This is the guarantee the skip-to-spring fast-forward relies on.
        for (long seed : TestFixtures.SEEDS) {
            Simulation single = TestFixtures.newSimulation(seed);
            Simulation batched = TestFixtures.newSimulation(seed);
            for (int i = 0; i < TICKS; i++) {
                single.tick();
                fingerprint(single.view()); // reading the view between ticks must not change anything
            }
            int remaining = TICKS;
            int batch = 1;
            while (remaining > 0) {
                int n = Math.min(batch, remaining);
                batched.runTicks(n); // runs fewer if the game ends, exactly like tick() becoming a no-op
                remaining -= n;
                batch = batch * 2 + 1;
            }
            assertEquals(single.currentTick(), batched.currentTick(), "seed " + seed);
            assertEquals(single.summary(), batched.summary(), "seed " + seed);
            assertEquals(fingerprint(single.view()), fingerprint(batched.view()), "seed " + seed);
            assertEquals(single.log().toJson(), batched.log().toJson(), "seed " + seed);
        }
    }

    private static String fingerprint(GameStateView v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v.tick()).append('|').append(v.nectar()).append('|').append(v.nectarCapacity())
                .append('|').append(v.population()).append('|').append(v.workersInHive())
                .append('|').append(v.broodCount()).append('|').append(v.score())
                .append('|').append(v.totalBirths()).append('|').append(v.totalDeaths())
                .append('|').append(v.feedingLevel()).append('|').append(v.combSize())
                .append('|').append(v.isGameOver()).append('|').append(v.discoveredSpeciesIds());
        for (int age = 0; age < v.hiveWorkers().lifespanDays(); age++) {
            sb.append(',').append(v.hiveWorkers().count(age));
        }
        for (ForagerGroup g : v.groups()) {
            sb.append("|g").append(g.id()).append(':').append(g.bees().total()).append(':')
                    .append(g.posXMilli()).append(',').append(g.posYMilli()).append(':').append(g.state())
                    .append(':').append(g.nectarCarried());
        }
        return sb.toString();
    }
}
