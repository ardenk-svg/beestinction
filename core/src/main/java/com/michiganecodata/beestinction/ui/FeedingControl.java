package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.SetFeedingLevelOrder;

/**
 * OVERVIEW:      Placeholder feeding-level control: shows the level and submits SetFeedingLevelOrders for one level
 *                lower or higher (CLAUDE.md B.7, B.12). Extra feeding raises the queen's egg laying.
 * INVARIANTS:    Shows the level from the last refresh().
 * COLLABORATORS: Simulation, SetFeedingLevelOrder, GameStateView.
 */
public final class FeedingControl {
    private final Table table = new Table();
    private final Label label;
    private int level = -1;
    private int max;

    public FeedingControl(Skin skin, Simulation sim) {
        label = new Label("", skin);
        table.add(label).left().expandX();
        table.add(UiFields.button(skin, "-", () -> sim.submit(new SetFeedingLevelOrder(Math.max(0, level - 1)))))
                .width(36).padLeft(4);
        table.add(UiFields.button(skin, "+", () -> sim.submit(new SetFeedingLevelOrder(Math.min(max, level + 1)))))
                .width(36).padLeft(4);
    }

    public Actor actor() {
        return table;
    }

    public void refresh(GameStateView v) {
        max = v.balance().maxFeedingLevel();
        if (v.feedingLevel() != level) {
            level = v.feedingLevel();
            label.setText("Feeding level " + level + " / " + max);
        }
    }
}
