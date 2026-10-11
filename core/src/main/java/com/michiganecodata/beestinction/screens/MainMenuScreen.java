package com.michiganecodata.beestinction.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.ui.UiFields;

/**
 * OVERVIEW:      Placeholder main menu: title, Play button, a "best played on desktop" notice, and the notice that
 *                anonymous gameplay data is collected (CLAUDE.md B.12, G).
 * INVARIANTS:    none
 * COLLABORATORS: BeestinctionGame (Play opens MapSelectScreen).
 */
public final class MainMenuScreen extends StageScreen {

    public MainMenuScreen(BeestinctionGame game) {
        super(game);
        Label title = new Label("Beestinction", skin, "title");
        title.setFontScale(2.5f);
        root.add(title).padBottom(8).row();
        root.add(new Label("A bumblebee colony survival game by Michigan EcoData", skin)).padBottom(32).row();
        root.add(UiFields.button(skin, "Play", game::showMapSelect)).width(220).height(48).padBottom(32).row();
        root.add(new Label("Best played on desktop.", skin, "small")).padBottom(4).row();
        root.add(new Label("Anonymous gameplay data (no personal information) is collected to improve the game.",
                skin, "small")).padBottom(16).row();
        root.add(new Label("Version " + services.appVersion(), skin, "small"));
    }
}
