package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.colony.ColonySystem;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.events.EventScheduler;
import com.michiganecodata.beestinction.sim.foraging.ForagingSystem;
import com.michiganecodata.beestinction.sim.season.WinterSystem;

import java.util.Objects;

/**
 * OVERVIEW:      The rule systems of one run, bundled together. GameState holds them so that orders (for example
 *                ForageOrder.apply calls rules().foraging().dispatch) and the read-only view (conversion window,
 *                skip availability) can use them. Simulation drives them in the order of CLAUDE.md D.5.
 *                (ScoreKeeper is held directly by GameState, as the spec lists it there.)
 * INVARIANTS:    Every system is non-null and created from the same BalanceConfig.
 * COLLABORATORS: ColonySystem, ForagingSystem, WinterSystem, EventScheduler, GameState, Simulation.
 */
public final class GameRules {
    private final ColonySystem colony;
    private final ForagingSystem foraging;
    private final WinterSystem winter;
    private final EventScheduler scheduler;

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Creates every rule system from cfg.
     */
    public GameRules(BalanceConfig cfg) {
        Objects.requireNonNull(cfg, "cfg");
        this.colony = new ColonySystem(cfg);
        this.foraging = new ForagingSystem(cfg);
        this.winter = new WinterSystem(cfg);
        this.scheduler = new EventScheduler(cfg);
    }

    public ColonySystem colony() {
        return colony;
    }

    public ForagingSystem foraging() {
        return foraging;
    }

    public WinterSystem winter() {
        return winter;
    }

    public EventScheduler scheduler() {
        return scheduler;
    }
}
