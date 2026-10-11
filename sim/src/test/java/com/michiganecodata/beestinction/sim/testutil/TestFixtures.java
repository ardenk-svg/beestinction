package com.michiganecodata.beestinction.sim.testutil;

import com.michiganecodata.beestinction.sim.GameClock;
import com.michiganecodata.beestinction.sim.GameRules;
import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.colony.AgeHistogram;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.config.DifficultyProfile;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.config.NectarPotential;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TilePos;
import com.michiganecodata.beestinction.sim.world.TileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * OVERVIEW:      Shared test data and helpers: balance configs, flower species, a tiny map built in code (hive
 *                plus patches at known distances), the pure-Java copy of test_map, fixed seeds, and shortcuts for
 *                building Simulations and GameStates and running the rules without the UI.
 * INVARIANTS:    Everything returned is freshly built (tests cannot affect each other).
 * COLLABORATORS: Most sim tests; TestMapCopy; RandomPlayer; InvariantChecker.
 */
public final class TestFixtures {
    public static final String VERSION = "test";

    public static final long SEED = 12345L;
    public static final long OTHER_SEED = 67890L;
    public static final long RUN_ID_SEED = 424242L;
    /** A few fixed seeds for tests that loop over seeds. */
    public static final long[] SEEDS = {1L, 2L, 3L, 42L, 12345L, -7L, Long.MAX_VALUE, Long.MIN_VALUE};

    // ----- The tiny map: 21 x 21 grass, hive in the middle, patches at known distances -----
    public static final String TINY_MAP_ID = "tiny";
    public static final int TINY_SIZE = 21;
    public static final TilePos TINY_HIVE = new TilePos(10, 10);
    /** Clover (spring + summer), size 4, 2 tiles east of the hive. */
    public static final int NEAR_PATCH_ID = 1;
    public static final TilePos NEAR_POS = new TilePos(12, 10);
    /** Dandelion (spring), size 4, 6 tiles north of the hive. */
    public static final int MID_PATCH_ID = 2;
    public static final TilePos MID_POS = new TilePos(10, 16);
    /** Pumpkin (summer), size 4, 10 tiles east of the hive (at the map edge). */
    public static final int FAR_PATCH_ID = 3;
    public static final TilePos FAR_POS = new TilePos(20, 10);
    /** Goldenrod (fall), size 3, 6 tiles west of the hive. */
    public static final int FALL_PATCH_ID = 4;
    public static final TilePos FALL_POS = new TilePos(4, 10);
    /** Apple blossom (spring), size 50, 6 tiles south of the hive: a patch with plenty of capacity. */
    public static final int BIG_PATCH_ID = 5;
    public static final TilePos BIG_POS = new TilePos(10, 4);
    /** An empty grass tile with no patch (for scouting). */
    public static final TilePos EMPTY_POS = new TilePos(15, 15);

    /** The first day of each season (0-based day of run) with the default config. */
    public static final int SPRING_DAY = 0;
    public static final int SUMMER_DAY = 28;
    public static final int FALL_DAY = 56;
    public static final int WINTER_DAY = 84;

    private TestFixtures() {
    }

    // ----- Configs -----

    /** The D.7 placeholder config. */
    public static BalanceConfig balance() {
        return BalanceConfig.defaults();
    }

    /**
     * A config where bees fly 170 milli-tiles per tick, so a round trip to the MID patch (6 tiles) takes about
     * two days (2 x 36 = 72 ticks). Used by travel-time tests.
     */
    public static BalanceConfig travelBalance() {
        return BalanceConfig.defaultsBuilder().set(BalanceConfig.Key.MILLI_TILES_PER_TICK, 170).build();
    }

    /** A config where every random event has a 100% daily chance once allowed (for scheduler tests). */
    public static BalanceConfig alwaysEventsBalance() {
        return BalanceConfig.defaultsBuilder()
                .difficulty(DifficultyLevel.EASY, new DifficultyProfile(100, 5, 110, 10_000))
                .difficulty(DifficultyLevel.MEDIUM, new DifficultyProfile(150, 8, 100, 10_000))
                .difficulty(DifficultyLevel.HARD, new DifficultyProfile(200, 12, 85, 10_000))
                .build();
    }

    // ----- Flowers -----

