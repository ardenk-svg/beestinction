package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.ConvertBeesOrder;

/**
 * OVERVIEW:      Placeholder "convert bees" control, visible only during the conversion window (mid-fall until
 *                winter, CLAUDE.md B.9). Submits a ConvertBeesOrder.
 * INVARIANTS:    Visible exactly when the view said the window was open at the last refresh().
 * COLLABORATORS: Simulation, ConvertBeesOrder, GameStateView.
 */
public final class ConversionControl {
    private final Table table = new Table();
    private final TextField bees;

    public ConversionControl(Skin skin, Simulation sim) {
        bees = UiFields.numberField(skin, 10);
        table.add(new Label("Convert bees to nectar", skin, "title")).colspan(3).left().row();
        table.add(new Label("Bees", skin, "small")).left().padRight(6);
        table.add(bees).width(70).left();
        table.add(UiFields.button(skin, "Convert", () -> {
            int n = UiFields.parse(bees);
            if (n > 0) {
                sim.submit(new ConvertBeesOrder(n));
            }
        })).padLeft(6).left().expandX();
        table.setVisible(false);
    }

    public Actor actor() {
        return table;
    }

    public void refresh(GameStateView v) {
        table.setVisible(v.isConversionWindowOpen() && !v.isGameOver());
    }
}
