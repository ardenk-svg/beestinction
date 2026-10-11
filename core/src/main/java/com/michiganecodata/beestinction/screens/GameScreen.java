package com.michiganecodata.beestinction.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.bridge.UiGameListener;
import com.michiganecodata.beestinction.loop.SpeedSetting;
import com.michiganecodata.beestinction.loop.TickDriver;
import com.michiganecodata.beestinction.loop.TickTarget;
import com.michiganecodata.beestinction.render.GroupRenderer;
import com.michiganecodata.beestinction.render.WorldRenderer;
import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.log.UiEventType;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TilePos;
import com.michiganecodata.beestinction.ui.CombControl;
import com.michiganecodata.beestinction.ui.ConversionControl;
import com.michiganecodata.beestinction.ui.FeedingControl;
import com.michiganecodata.beestinction.ui.FirstRunTips;
import com.michiganecodata.beestinction.ui.GroupListPanel;
import com.michiganecodata.beestinction.ui.Hud;
import com.michiganecodata.beestinction.ui.OrderPanel;
import com.michiganecodata.beestinction.ui.SkipButton;
import com.michiganecodata.beestinction.ui.SpeedToggleButton;

/**
 * OVERVIEW:      The in-game screen (wiring by Claude Code, polish by the humans). Creates the Simulation, the
 *                TickDriver, the renderers, the HUD and panels, and the UiGameListener; routes map clicks to the
 *                order panel and buttons to Orders. Every frame: drive the ticks, draw the map and groups
 *                (interpolated), then the UI. When the run is over and its popup dismissed, opens the summary.
 *                Debug keys (only when no text field has focus): Space pause, S speed, F fog overlay,
 *                E end the run.
 * INVARIANTS:    The simulation is changed only through Orders, ticks, and endRunEarly; UI inputs that do not
 *                change it are logged with Simulation.recordUiEvent.
 * COLLABORATORS: Simulation, TickDriver, UiGameListener, WorldRenderer, GroupRenderer, the ui widgets,
 *                LogUploader (run start), BeestinctionGame (summary).
 */
public final class GameScreen extends StageScreen {
    private static final float PANEL_WIDTH = 300f;

    private final Simulation sim;
    private final TickDriver driver;
    private final UiGameListener listener;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport mapViewport;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final WorldRenderer worldRenderer = new WorldRenderer();
    private final GroupRenderer groupRenderer = new GroupRenderer();
    private final Hud hud;
    private final SpeedToggleButton speedButton;
    private final OrderPanel orderPanel;
    private final GroupListPanel groupList;
    private final FeedingControl feeding;
    private final CombControl comb;
    private final ConversionControl conversion;
    private final SkipButton skipButton;
    private final InputMultiplexer input;
    private boolean showFog = true;
    private boolean wasSkipping;
    private boolean leaving;
    private float lastHudHeight = -1;
    private int lastWidth = -1;
    private int lastHeight = -1;

    public GameScreen(BeestinctionGame game, GameSetup setup) {
        super(game);
        sim = Simulation.create(setup);
        BalanceConfig cfg = setup.balance();
        driver = new TickDriver(new TickTarget() {
            @Override
            public void tick() {
                sim.tick();
            }

            @Override
            public boolean isGameOver() {
                return sim.isGameOver();
            }

            @Override
            public boolean hasPendingPause() {
                return listener != null && listener.hasOpenPopup();
            }

            @Override
            public boolean isSpring() {
                return sim.view().season() == Season.SPRING;
            }
        }, cfg.ticksPerSecond(), cfg.maxCatchUpTicksPerFrame(), cfg.skipTicksPerFrame());
        listener = new UiGameListener(sim, driver, stage, skin);
        sim.addListener(listener);

        GameMap map = sim.view().map();
        mapViewport = new FitViewport(map.width(), map.height(), camera);
        camera.position.set(map.width() / 2f, map.height() / 2f, 0);

        speedButton = new SpeedToggleButton(skin, driver.speed(), this::cycleSpeed);
        hud = new Hud(skin, speedButton, this::togglePause);
        orderPanel = new OrderPanel(skin, sim);
        groupList = new GroupListPanel(skin, sim);
        feeding = new FeedingControl(skin, sim);
        comb = new CombControl(skin, sim);
        conversion = new ConversionControl(skin, sim);
        skipButton = new SkipButton(skin, this::startSkip);
        FirstRunTips tips = new FirstRunTips(skin);

        root.top();
        root.add(hud.actor()).colspan(2).growX().row();
        root.add().grow(); // the map shows through here
        Table side = new Table();
        side.setBackground(skin.getDrawable("panel"));
        side.top().pad(10);
        side.defaults().left().padBottom(12);
        side.add(orderPanel.actor()).row();
        side.add(groupList.actor()).row();
        side.add(feeding.actor()).growX().row();
        side.add(comb.actor()).growX().row();
        side.add(conversion.actor()).growX().row();
        side.add(skipButton.actor()).width(180).height(40).row();
        Label keys = new Label("Keys: Space pause, S speed\nDebug: F fog overlay, E end run", skin, "small");
        side.add(keys).padTop(8);
        root.add(side).width(PANEL_WIDTH).growY();

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.bottom().left().pad(12);
        overlay.add(tips.actor());
        stage.addActor(overlay);

        input = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return selectAt(screenX, screenY);
            }

