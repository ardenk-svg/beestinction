package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.foraging.GroupState;
import com.michiganecodata.beestinction.sim.orders.CancelStandingOrder;
import com.michiganecodata.beestinction.sim.orders.RecallOrder;

/**
 * OVERVIEW:      Placeholder list of groups away from the hive, each with Recall and (for standing orders) Cancel
 *                buttons that submit orders through Simulation (CLAUDE.md B.12).
 * INVARIANTS:    The rows match the groups seen at the last refresh() (rebuilt only when they change).
 * COLLABORATORS: Simulation, RecallOrder, CancelStandingOrder, ForagerGroup, GameStateView.
 */
public final class GroupListPanel {
    private final Table table = new Table();
    private final Table rows = new Table();
    private final Skin skin;
    private final Simulation sim;
    private long signature = Long.MIN_VALUE;

    public GroupListPanel(Skin skin, Simulation sim) {
        this.skin = skin;
        this.sim = sim;
        rows.top().left();
        ScrollPane scroll = new ScrollPane(rows, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        table.add(new Label("Groups out", skin, "title")).left().row();
        table.add(scroll).width(270).height(130).left();
    }

    public Actor actor() {
        return table;
    }

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Rebuilds the rows if any group appeared, disappeared, or changed state, size, or flags.
     */
    public void refresh(GameStateView v) {
        long sig = 17;
        for (ForagerGroup g : v.groups()) {
            sig = sig * 31 + g.id();
            sig = sig * 31 + g.state().ordinal();
            sig = sig * 31 + g.bees().total();
            sig = sig * 31 + (g.isStanding() ? 1 : 0) + (g.isCancelRequested() ? 2 : 0);
        }
        sig = sig * 31 + (v.isGameOver() ? 1 : 0);
        if (sig == signature) {
            return;
        }
        signature = sig;
        rows.clearChildren();
        if (v.groups().isEmpty()) {
            rows.add(new Label("No bees are out.", skin, "small")).left();
            return;
        }
        for (ForagerGroup g : v.groups()) {
            String text = "#" + g.id() + "  " + g.bees().total() + " bees  " + g.state().name().toLowerCase()
                    + (g.isStanding() ? (g.isCancelRequested() ? "  (stopping)" : "  (standing)") : "");
            rows.add(new Label(text, skin, "small")).left().expandX();
            final int id = g.id();
            TextButton recall = UiFields.button(skin, "Recall", () -> sim.submit(new RecallOrder(id)));
            recall.setDisabled(v.isGameOver() || g.state() == GroupState.RETURNING);
            rows.add(recall).padLeft(4);
            if (g.isStanding() && !g.isCancelRequested()) {
                rows.add(UiFields.button(skin, "Cancel", () -> sim.submit(new CancelStandingOrder(id)))).padLeft(4);
            } else {
                rows.add();
            }
            rows.row();
        }
    }
}
