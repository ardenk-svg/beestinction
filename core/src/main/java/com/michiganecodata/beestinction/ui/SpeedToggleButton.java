package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.michiganecodata.beestinction.loop.SpeedSetting;

import java.util.function.Supplier;

/**
 * OVERVIEW:      Placeholder speed toggle: each press cycles 1x -> 2x -> 3x -> 1x.
 * INVARIANTS:    The label shows the speed returned by the last cycle.
 * COLLABORATORS: Hud, GameScreen (supplies the cycle action, which also logs the UI event), TickDriver.
 */
public final class SpeedToggleButton {
    private final TextButton button;

    /**
     * REQUIRES: skin, cycle != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the button; pressing it calls cycle (which changes the speed and returns the new one).
     */
    public SpeedToggleButton(Skin skin, SpeedSetting initial, Supplier<SpeedSetting> cycle) {
        button = new TextButton("", skin);
        show(initial);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                show(cycle.get());
            }
        });
    }

    /** Updates the label (for example after a keyboard shortcut changed the speed). */
    public void show(SpeedSetting speed) {
        button.setText("Speed " + speed.label());
    }

    public Actor actor() {
        return button;
    }
}
