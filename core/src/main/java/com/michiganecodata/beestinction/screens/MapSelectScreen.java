package com.michiganecodata.beestinction.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.data.MapCatalog;
import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.ui.UiFields;

/**
 * OVERVIEW:      Placeholder map and difficulty selection: a list of maps with descriptions, Easy / Medium / Hard,
 *                then Start. Each run gets a new random seed (and a separate random seed for the log's run ID).
 * INVARIANTS:    selectedMap is non-null once the screen is built (if any map exists).
 * COLLABORATORS: MapCatalog, GameSetup, BeestinctionGame (startGame).
 */
public final class MapSelectScreen extends StageScreen {
    private final Label description;
    private final Label error;
    private MapCatalog.Entry selectedMap;
    private DifficultyLevel level = DifficultyLevel.EASY;

    public MapSelectScreen(BeestinctionGame game) {
        super(game);
        description = new Label("", skin);
        description.setWrap(true);
        error = new Label("", skin, "warning");
        error.setWrap(true);

        root.add(new Label("Choose a map", skin, "title")).colspan(2).padBottom(16).row();

        Table maps = new Table();
        ButtonGroup<TextButton> mapGroup = new ButtonGroup<>();
        mapGroup.setMinCheckCount(1);
        mapGroup.setMaxCheckCount(1);
        TextButton first = null;
        for (final MapCatalog.Entry entry : services.maps().entries()) {
            final TextButton b = new TextButton(entry.displayName(), skin, "toggle");
            b.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (b.isChecked()) {
                        select(entry);
                    }
                }
            });
            mapGroup.add(b);
            maps.add(b).width(240).height(40).padBottom(6).row();
            if (first == null) {
                first = b;
            }
        }
        root.add(maps).top().padRight(24);
        root.add(description).width(380).top().left().row();

        Table difficulty = new Table();
        ButtonGroup<TextButton> levelGroup = new ButtonGroup<>();
        levelGroup.setMinCheckCount(1);
        levelGroup.setMaxCheckCount(1);
        for (final DifficultyLevel l : DifficultyLevel.values()) {
            String name = l.name().charAt(0) + l.name().substring(1).toLowerCase();
            final TextButton b = new TextButton(name, skin, "toggle");
            b.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (b.isChecked()) {
                        level = l;
                    }
                }
            });
            levelGroup.add(b);
            difficulty.add(b).width(110).height(36).padRight(8);
            if (l == level) {
                b.setChecked(true);
            }
        }
        root.add(new Label("Difficulty", skin, "title")).colspan(2).padTop(28).row();
        root.add(difficulty).colspan(2).padTop(8).row();

        Table buttons = new Table();
        buttons.add(UiFields.button(skin, "Back", game::showMainMenu)).width(140).height(40).padRight(16);
        buttons.add(UiFields.button(skin, "Start", this::start)).width(180).height(44);
        root.add(buttons).colspan(2).padTop(32).row();
        root.add(error).colspan(2).width(620).padTop(12);

        if (first != null) {
            first.setChecked(true);
        } else {
            error.setText("No maps found. Add .tmx files to MapCatalog.MAP_FILES.");
        }
    }

    private void select(MapCatalog.Entry entry) {
        selectedMap = entry;
        description.setText(entry.description());
    }

    private void start() {
        if (selectedMap == null) {
            return;
        }
        try {
            GameMap map = services.maps().loadMap(selectedMap, services.flowers());
            long seed = MathUtils.random.nextLong();
            long runIdSeed = MathUtils.random.nextLong();
            GameSetup setup = new GameSetup(seed, map, level, services.balance(), services.flowers(),
                    services.appVersion(), runIdSeed);
            game.startGame(setup);
        } catch (RuntimeException ex) {
            Gdx.app.error("Beestinction", "Could not load map " + selectedMap.path(), ex);
            error.setText("Could not load the map: " + ex.getMessage());
        }
    }
}
