package com.michiganecodata.beestinction.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.world.TilePos;

/**
 * OVERVIEW:      Placeholder renderer for forager groups: a dot per group, drawn between its previous and current
 *                tick positions using the TickDriver's interpolation alpha, so movement looks smooth at any frame
 *                rate. Dot size grows with the number of bees; color shows the group's state.
 * INVARIANTS:    Stateless apart from its colors.
 * COLLABORATORS: ShapeRenderer, GameStateView, ForagerGroup, TickDriver (alpha), GameScreen.
 */
public final class GroupRenderer {
    private static final Color OUTBOUND = new Color(1f, 0.9f, 0.2f, 1f);
    private static final Color FORAGING = new Color(1f, 0.55f, 0.1f, 1f);
    private static final Color RETURNING = new Color(1f, 1f, 0.6f, 1f);
    private static final float MILLI = TilePos.MILLI_PER_TILE;

    /**
     * REQUIRES: shapes has its projection set to world (tile) units and is not drawing; 0 <= alpha < 1
     * MODIFIES: shapes
     * EFFECTS:  Draws every group at prev + (current - prev) * alpha.
     */
    public void render(ShapeRenderer shapes, GameStateView view, float alpha) {
        if (view.groups().isEmpty()) {
            return;
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (ForagerGroup g : view.groups()) {
            float x = (g.prevXMilli() + (g.posXMilli() - g.prevXMilli()) * alpha) / MILLI;
            float y = (g.prevYMilli() + (g.posYMilli() - g.prevYMilli()) * alpha) / MILLI;
            float r = 0.14f + 0.03f * Math.min(10f, (float) Math.sqrt(Math.max(0, g.bees().total())));
            shapes.setColor(Color.BLACK);
            shapes.circle(x, y, r + 0.05f, 12);
            switch (g.state()) {
                case OUTBOUND:
                    shapes.setColor(OUTBOUND);
                    break;
                case FORAGING:
                    shapes.setColor(FORAGING);
                    break;
                default:
                    shapes.setColor(RETURNING);
                    break;
            }
            shapes.circle(x, y, r, 12);
        }
        shapes.end();
    }
}
