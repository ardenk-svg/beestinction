package com.michiganecodata.beestinction.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.GameServices;

/**
 * OVERVIEW:      Base class for the game's libGDX screens (inheritance is allowed for screens, CLAUDE.md A.5
 *                rule 8): a Scene2D Stage with a ScreenViewport (1 unit = 1 pixel) and a root Table that fills it.
 *                Subclasses add their widgets to `root` in their constructors.
 * INVARIANTS:    stage and root are non-null until dispose().
 * COLLABORATORS: BeestinctionGame, GameServices, the concrete screens.
 */
public abstract class StageScreen extends ScreenAdapter {
    protected static final Color BACKGROUND = new Color(0.10f, 0.12f, 0.10f, 1f);

    protected final BeestinctionGame game;
    protected final GameServices services;
    protected final Skin skin;
    protected final Stage stage;
    protected final Table root;

    protected StageScreen(BeestinctionGame game) {
        this.game = game;
        this.services = game.services();
        this.skin = services.skin();
        this.stage = new Stage(new ScreenViewport());
        this.root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BACKGROUND);
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