    /** The placeholder species from assets/data/flowers.json. */
    public static List<FlowerType> flowers() {
        List<FlowerType> list = new ArrayList<>();
        list.add(new FlowerType("dandelion", "Dandelion", EnumSet.of(Season.SPRING), 8, NectarPotential.LOW, null));
        list.add(new FlowerType("apple_blossom", "Apple Blossom", EnumSet.of(Season.SPRING), 12,
                NectarPotential.HIGH, null));
        list.add(new FlowerType("clover", "White Clover", EnumSet.of(Season.SPRING, Season.SUMMER), 10,
                NectarPotential.MEDIUM, null));
        list.add(new FlowerType("pumpkin", "Pumpkin", EnumSet.of(Season.SUMMER), 14, NectarPotential.HIGH, null));
        list.add(new FlowerType("wild_bergamot", "Wild Bergamot", EnumSet.of(Season.SUMMER), 10,
                NectarPotential.MEDIUM, null));
        list.add(new FlowerType("goldenrod", "Goldenrod", EnumSet.of(Season.FALL), 12, NectarPotential.HIGH, null));
        list.add(new FlowerType("new_england_aster", "New England Aster", EnumSet.of(Season.FALL), 10,
                NectarPotential.MEDIUM, null));
        return list;
    }

    /** The placeholder species with this id. Throws IllegalArgumentException if there is none. */
    public static FlowerType flower(String id) {
        for (FlowerType t : flowers()) {
            if (t.id().equals(id)) {
                return t;
            }
        }
        throw new IllegalArgumentException("no test flower " + id);
    }

    // ----- Maps -----

    /** A 21 x 21 all-grass map with the hive at (10, 10) and the five patches described above. */
    public static GameMap tinyMap() {
        TileType[] tiles = new TileType[TINY_SIZE * TINY_SIZE];
        Arrays.fill(tiles, TileType.GRASS);
        List<FlowerPatch> patches = new ArrayList<>();
        patches.add(new FlowerPatch(NEAR_PATCH_ID, flower("clover"), NEAR_POS, 4));
        patches.add(new FlowerPatch(MID_PATCH_ID, flower("dandelion"), MID_POS, 4));
        patches.add(new FlowerPatch(FAR_PATCH_ID, flower("pumpkin"), FAR_POS, 4));
        patches.add(new FlowerPatch(FALL_PATCH_ID, flower("goldenrod"), FALL_POS, 3));
        patches.add(new FlowerPatch(BIG_PATCH_ID, flower("apple_blossom"), BIG_POS, 50));
        return new GameMap(TINY_MAP_ID, "Tiny", "A tiny test map", TINY_SIZE, TINY_SIZE, tiles, TINY_HIVE, patches,
                200);
    }

    /** The pure-Java copy of assets/maps/test_map.tmx (see TestMapCopy). */
    public static GameMap testMapCopy() {
        int w = TestMapCopy.WIDTH;
        int h = TestMapCopy.HEIGHT;
        TileType[] tiles = new TileType[w * h];
        for (int row = 0; row < h; row++) {
            int y = h - 1 - row;
            String line = TestMapCopy.ROWS_TOP_DOWN[row];
            for (int x = 0; x < w; x++) {
                tiles[y * w + x] = tileForLetter(line.charAt(x));
            }
        }
        List<FlowerPatch> patches = new ArrayList<>();
        for (TestMapCopy.PatchSpec p : TestMapCopy.PATCHES) {
            patches.add(new FlowerPatch(p.id, flower(p.species), new TilePos(p.x, p.y), p.size));
        }
        return new GameMap(TestMapCopy.ID, TestMapCopy.DISPLAY_NAME, TestMapCopy.DESCRIPTION, w, h, tiles,
                new TilePos(TestMapCopy.HIVE_X, TestMapCopy.HIVE_Y), patches, TestMapCopy.MILLI_MILES_PER_TILE);
    }

    /** Maps a TestMapCopy letter to its TileType. */
    public static TileType tileForLetter(char c) {
        switch (c) {
            case 'G': return TileType.GRASS;
            case 'F': return TileType.FOREST;
            case 'A': return TileType.FARM;
            case 'O': return TileType.ORCHARD;
            case 'W': return TileType.WATER;
            case 'U': return TileType.URBAN;
            case 'R': return TileType.ROAD;
            case 'M': return TileType.WETLAND;
            default: throw new IllegalArgumentException("unknown tile letter " + c);
        }
    }

