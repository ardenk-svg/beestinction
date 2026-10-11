package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.ForageOrder;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.TilePos;

/**
 * OVERVIEW:      Placeholder order panel (CLAUDE.md B.12): shows the selected tile or patch, a bee count, and
 *                Send (one trip) / Standing (repeat trips) buttons that submit ForageOrders through Simulation.
 *                Whether an order is valid is decided by the simulation at its tick, not here.
 * INVARIANTS:    selected is null until the player clicks the map.
 * COLLABORATORS: Simulation (submit), ForageOrder, GameScreen (sets the selection), GameStateView.
 */
public final class OrderPanel {
    private final Table table = new Table();
    private final Simulation sim;
    private final Label selection;
    private final Label status;
    private final TextField count;
    private final TextButton send;
    private final TextButton standing;
    private TilePos selected;
    private FlowerPatch selectedPatch;

    /**
     * REQUIRES: skin, sim != null
     * MODIFIES: nothing
     * EFFECTS:  Builds the panel with nothing selected.
     */
    public OrderPanel(Skin skin, Simulation sim) {
        this.sim = sim;
        selection = new Label("Click a patch or tile on the map.", skin, "small");
        selection.setWrap(true);
        status = new Label("", skin, "small");
        status.setWrap(true);
        count = UiFields.numberField(skin, 10);
        send = UiFields.button(skin, "Send", () -> submit(false));
        standing = UiFields.button(skin, "Standing", () -> submit(true));

        table.add(new Label("Send foragers", skin, "title")).colspan(3).left().row();
        table.add(selection).colspan(3).width(260).left().padBottom(4).row();
        table.add(new Label("Bees", skin)).left().padRight(6);
        table.add(count).width(70).left();
        table.add().expandX().row();
        Table buttons = new Table();
        buttons.add(send).width(110).padRight(6);
        buttons.add(standing).width(110);
        table.add(buttons).colspan(3).left().padTop(4).row();
        table.add(status).colspan(3).width(260).left().padTop(2);
        refreshButtons();
    }

    public Actor actor() {
        return table;
    }

    /**
     * REQUIRES: tile is inside the map or null; patch is the patch on that tile or null
     * MODIFIES: this
     * EFFECTS:  Changes the selected target.
     */
    public void setSelection(TilePos tile, FlowerPatch patch, boolean discovered) {
        selected = tile;
        selectedPatch = patch;
        if (tile == null) {
            selection.setText("Click a patch or tile on the map.");
        } else if (patch == null) {
            selection.setText("Tile " + tile + (discovered ? " (no flowers)" : " (unexplored: scouting trip)"));
        } else {
            selection.setText(patch.type().displayName() + " patch, size " + patch.size() + ", at " + tile);
        }
        refreshButtons();
    }

    public TilePos selected() {
        return selected;
    }

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Enables or disables the buttons for the current state.
     */
    public void refresh(GameStateView v) {
        boolean over = v.isGameOver();
        send.setDisabled(over || selected == null);
        standing.setDisabled(over || selectedPatch == null);
    }

    private void refreshButtons() {
        send.setDisabled(selected == null);
        standing.setDisabled(selectedPatch == null);
    }

    private void submit(boolean isStanding) {
        if (selected == null || (isStanding && selectedPatch == null)) {
            return;
        }
        int bees = UiFields.parse(count);
        if (bees <= 0) {
            status.setText("Enter a number of bees.");
            return;
        }
        ForageOrder order = new ForageOrder(bees, selected, isStanding);
        sim.submit(order);
        status.setText((isStanding ? "Standing order" : "Order") + " for " + bees + " bees submitted (tick "
                + order.tick() + ").");
    }
}
