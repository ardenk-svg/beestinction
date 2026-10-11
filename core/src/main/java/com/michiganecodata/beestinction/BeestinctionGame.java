package com.michiganecodata.beestinction;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.michiganecodata.beestinction.data.BalanceLoader;
import com.michiganecodata.beestinction.data.FlowerTypeLoader;
import com.michiganecodata.beestinction.data.MapCatalog;
import com.michiganecodata.beestinction.platform.LogUploader;
import com.michiganecodata.beestinction.platform.NoOpLogUploader;
import com.michiganecodata.beestinction.screens.GameScreen;
import com.michiganecodata.beestinction.screens.MainMenuScreen;
import com.michiganecodata.beestinction.screens.MapSelectScreen;
import com.michiganecodata.beestinction.screens.SummaryScreen;
import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.ui.UiSkin;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * OVERVIEW:      The libGDX application. Loads the data files and UI skin, then runs the screen flow (the State
 *                pattern via libGDX Game/Screen): MainMenu -> MapSelect -> Game -> Summary -> MainMenu.
 *                Each platform launcher passes in its LogUploader.
 * INVARIANTS:    After create(), services is non-null and exactly one screen is showing.
 * COLLABORATORS: GameServices, the screens, BalanceLoader, FlowerTypeLoader, MapCatalog, UiSkin, LogUploader,
 *                Lwjgl3Launcher and TeaVMLauncher (create it).
 */
public class BeestinctionGame extends Game {
    private static final String TAG = "Beestinction";

    private final LogUploader logUploader;
    private GameServices services;

    /**
     * REQUIRES: logUploader != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the game with the platform's log uploader.
     */
    public BeestinctionGame(LogUploader logUploader) {
        this.logUploader = Objects.requireNonNull(logUploader, "logUploader");
    }

    /** Creates the game with logging turned off. */
    public BeestinctionGame() {
        this(new NoOpLogUploader());
    }

    @Override
    public void create() {
        try {
            BalanceConfig balance = BalanceLoader.load(Gdx.files.internal("data/balance.json"));
            List<FlowerType> flowers = FlowerTypeLoader.load(Gdx.files.internal("data/flowers.json"));
            MapCatalog maps = MapCatalog.loadDefault();
            services = new GameServices(logUploader, maps, balance, flowers, AppVersion.VERSION, UiSkin.create());
            Gdx.app.log(TAG, "Beestinction " + AppVersion.VERSION + ": loaded " + flowers.size() + " flower species and "
                    + maps.entries().size() + " map(s)");
        } catch (RuntimeException e) {
            // Make data-file mistakes easy to spot, especially in the browser console.
            Gdx.app.error(TAG, "Could not start the game: " + e.getMessage(), e);
            throw e;
        }
        showMainMenu();
    }

    public GameServices services() {
        return services;
    }

    /** Shows the main menu (disposing the current screen). */
    public void showMainMenu() {
        switchTo(() -> new MainMenuScreen(this));
    }

    /** Shows the map and difficulty selection screen. */
    public void showMapSelect() {
        switchTo(() -> new MapSelectScreen(this));
    }

    /** Starts a run with the given setup. */
    public void startGame(GameSetup setup) {
        switchTo(() -> new GameScreen(this, setup));
    }

    /** Shows the end-of-run summary for a finished simulation. */
    public void showSummary(Simulation simulation) {
        switchTo(() -> new SummaryScreen(this, simulation));
    }

    /**
     * Switches screens at the start of the next frame, then disposes the old one. Deferring matters: these methods
     * are called from button handlers while the current screen's Stage is still acting and drawing.
     */
    private void switchTo(Supplier<Screen> next) {
        Gdx.app.postRunnable(() -> {
            Screen previous = getScreen();
            setScreen(next.get());
            if (previous != null) {
                previous.dispose();
            }
        });
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        super.dispose();
        if (current != null) {
            current.dispose();
        }
        if (services != null) {
            services.dispose();
        }
    }
}
