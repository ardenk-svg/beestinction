package com.michiganecodata.beestinction.sim.world;

/**
 * OVERVIEW:      Terrain types (a placeholder set). In a Tiled map, each terrain tile has a string property
 *                `type` whose value is one of these names.
 * INVARIANTS:    none
 * COLLABORATORS: GameMap, TiledMapConverter (core), WorldRenderer (core).
 */
public enum TileType {
    GRASS, FOREST, FARM, ORCHARD, WATER, URBAN, ROAD, WETLAND
}