            @Override
            public boolean keyDown(int keycode) {
                return handleKey(keycode);
            }
        });

        services.logUploader().uploadRunStart(sim.log());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render(float delta) {
        driver.update(delta);
        boolean skipping = driver.isSkipping();
        if (wasSkipping && !skipping) {
            sim.recordUiEvent(UiEventType.SKIP_END);
        }
        wasSkipping = skipping;

        GameStateView v = sim.view();
        hud.refresh(v, driver.isPaused(), skipping);
        orderPanel.refresh(v);
        groupList.refresh(v);
        feeding.refresh(v);
        comb.refresh(v);
        conversion.refresh(v);
        skipButton.refresh(v, skipping);

        ScreenUtils.clear(BACKGROUND);
        stage.getViewport().apply();
        stage.act(delta);
        updateMapViewport();
        mapViewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        worldRenderer.render(shapes, v, orderPanel.selected(), showFog);
        groupRenderer.render(shapes, v, driver.interpolationAlpha());
        stage.getViewport().apply();
        stage.draw();

        if (!leaving && listener.isReadyForSummary()) {
            leaving = true;
            game.showSummary(sim);
        }
    }

    /** Fits the map into the area left of the side panel and below the HUD. */
    private void updateMapViewport() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float hudHeight = hud.actor().getHeight();
        if (w == lastWidth && h == lastHeight && hudHeight == lastHudHeight) {
            return;
        }
        lastWidth = w;
        lastHeight = h;
        lastHudHeight = hudHeight;
        int areaW = Math.max(1, Math.round(w - PANEL_WIDTH));
        int areaH = Math.max(1, Math.round(h - hudHeight));
        mapViewport.update(areaW, areaH, false);
    }

    private boolean selectAt(int screenX, int screenY) {
        Vector2 world = mapViewport.unproject(new Vector2(screenX, screenY));
        int x = (int) Math.floor(world.x);
        int y = (int) Math.floor(world.y);
        GameMap map = sim.view().map();
        if (!map.inBounds(x, y)) {
            return false;
        }
        stage.setKeyboardFocus(null);
        TilePos tile = new TilePos(x, y);
        orderPanel.setSelection(tile, map.patchAt(tile), sim.view().isRevealed(x, y));
        return true;
    }

    private boolean handleKey(int keycode) {
        if (stage.getKeyboardFocus() != null) {
            return false;
        }
        switch (keycode) {
            case Input.Keys.SPACE:
                togglePause();
                return true;
            case Input.Keys.S:
                speedButton.show(cycleSpeed());
                return true;
            case Input.Keys.F:
                showFog = !showFog; // debug
                return true;
            case Input.Keys.E:
                sim.endRunEarly(); // debug: ends the run and goes to the summary
                return true;
            default:
                return false;
        }
    }

    private void togglePause() {
        if (sim.isGameOver() || listener.hasOpenPopup()) {
            return;
        }
        boolean paused = !driver.isPaused();
        driver.setPaused(paused);
        sim.recordUiEvent(paused ? UiEventType.PAUSE : UiEventType.RESUME);
    }

    private SpeedSetting cycleSpeed() {
        SpeedSetting s = driver.cycleSpeed();
        switch (s) {
            case X2:
                sim.recordUiEvent(UiEventType.SPEED_2X);
                break;
            case X3:
                sim.recordUiEvent(UiEventType.SPEED_3X);
                break;
            default:
                sim.recordUiEvent(UiEventType.SPEED_1X);
                break;
        }
        return s;
    }

    private void startSkip() {
        if (!sim.view().isSkipAvailable() || driver.isSkipping()) {
            return;
        }
        driver.startSkip();
        sim.recordUiEvent(UiEventType.SKIP_START);
    }

    /** libGDX calls this when the window is minimized or the browser tab is hidden: auto-pause. */
    @Override
    public void pause() {
        if (!driver.isPaused() && !sim.isGameOver()) {
            driver.setPaused(true);
            sim.recordUiEvent(UiEventType.PAUSE);
        }
    }

    @Override
    public void dispose() {
        sim.removeListener(listener);
        shapes.dispose();
        super.dispose();
    }
}
