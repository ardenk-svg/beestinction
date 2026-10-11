package com.michiganecodata.beestinction.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TilePos;
import com.michiganecodata.beestinction.sim.world.TileType;

/**
 * OVERVIEW:      Placeholder map renderer: terrain tiles as colored squares, flower patches as colored circles
 *                (bigger for bigger patches), the hive as a honey-colored hexagon, fog as a translucent dark
 *                overlay on unrevealed tiles, and an outline around the selected tile. World units are tiles,
 *                with (0, 0) at the bottom-left. The humans will replace this with real art.
 *                DECISION: the fog is drawn translucent so the map is still visible while FogOfWar is a stub (it
 *                reports every tile as hidden). Press F in game to toggle it.
 * INVARIANTS:    Stateless apart from its color tables.
 * COLLABORATORS: ShapeRenderer, GameStateView, GameMap, FlowerPatch, GameScreen.
 */
public final class WorldRenderer {
    private static final Color[] TILE_COLORS = new Color[TileType.values().length];
    private static final Color[] PATCH_COLORS = {
            new Color(1f, 0.95f, 0.3f, 1f), new Color(1f, 0.6f, 0.75f, 1f), new Color(0.95f, 0.95f, 0.95f, 1f),
            new Color(1f, 0.55f, 0.1f, 1f), new Color(0.75f, 0.5f, 0.95f, 1f), new Color(0.95f, 0.8f, 0.1f, 1f),
            new Color(0.6f, 0.45f, 0.95f, 1f), new Color(0.4f, 0.85f, 0.95f, 1f),
    };
    private static final Color HIVE = new Color(1f, 0.78f, 0.2f, 1f);
    private static final Color HIVE_DARK = new Color(0.55f, 0.35f, 0.05f, 1f);
    private static final Color FOG = new Color(0f, 0f, 0.03f, 0.55f);
    private static final Color SELECTION = new Color(1f, 1f, 1f, 1f);

    static {
        TILE_COLORS[TileType.GRASS.ordinal()] = new Color(0.49f, 0.70f, 0.33f, 1f);
        TILE_COLORS[TileType.FOREST.ordinal()] = new Color(0.18f, 0.41f, 0.20f, 1f);
        TILE_COLORS[TileType.FARM.ordinal()] = new Color(0.80f, 0.67f, 0.41f, 1f);
        TILE_COLORS[TileType.ORCHARD.ordinal()] = new Color(0.59f, 0.67f, 0.27f, 1f);
        TILE_COLORS[TileType.WATER.ordinal()] = new Color(0.25f, 0.47f, 0.78f, 1f);
        TILE_COLORS[TileType.URBAN.ordinal()] = new Color(0.59f, 0.59f, 0.59f, 1f);
        TILE_COLORS[TileType.ROAD.ordinal()] = new Color(0.35f, 0.35f, 0.35f, 1f);
        TILE_COLORS[TileType.WETLAND.ordinal()] = new Color(0.31f, 0.59f, 0.51f, 1f);
    }

    /**
     * REQUIRES: shapes has its projection set to world (tile) units and is not drawing; view != null
     * MODIFIES: shapes
     * EFFECTS:  Draws the map. selected may be null. showFog false hides the fog overlay (debug toggle).
     */
    public void render(ShapeRenderer shapes, GameStateView view, TilePos selected, boolean showFog) {
        GameMap map = view.map();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.height(); y++) {
            for (int x = 0; x < map.width(); x++) {
                shapes.setColor(TILE_COLORS[map.tileAt(x, y).ordinal()]);
                shapes.rect(x, y, 1f, 1f);
            }
        }
        for (FlowerPatch p : map.patches()) {
            float r = Math.min(0.48f, 0.22f + 0.05f * p.size());
            shapes.setColor(Color.BLACK);
            shapes.circle(p.pos().x() + 0.5f, p.pos().y() + 0.5f, r + 0.06f, 20);
            shapes.setColor(patchColor(p));
            shapes.circle(p.pos().x() + 0.5f, p.pos().y() + 0.5f, r, 20);
        }
        TilePos hive = map.hivePos();
        shapes.setColor(HIVE_DARK);
        shapes.circle(hive.x() + 0.5f, hive.y() + 0.5f, 0.62f, 6);
        shapes.setColor(HIVE);
        shapes.circle(hive.x() + 0.5f, hive.y() + 0.5f, 0.5f, 6);
        shapes.end();

        if (showFog) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(FOG);
            for (int y = 0; y < map.height(); y++) {
                for (int x = 0; x < map.width(); x++) {
                    if (!view.isRevealed(x, y)) {
                        shapes.rect(x, y, 1f, 1f);
                    }
                }
            }
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (selected != null) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(SELECTION);
            shapes.rect(selected.x() + 0.04f, selected.y() + 0.04f, 0.92f, 0.92f);
            shapes.end();
        }
    }

    private static Color patchColor(FlowerPatch p) {
        int h = p.type().id().hashCode();
        return PATCH_COLORS[Math.floorMod(h, PATCH_COLORS.length)];
    }
}
