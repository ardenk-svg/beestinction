package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.world.GameMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      Everything needed to start a run: the seed, map, difficulty, balance values, flower species, and
 *                game version. The same setup and the same orders always produce the same game.
 *                DECISION: the setup also carries runIdSeed, a second seed used only to generate the GameLog's
 *                random run ID, so the run ID never reveals (or equals) the game seed. The game picks both seeds
 *                at random; tests use fixed values.
 * INVARIANTS:    Immutable. map, difficulty, balance, flowerTypes, gameVersion are non-null.
 * COLLABORATORS: Simulation, GameState, GameLog, BalanceConfig, GameMap, FlowerType, MapSelectScreen (core),
 *                TestFixtures.
 */
public final class GameSetup {
    private final long seed;
    private final GameMap map;
    private final DifficultyLevel difficulty;
    private final BalanceConfig balance;
    private final List<FlowerType> flowerTypes;
    private final String gameVersion;
    private final long runIdSeed;

    /**
     * REQUIRES: map, difficulty, balance, flowerTypes, gameVersion != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a setup (flowerTypes is copied).
     */
    public GameSetup(long seed, GameMap map, DifficultyLevel difficulty, BalanceConfig balance,
                     List<FlowerType> flowerTypes, String gameVersion, long runIdSeed) {
        this.seed = seed;
        this.map = Objects.requireNonNull(map, "map");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.balance = Objects.requireNonNull(balance, "balance");
        this.flowerTypes = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(flowerTypes)));
        this.gameVersion = Objects.requireNonNull(gameVersion, "gameVersion");
        this.runIdSeed = runIdSeed;
    }

    public long seed() {
        return seed;
    }

    /** The map as loaded. Each run works on map().freshCopy(), so this object is never changed by a run. */
    public GameMap map() {
        return map;
    }

    public DifficultyLevel difficulty() {
        return difficulty;
    }

    public BalanceConfig balance() {
        return balance;
    }

    public List<FlowerType> flowerTypes() {
        return flowerTypes;
    }

    public String gameVersion() {
        return gameVersion;
    }

    /** Seed for the log's run ID only. Never used by the game rules. */
    public long runIdSeed() {
        return runIdSeed;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a copy of this setup with a different game seed.
     */
    public GameSetup withSeed(long newSeed) {
        return new GameSetup(newSeed, map, difficulty, balance, flowerTypes, gameVersion, runIdSeed);
    }
}