    // ----- Setups, simulations, states -----

    /** Tiny map, MEDIUM difficulty, default config. */
    public static GameSetup setup(long seed) {
        return setup(seed, tinyMap(), DifficultyLevel.MEDIUM, balance());
    }

    public static GameSetup setup(long seed, GameMap map, DifficultyLevel level, BalanceConfig cfg) {
        return new GameSetup(seed, map, level, cfg, flowers(), VERSION, RUN_ID_SEED);
    }

    /** A new Simulation on the tiny map (MEDIUM, default config). */
    public static Simulation newSimulation(long seed) {
        return Simulation.create(setup(seed));
    }

    /**
     * A bare GameState on the tiny map with an EMPTY hive (initializeColony has not run), at tick 0.
     * Rule tests use this to set up exact situations.
     */
    public static GameState newState(long seed) {
        return new GameState(setup(seed));
    }

    public static GameState newState(GameSetup setup) {
        return new GameState(setup);
    }

    /** Advances only the clock (no rules run) until it reaches `tick`. */
    public static void advanceClockTo(GameState s, long tick) {
        GameClock clock = s.clock();
        while (clock.tick() < tick) {
            clock.advance();
        }
    }

    /** Advances only the clock (no rules run) to the first tick of the given 0-based day. */
    public static void advanceToDay(GameState s, int day) {
        advanceClockTo(s, (long) day * s.balance().ticksPerDay());
    }

    /** Adds n workers of the given age to the hive. */
    public static void addWorkers(GameState s, int age, int n) {
        s.hive().workers().addAtAge(age, n);
    }

    /** Sets the nectar store's capacity and fills it with `amount` nectar (amount <= capacity). */
    public static void setNectar(GameState s, int capacity, int amount) {
        s.hive().nectar().setCapacity(capacity);
        s.hive().nectar().withdraw(s.hive().nectar().amount());
        s.hive().nectar().deposit(amount);
    }

    /**
     * Adds a group of `bees` bees aged `age` to the state, positioned at the hive and heading for `target`
     * (built directly, without ForagingSystem.dispatch). Returns it.
     */
    public static ForagerGroup addGroup(GameState s, int bees, int age, TilePos target, boolean standing) {
        AgeHistogram h = new AgeHistogram(s.balance().lifespanDays());
        h.addAtAge(age, bees);
        ForagerGroup g = new ForagerGroup(s.nextGroupId(), h, target, s.map().patchAt(target), standing,
                s.map().hivePos());
        s.addGroup(g);
        return g;
    }

    /** The run's copy of the patch with this id. */
    public static FlowerPatch patch(GameState s, int patchId) {
        FlowerPatch p = s.map().patchById(patchId);
        if (p == null) {
            throw new IllegalArgumentException("no patch " + patchId);
        }
        return p;
    }

    /**
     * Runs the rule systems for `ticks` ticks in the same order as Simulation.tick (CLAUDE.md D.5), but with no
     * random events, no orders, and no listeners. Returns every notification posted, in order (the outbox is
     * emptied each tick). Stops early if the game ends.
     */
    public static List<Notification> runRules(GameState s, int ticks) {
        GameRules rules = s.rules();
        List<Notification> posted = new ArrayList<>();
        for (int i = 0; i < ticks && !s.isGameOver(); i++) {
            GameClock clock = s.clock();
            if (clock.isDayStart()) {
                if (clock.isSeasonStart()) {
                    rules.winter().onSeasonStart(s, clock.season());
                }
                rules.colony().onDayStart(s);
                rules.foraging().onDayStart(s);
                rules.winter().onDayStart(s);
                s.scoreKeeper().onDayStart(s);
                s.stats().recordDay(s);
            }
            rules.colony().onTick(s);
            for (ForagerGroup g : s.groups()) {
                g.snapshotPosition();
            }
            rules.foraging().onTick(s);
            rules.winter().onTick(s);
            s.scoreKeeper().onTick(s);
            posted.addAll(s.drainOutbox());
            clock.advance();
        }
        return posted;
    }
}
