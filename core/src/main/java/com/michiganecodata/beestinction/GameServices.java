package com.michiganecodata.beestinction;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.michiganecodata.beestinction.data.MapCatalog;
import com.michiganecodata.beestinction.platform.LogUploader;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.FlowerType;

import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      The shared services and data every screen needs: the platform's LogUploader, the map catalog,
 *                the balance values, the flower species, the app version, and the UI skin. Created once by
 *                BeestinctionGame.create().
 * INVARIANTS:    Every field is non-null.
 * COLLABORATORS: BeestinctionGame, the screens, LogUploader, MapCatalog, BalanceConfig, FlowerType, Skin.
 */
public final class GameServices implements Disposable {
    private final LogUploader logUploader;
    private final MapCatalog maps;
    private final BalanceConfig balance;
    private final List<FlowerType> flowers;
    private final String appVersion;
    private final Skin skin;

    /**
     * REQUIRES: all arguments non-null
     * MODIFIES: nothing
     * EFFECTS:  Bundles the services. The skin is disposed with this object.
     */
    public GameServices(LogUploader logUploader, MapCatalog maps, BalanceConfig balance, List<FlowerType> flowers,
                        String appVersion, Skin skin) {
        this.logUploader = Objects.requireNonNull(logUploader, "logUploader");
        this.maps = Objects.requireNonNull(maps, "maps");
        this.balance = Objects.requireNonNull(balance, "balance");
        this.flowers = Objects.requireNonNull(flowers, "flowers");
        this.appVersion = Objects.requireNonNull(appVersion, "appVersion");
        this.skin = Objects.requireNonNull(skin, "skin");
    }

    public LogUploader logUploader() {
        return logUploader;
    }

    public MapCatalog maps() {
        return maps;
    }

    public BalanceConfig balance() {
        return balance;
    }

    public List<FlowerType> flowers() {
        return flowers;
    }

    public String appVersion() {
        return appVersion;
    }

    public Skin skin() {
        return skin;
    }

    @Override
    public void dispose() {
        skin.dispose();
    }
}
