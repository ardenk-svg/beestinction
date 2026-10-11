package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.BuildCombOrder;

/**
 * OVERVIEW:      Placeholder "build comb" control: shows the comb size and submits a BuildCombOrder with the chosen
 *                number of house bees as builders. Expansion is expensive in nectar (CLAUDE.md B.5).
 * INVARIANTS:    Shows the values from the last refresh().
 * COLLABORATORS: Simulation, BuildCombOrder, GameStateView.
 */
public final class CombControl {
    private final Table table = new Table();
    private final Label label;
    private final TextField bees;
    private int size = -1;
    private int expanding = -1;

    public CombControl(Skin skin, Simulation sim) {
        label = new Label("", skin);
        bees = UiFields.numberField(skin, 10);
        table.add(label).colspan(3).left().row();
        table.add(new Label("Builders", skin, "small")).left().padRight(6);
        table.add(bees).width(70).left();
        table.add(UiFields.button(skin, "Build comb", () -> {
            int n = UiFields.parse(bees);
            if (n > 0) {
                sim.submit(new BuildCombOrder(n));
            }
        })).padLeft(6).left().expandX();
    }

    public Actor actor() {
        return table;
    }

    public void refresh(GameStateView v) {
        int exp = v.isCombExpanding() ? 1 : 0;
        if (v.combSize() != size || exp != expanding) {
            size = v.combSize();
            expanding = exp;
            label.setText("Comb size " + size + (exp == 1 ? " (expanding...)" : "") + "  - costs "
                    + v.balance().combExpansionNectarCost() + " nectar");
        }
    }
}
