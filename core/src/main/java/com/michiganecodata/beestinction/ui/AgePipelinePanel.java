package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.michiganecodata.beestinction.sim.GameStateView;

/**
 * OVERVIEW:      Placeholder HUD row showing the colony's age pipeline: brood -> house bees -> available
 *                foragers -> bees out (CLAUDE.md B.12). Bound to GameStateView; the humans will design the real one.
 * INVARIANTS:    Shows the values from the last refresh().
 * COLLABORATORS: Hud, GameStateView.
 */
public final class AgePipelinePanel {
    private final Table table = new Table();
    private final Label label;
    private int brood = -1;
    private int house = -1;
    private int foragers = -1;
    private int out = -1;

    public AgePipelinePanel(Skin skin) {
        label = new Label("", skin, "small");
        table.add(label).left();
    }

    public Actor actor() {
        return table;
    }

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Updates the numbers (only rebuilding the text when they change).
     */
    public void refresh(GameStateView v) {
        if (v.broodCount() == brood && v.houseBees() == house && v.availableForagers() == foragers
                && v.beesOut() == out) {
            return;
        }
        brood = v.broodCount();
        house = v.houseBees();
        foragers = v.availableForagers();
        out = v.beesOut();
        label.setText("Brood " + brood + "  >  House bees " + house + "  >  Foragers " + foragers
                + "  >  Out " + out);
    }
}
