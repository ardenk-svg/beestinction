package com.michiganecodata.beestinction.sim.testutil;

import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.foraging.GroupState;
import com.michiganecodata.beestinction.sim.orders.BuildCombOrder;
import com.michiganecodata.beestinction.sim.orders.CancelStandingOrder;
import com.michiganecodata.beestinction.sim.orders.ConvertBeesOrder;
import com.michiganecodata.beestinction.sim.orders.ForageOrder;
import com.michiganecodata.beestinction.sim.orders.Order;
import com.michiganecodata.beestinction.sim.orders.RecallOrder;
import com.michiganecodata.beestinction.sim.orders.SetFeedingLevelOrder;
import com.michiganecodata.beestinction.sim.util.GameRandom;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TilePos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OVERVIEW:      A bot that plays by issuing random VALID orders (valid according to the RMEs of the orders),
 *                using its own seeded GameRandom so its choices are reproducible and independent of the game's
 *                generator. Used by SmokeTest and ReplayDeterminismTest.
 * INVARIANTS:    Same seed + same game = same orders. Never touches the game except through Simulation.submit.
 * COLLABORATORS: Simulation, GameStateView, the Order classes, GameRandom.
 */
public final class RandomPlayer {
    private final GameRandom rng;
    private final int actPercent;
    private final List<Order> issued = new ArrayList<>();

    /** A player that considers acting on 5% of ticks. */
    public RandomPlayer(long seed) {
        this(seed, 5);
    }

    /**
     * REQUIRES: 0 <= actPercent <= 100
     * MODIFIES: nothing
     * EFFECTS:  Creates a player that tries to issue one order on actPercent% of ticks.
     */
    public RandomPlayer(long seed, int actPercent) {
        this.rng = new GameRandom(seed);
        this.actPercent = actPercent;
    }

    /** Every order this player has submitted, in order (with the ticks they were stamped with). */
    public List<Order> issued() {
        return Collections.unmodifiableList(issued);
    }

    /**
     * REQUIRES: sim != null
     * MODIFIES: sim (through submit), this
     * EFFECTS:  Call once before each tick. With probability actPercent% picks a random kind of order and, if a
     *           valid one of that kind exists in the current state, submits it with random valid parameters.
     */
    public void act(Simulation sim) {
        GameStateView v = sim.view();
        if (v.isGameOver() || !rng.chancePercent(actPercent)) {
            return;
        }
        Order order = null;
        switch (rng.nextInt(6)) {
            case 0:
                order = forage(v);
                break;
            case 1:
                order = recall(v);
                break;
            case 2:
                order = cancelStanding(v);
                break;
            case 3:
                order = buildComb(v);
                break;
            case 4:
                order = convert(v);
                break;
            default:
                order = new SetFeedingLevelOrder(rng.rangeInclusive(0, v.balance().maxFeedingLevel()));
                break;
        }
        if (order != null) {
            sim.submit(order);
            issued.add(order);
        }
    }

    private Order forage(GameStateView v) {
        int available = v.availableForagers();
        if (available <= 0 || v.season() == Season.WINTER) {
            return null;
        }
        int count = rng.rangeInclusive(1, available);
        GameMap map = v.map();
        List<FlowerPatch> patches = map.patches();
        if (!patches.isEmpty() && rng.chancePercent(80)) {
            FlowerPatch p = patches.get(rng.nextInt(patches.size()));
            return new ForageOrder(count, p.pos(), rng.chancePercent(30));
        }
        TilePos tile = new TilePos(rng.nextInt(map.width()), rng.nextInt(map.height()));
        return new ForageOrder(count, tile, false);
    }

    private Order recall(GameStateView v) {
        List<ForagerGroup> candidates = new ArrayList<>();
        for (ForagerGroup g : v.groups()) {
            if (g.state() != GroupState.RETURNING) {
                candidates.add(g);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return new RecallOrder(candidates.get(rng.nextInt(candidates.size())).id());
    }

    private Order cancelStanding(GameStateView v) {
        List<ForagerGroup> candidates = new ArrayList<>();
        for (ForagerGroup g : v.groups()) {
            if (g.isStanding() && !g.isCancelRequested()) {
                candidates.add(g);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return new CancelStandingOrder(candidates.get(rng.nextInt(candidates.size())).id());
    }

    private Order buildComb(GameStateView v) {
        int house = v.houseBees();
        if (house <= 0 || v.isCombExpanding() || v.nectar() < v.balance().combExpansionNectarCost()) {
            return null;
        }
        return new BuildCombOrder(rng.rangeInclusive(1, house));
    }

    private Order convert(GameStateView v) {
        int workers = v.workersInHive();
        if (!v.isConversionWindowOpen() || workers <= 0) {
            return null;
        }
        return new ConvertBeesOrder(rng.rangeInclusive(1, Math.max(1, workers / 4)));
    }
}
