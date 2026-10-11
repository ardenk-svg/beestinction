package com.michiganecodata.beestinction.bridge;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michiganecodata.beestinction.loop.TickDriver;
import com.michiganecodata.beestinction.sim.GameListener;
import com.michiganecodata.beestinction.sim.Notification;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.log.UiEventType;
import com.michiganecodata.beestinction.sim.stats.RunSummary;
import com.michiganecodata.beestinction.ui.NotificationDialog;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * OVERVIEW:      Connects the simulation's announcements (Observer) to the UI. For every notification that pauses
 *                the game it pauses the TickDriver and shows a NotificationDialog; popups posted in the same tick
 *                are shown one after another. Dismissing a popup logs POPUP_DISMISSED; when the last one is
 *                dismissed the game resumes. When the run ends it remembers the summary, and once the game-over
 *                popup has been dismissed GameScreen moves to the summary screen. The other widgets read
 *                GameStateView every frame, so day and season callbacks need no work here yet.
 * INVARIANTS:    hasOpenPopup() is true from the moment a pausing notification arrives until its dialog (and every
 *                queued one) has been dismissed.
 * COLLABORATORS: Simulation (listener registration, recordUiEvent), TickDriver, NotificationDialog, GameScreen.
 */
public final class UiGameListener implements GameListener {
    private final Simulation sim;
    private final TickDriver driver;
    private final Stage stage;
    private final Skin skin;
    private final ArrayDeque<Notification> queued = new ArrayDeque<>();
    private boolean dialogOpen;
    private RunSummary summary;

    /**
     * REQUIRES: all arguments non-null
     * MODIFIES: nothing
     * EFFECTS:  Creates the listener (the caller registers it with sim.addListener).
     */
    public UiGameListener(Simulation sim, TickDriver driver, Stage stage, Skin skin) {
        this.sim = Objects.requireNonNull(sim, "sim");
        this.driver = Objects.requireNonNull(driver, "driver");
        this.stage = Objects.requireNonNull(stage, "stage");
        this.skin = Objects.requireNonNull(skin, "skin");
    }

    @Override
    public void onNotification(Notification n) {
        if (!n.pausesGame()) {
            return;
        }
        driver.setPaused(true);
        queued.add(n);
        if (!dialogOpen) {
            showNext();
        }
    }

    @Override
    public void onGameOver(RunSummary runSummary) {
        summary = runSummary;
    }

    /** True while a popup is open or waiting to open (TickDriver's TickTarget.hasPendingPause). */
    public boolean hasOpenPopup() {
        return dialogOpen || !queued.isEmpty();
    }

    /** True once the run is over and every popup (including GAME_OVER) has been dismissed. */
    public boolean isReadyForSummary() {
        return summary != null && !hasOpenPopup();
    }

    private void showNext() {
        Notification next = queued.poll();
        if (next == null) {
            dialogOpen = false;
            if (!sim.isGameOver()) {
                driver.setPaused(false);
            }
            return;
        }
        dialogOpen = true;
        NotificationDialog.show(stage, skin, next, () -> {
            sim.recordUiEvent(UiEventType.POPUP_DISMISSED);
            showNext();
        });
    }
}
