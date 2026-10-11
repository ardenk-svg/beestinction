package com.michiganecodata.beestinction.data;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TilePos;
import com.michiganecodata.beestinction.sim.world.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OVERVIEW:      Turns a Tiled .tmx map into a GameMap, following the conventions in CLAUDE.md D.6:
 *                - map properties: id, displayName, description, milliMilesPerTile (int);
 *                - tile layer "terrain": every tile has a string property "type" naming a TileType;
 *                - object layer "patches": point objects with properties "species" (a flower id) and "size" (int);
 *                  the Tiled object id becomes the patch id;
 *                - object layer "hive": exactly one point object.
 *                Positions become tile coordinates with (0, 0) at the bottom-left (libGDX's TmxMapLoader already
 *                flips Tiled's y axis). Errors name the map and the problem.
 *                Loading uses libGDX's TmxMapLoader, which also loads the tileset image, so it needs a GL context
 *                (it runs in the game, not in unit tests). The TiledMap is disposed right after conversion: the
 *                game draws terrain from the GameMap.
 * INVARIANTS:    Stateless.
 * COLLABORATORS: TmxMapLoader, GameMap, FlowerPatch, FlowerType, TileType, MapCatalog.
 */
public final class TiledMapConverter {
    private TiledMapConverter() {
    }

    /**
     * REQUIRES: internalPath names a .tmx file in assets; flowersById maps every species used by the map
     * MODIFIES: nothing
     * EFFECTS:  Loads and converts the map. Throws IllegalArgumentException if it breaks the conventions.
     */
    public static GameMap load(String internalPath, Map<String, FlowerType> flowersById) {
        TiledMap tiled = new TmxMapLoader().load(internalPath);
        try {
            return convert(tiled, flowersById, internalPath);
        } finally {
            tiled.dispose();
        }
    }

    /**
     * REQUIRES: tiled != null; flowersById maps every species used by the map
     * MODIFIES: nothing
     * EFFECTS:  Converts a loaded TiledMap into a GameMap. sourceName is used in error messages.
     */
    public static GameMap convert(TiledMap tiled, Map<String, FlowerType> flowersById, String sourceName) {
        MapProperties props = tiled.getProperties();
        String id = requireString(props, "id", sourceName);
        String displayName = requireString(props, "displayName", sourceName);
        String description = props.get("description", "", String.class);
        int milliMilesPerTile = requireInt(props, "milliMilesPerTile", sourceName);
        int width = requireInt(props, "width", sourceName);
        int height = requireInt(props, "height", sourceName);
        int tileWidth = requireInt(props, "tilewidth", sourceName);
        int tileHeight = requireInt(props, "tileheight", sourceName);

        MapLayer terrainLayer = tiled.getLayers().get("terrain");
        if (!(terrainLayer instanceof TiledMapTileLayer)) {
            throw new IllegalArgumentException(sourceName + ": needs a tile layer named \"terrain\"");
        }
        TiledMapTileLayer terrain = (TiledMapTileLayer) terrainLayer;
        TileType[] tiles = new TileType[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = terrain.getCell(x, y);
                TiledMapTile tile = cell == null ? null : cell.getTile();
                if (tile == null) {
                    throw new IllegalArgumentException(sourceName + ": no terrain tile at (" + x + ", " + y + ")");
                }
                String type = tile.getProperties().get("type", String.class);
                tiles[y * width + x] = tileType(type, sourceName, x, y);
            }
        }

        MapObjects hiveObjects = requireObjectLayer(tiled, "hive", sourceName).getObjects();
        if (hiveObjects.getCount() != 1) {
            throw new IllegalArgumentException(sourceName + ": the \"hive\" layer must have exactly one object, has "
                    + hiveObjects.getCount());
        }
        TilePos hive = tileOf(hiveObjects.get(0), tileWidth, tileHeight, width, height);

        List<FlowerPatch> patches = new ArrayList<>();
        MapObjects patchObjects = requireObjectLayer(tiled, "patches", sourceName).getObjects();
        for (int i = 0; i < patchObjects.getCount(); i++) {
            MapObject obj = patchObjects.get(i);
            MapProperties p = obj.getProperties();
            Integer patchId = p.get("id", Integer.class);
            String species = p.get("species", String.class);
            Integer size = p.get("size", Integer.class);
            String where = sourceName + ": patch object " + patchId;
            if (patchId == null) {
                throw new IllegalArgumentException(sourceName + ": a patch object has no id");
            }
            if (species == null || !flowersById.containsKey(species)) {
                throw new IllegalArgumentException(where + " has unknown species \"" + species + "\"");
            }
            if (size == null || size <= 0) {
                throw new IllegalArgumentException(where + " needs a positive int property \"size\"");
            }
            patches.add(new FlowerPatch(patchId, flowersById.get(species),
                    tileOf(obj, tileWidth, tileHeight, width, height), size));
        }
        return new GameMap(id, displayName, description, width, height, tiles, hive, patches, milliMilesPerTile);
    }

    private static TilePos tileOf(MapObject obj, int tileWidth, int tileHeight, int width, int height) {
        Float x = obj.getProperties().get("x", Float.class);
        Float y = obj.getProperties().get("y", Float.class);
        int tx = (int) Math.floor((x == null ? 0f : x) / tileWidth);
        int ty = (int) Math.floor((y == null ? 0f : y) / tileHeight);
        // A point exactly on the top or right edge belongs to the last tile.
        tx = Math.max(0, Math.min(width - 1, tx));
        ty = Math.max(0, Math.min(height - 1, ty));
        return new TilePos(tx, ty);
    }

    private static MapLayer requireObjectLayer(TiledMap tiled, String name, String sourceName) {
        MapLayer layer = tiled.getLayers().get(name);
        if (layer == null) {
            throw new IllegalArgumentException(sourceName + ": needs an object layer named \"" + name + "\"");
        }
        return layer;
    }

    private static TileType tileType(String name, String sourceName, int x, int y) {
        for (TileType t : TileType.values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException(sourceName + ": tile (" + x + ", " + y + ") has unknown type \"" + name + "\"");
    }

    private static String requireString(MapProperties props, String key, String sourceName) {
        Object v = props.get(key);
        if (!(v instanceof String) || ((String) v).isEmpty()) {
            throw new IllegalArgumentException(sourceName + ": missing map property \"" + key + "\"");
        }
        return (String) v;
    }

    private static int requireInt(MapProperties props, String key, String sourceName) {
        Object v = props.get(key);
        if (v instanceof Integer) {
            return (Integer) v;
        }
        throw new IllegalArgumentException(sourceName + ": map property \"" + key + "\" must be an int");
    }
}
