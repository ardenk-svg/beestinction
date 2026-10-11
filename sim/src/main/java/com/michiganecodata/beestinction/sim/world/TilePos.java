package com.michiganecodata.beestinction.sim.world;

/**
 * OVERVIEW:      An immutable tile coordinate. (0, 0) is the bottom-left tile and y grows upward, matching libGDX
 *                (TiledMapConverter converts from Tiled's top-left origin).
 * INVARIANTS:    Immutable. Value semantics: equal x and y means equal positions.
 * COLLABORATORS: GameMap, FlowerPatch, ForagerGroup, FogOfWar, orders.
 */
public final class TilePos {
    /** Fixed-point scale for sub-tile positions: 1 tile = 1000 milli-tiles. */
    public static final int MILLI_PER_TILE = 1000;

    private final int x;
    private final int y;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates the position (x, y).
     */
    public TilePos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * REQUIRES: o != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the squared straight-line distance to o, in tiles squared (exact, no overflow for any
     *           int coordinates).
     */
    public long distanceSquared(TilePos o) {
        long dx = (long) x - o.x;
        long dy = (long) y - o.y;
        return dx * dx + dy * dy;
    }

    /** Returns the x coordinate of this tile's center in milli-tiles. */
    public int centerXMilli() {
        return x * MILLI_PER_TILE + MILLI_PER_TILE / 2;
    }

    /** Returns the y coordinate of this tile's center in milli-tiles. */
    public int centerYMilli() {
        return y * MILLI_PER_TILE + MILLI_PER_TILE / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TilePos)) {
            return false;
        }
        TilePos p = (TilePos) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
