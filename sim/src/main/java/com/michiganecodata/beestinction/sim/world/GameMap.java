package com.michiganecodata.beestinction.sim.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      A map: a grid of terrain tiles, the hive's tile, and the flower patches. Built by
 *                TiledMapConverter (core) from a .tmx file, or in code by TestFixtures.
 *                The terrain and layout never change. The FlowerPatch objects DO hold per-run state (today's
 *                nectar, toxicity), so every run must use its own copy: GameState calls freshCopy().
 * INVARIANTS:    width, height > 0; tiles.length == width * height; every tile is non-null; hivePos and every
 *                patch position are inside the map; patch ids are unique; milliMilesPerTile > 0.
 * COLLABORATORS: TileType, TilePos, FlowerPatch, GameState, TiledMapConverter (core), WorldRenderer (core).
 */
public final class GameMap {
    private final String id;
    private final String displayName;
    private final String description;
    private final int width;
    private final int height;
    /** Row-major, bottom row first: index = y * width + x. */
    private final TileType[] tiles;
    private final TilePos hivePos;
    private final List<FlowerPatch> patches;
    private final int milliMilesPerTile;

    /**
     * REQUIRES: the GameMap invariants hold for the arguments (tiles is row-major, bottom row first)
     * MODIFIES: nothing
     * EFFECTS:  Creates a map. tiles and patches are copied. Throws IllegalArgumentException if an invariant
     *           is broken, with a message naming the problem.
     */
    public GameMap(String id, String displayName, String description, int width, int height, TileType[] tiles,
                   TilePos hivePos, List<FlowerPatch> patches, int milliMilesPerTile) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = description == null ? "" : description;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("map size must be positive: " + width + "x" + height);
        }
        if (tiles == null || tiles.length != width * height) {
            throw new IllegalArgumentException("tiles must have width*height entries");
        }
        for (TileType t : tiles) {
            if (t == null) {
                throw new IllegalArgumentException("map " + id + " has a tile with no type");
            }
        }
        if (milliMilesPerTile <= 0) {
            throw new IllegalArgumentException("milliMilesPerTile must be positive");
        }
        this.width = width;
        this.height = height;
        this.tiles = tiles.clone();
        this.hivePos = Objects.requireNonNull(hivePos, "hivePos");
        if (!inBounds(hivePos.x(), hivePos.y())) {
            throw new IllegalArgumentException("hive is outside map " + id + ": " + hivePos);
        }
        List<FlowerPatch> copy = new ArrayList<>(patches);
        for (int i = 0; i < copy.size(); i++) {
            FlowerPatch p = copy.get(i);
            if (!inBounds(p.pos().x(), p.pos().y())) {
                throw new IllegalArgumentException("patch " + p.id() + " is outside map " + id);
            }
            for (int j = 0; j < i; j++) {
                if (copy.get(j).id() == p.id()) {
                    throw new IllegalArgumentException("duplicate patch id " + p.id() + " in map " + id);
                }
            }
        }
        this.patches = Collections.unmodifiableList(copy);
        this.milliMilesPerTile = milliMilesPerTile;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a new GameMap with the same terrain and layout and NEW FlowerPatch objects
     *           (copies of this map's patches, with their per-run state reset). Each run uses its own copy so
     *           one run's patch state never leaks into another.
     */
    public GameMap freshCopy() {
        List<FlowerPatch> copies = new ArrayList<>(patches.size());
        for (FlowerPatch p : patches) {
            copies.add(new FlowerPatch(p.id(), p.type(), p.pos(), p.size()));
        }
        return new GameMap(id, displayName, description, width, height, tiles, hivePos, copies, milliMilesPerTile);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Returns true if (x, y) is a tile of this map. */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * REQUIRES: inBounds(x, y)
     * MODIFIES: nothing
     * EFFECTS:  Returns the terrain at (x, y). Throws IndexOutOfBoundsException if (x, y) is outside the map.
     */
    public TileType tileAt(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException("tile outside map: (" + x + ", " + y + ")");
        }
        return tiles[y * width + x];
    }

    public TilePos hivePos() {
        return hivePos;
    }

    /** Returns the patches, in a fixed order (unmodifiable list). */
    public List<FlowerPatch> patches() {
        return patches;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the patch with the given id, or null if there is none.
     */
    public FlowerPatch patchById(int patchId) {
        for (FlowerPatch p : patches) {
            if (p.id() == patchId) {
                return p;
            }
        }
        return null;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the first patch located on tile pos, or null if there is none.
     */
    public FlowerPatch patchAt(TilePos pos) {
        for (FlowerPatch p : patches) {
            if (p.pos().equals(pos)) {
                return p;
            }
        }
        return null;
    }

    /** Map scale: thousandths of a mile per tile (200 means each tile is 0.2 miles across). */
    public int milliMilesPerTile() {
        return milliMilesPerTile;
    }

    @Override
    public String toString() {
        return "GameMap[" + id + ", " + width + "x" + height + ", " + patches.size() + " patches]";
    }
}
