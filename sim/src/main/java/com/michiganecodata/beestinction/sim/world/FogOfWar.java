package com.michiganecodata.beestinction.sim.world;

/**
 * OVERVIEW:      Which tiles of the map the player has seen. Everything starts hidden except a radius around
 *                the hive (revealed when the run starts); tiles are revealed around groups as they travel.
 *                Revealed tiles stay revealed for the rest of the run.
 * INVARIANTS:    revealed.length == width * height (row-major, bottom row first: index = y * width + x).
 * COLLABORATORS: GameMap (size), ForagingSystem (reveals around groups), Simulation (starting reveal),
 *                WorldRenderer (core, draws the fog).
 */
public final class FogOfWar {
    private final int width;
    private final int height;
    private final boolean[] revealed;

    /**
     * REQUIRES: width > 0, height > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates fog covering every tile of a width x height map.
     */
    public FogOfWar(int width, int height) {
        this.width = width;
        this.height = height;
        this.revealed = new boolean[width * height];
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns true if (x, y) is inside the map and has been revealed. Tiles outside the map are
     *           never revealed.
     */
    public boolean isRevealed(int x, int y) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: center != null, radiusTiles >= 0
     * MODIFIES: this
     * EFFECTS:  Reveals every in-bounds tile whose squared distance to center is <= radiusTiles^2
     *           (a filled circle). Parts of the circle outside the map are ignored.
     */
    public void revealAround(TilePos center, int radiusTiles) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: p != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if the tile the patch is on has been revealed.
     */
    public boolean isPatchDiscovered(FlowerPatch p) {
        // TODO(human): implement per RME above.
        return false;
    }
}
