package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.michiganecodata.beestinction.sim.GameStateView;

/**
 * OVERVIEW:      Placeholder "skip to spring" button, visible only after the winter die-off (CLAUDE.md B.9).
 *                Pressing it starts the TickDriver's fast-forward (every tick still runs).
 * INVARIANTS:    Visible exactly when skipping is available and not already running.
 * COLLABORATORS: GameScreen (supplies the action, which also logs SKIP_START), TickDriver, GameStateView.
 */
public final class SkipButton {
    private final TextButton button;

    public SkipButton(Skin skin, Runnable onSkip) {
        button = UiFields.button(skin, "Skip to spring", onSkip);
        button.setVisible(false);
    }

    public Actor actor() {
        return button;
    }

    public void refresh(GameStateView v, boolean skipping) {
        button.setVisible(v.isSkipAvailable() && !skipping && !v.isGameOver());
    }
}
