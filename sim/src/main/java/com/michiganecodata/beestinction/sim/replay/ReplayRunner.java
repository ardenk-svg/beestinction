package com.michiganecodata.beestinction.sim.replay;

import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.Order;
import com.michiganecodata.beestinction.sim.stats.RunSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * OVERVIEW:      Replays a run headlessly: a fresh Simulation from the setup, with each order submitted at its
 *                tick. Because the simulation is deterministic, the replay reproduces the original game exactly.
 * INVARIANTS:    Stateless.
 * COLLABORATORS: Simulation, GameSetup, Order, RunSummary, tests.
 */
public final class ReplayRunner {
    private ReplayRunner() {
    }

    /**
     * REQUIRES: setup, orders != null; maxTicks >= 0
     * MODIFIES: the orders (each is re-stamped with the tick it is submitted at, which equals its tick)
     * EFFECTS:  Runs a fresh Simulation until game over or until maxTicks ticks have run, submitting each order
     *           just before the tick it is stamped with (orders are taken in tick order; orders with equal ticks
     *           keep their list order). Returns the final summary (or the summary so far if maxTicks was hit).
     */
    public static RunSummary replay(GameSetup setup, List<Order> orders, long maxTicks) {
        return run(setup, orders, maxTicks).summary();
    }

    /**
     * REQUIRES: setup, orders != null; maxTicks >= 0
     * MODIFIES: the orders (re-stamped as in replay)
     * EFFECTS:  Same as replay, but returns the Simulation so its log and state can be inspected.
     */
    public static Simulation run(GameSetup setup, List<Order> orders, long maxTicks) {
        Simulation sim = Simulation.create(setup);
        List<Order> sorted = new ArrayList<>(orders);
        // Stable insertion sort by tick (keeps the submission order of orders with equal ticks).
        for (int i = 1; i < sorted.size(); i++) {
            Order o = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && sorted.get(j).tick() > o.tick()) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, o);
        }
        int next = 0;
        while (!sim.isGameOver() && sim.currentTick() < maxTicks) {
            long now = sim.currentTick();
            while (next < sorted.size() && sorted.get(next).tick() <= now) {
                sim.submit(sorted.get(next));
                next++;
            }
            sim.tick();
        }
        return sim;
    }
}
