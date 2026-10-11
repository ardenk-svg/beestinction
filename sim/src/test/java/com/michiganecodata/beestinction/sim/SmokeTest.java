package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.testutil.InvariantChecker;
import com.michiganecodata.beestinction.sim.testutil.RandomPlayer;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.world.GameMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Long headless games: 200 seeds x RandomPlayer on each test map, until game over or 5 in-game years, checking
 * the invariants every day. Tagged "smoke" (run with ./gradlew smokeTest) and "pending" until Milestone 1 is
 * complete: remove @Tag("pending") then, and CI will run it on every pull request.
 */
@Tag("smoke")
@Tag("pending")
class SmokeTest {
    private static final int SEEDS = 200;
    private static final long MAX_TICKS = 5L * 3920; // 5 in-game years

    @Test
    void randomPlayersOnTheTinyMapNeverBreakInvariants() {
        playMany(TestFixtures.tinyMap());
    }

    @Test
    void randomPlayersOnTheTestMapNeverBreakInvariants() {
        playMany(TestFixtures.testMapCopy());
    }

    private static void playMany(GameMap map) {
        for (int i = 0; i < SEEDS; i++) {
            long seed = 1000L + i;
            DifficultyLevel level = DifficultyLevel.values()[i % DifficultyLevel.values().length];
            Simulation sim = Simulation.create(TestFixtures.setup(seed, map, level, TestFixtures.balance()));
            RandomPlayer player = new RandomPlayer(seed * 6364136223846793005L + 1442695040888963407L);
            InvariantChecker checker = new InvariantChecker();
            try {
                checker.check(sim.view());
                while (!sim.isGameOver() && sim.currentTick() < MAX_TICKS) {
                    player.act(sim);
                    sim.tick();
                    if (sim.view().tickOfDay() == 0 || sim.isGameOver()) {
                        checker.check(sim.view());
                    }
                }
                sim.runTicks(100); // after game over nothing may change
                checker.check(sim.view());
            } catch (Throwable t) {
                throw new AssertionError("seed " + seed + " (" + level + ") on map " + map.id() + " failed at tick "
                        + sim.currentTick() + ": " + t.getMessage(), t);
            }
        }
    }
}
