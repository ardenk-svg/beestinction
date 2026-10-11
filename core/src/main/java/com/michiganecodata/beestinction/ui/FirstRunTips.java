package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * OVERVIEW:      A few placeholder first-run tips (CLAUDE.md B.12), shown one at a time until the player has seen
 *                them all. Remembered in libGDX Preferences (a local file on desktop, localStorage on the web), so
 *                they only appear on the first run. This is a UI convenience, not game state.
 * INVARIANTS:    Hidden once every tip has been dismissed.
 * COLLABORATORS: GameScreen.
 */
public final class FirstRunTips {
    private static final String PREFS = "beestinction";
    private static final String SEEN_KEY = "firstRunTipsSeen";
    private static final String[] TIPS = {
            "Tip: click a flower patch on the map, choose how many bees, then press Send.",
            "Tip: Standing sends the same bees back to a patch again and again until you cancel it.",
            "Tip: watch your nectar. The queen eats first; if she goes hungry two days in a row, the colony is lost.",
    };

    private final Table table = new Table();
    private final Label text;
    private final TextButton next;
    private int index;

    public FirstRunTips(Skin skin) {
        text = new Label("", skin);
        text.setWrap(true);
        next = UiFields.button(skin, "Next", this::advance);
        table.setBackground(skin.getDrawable("panel"));
        table.pad(8);
        table.add(text).width(420).left().padRight(8);
        table.add(next).width(90);
        boolean seen = prefs().getBoolean(SEEN_KEY, false);
        table.setVisible(!seen);
        show();
    }

    public Actor actor() {
        return table;
    }

    private void show() {
        text.setText(TIPS[index]);
        next.setText(index == TIPS.length - 1 ? "Got it" : "Next");
    }

    private void advance() {
        index++;
        if (index >= TIPS.length) {
            table.setVisible(false);
            Preferences p = prefs();
            p.putBoolean(SEEN_KEY, true);
            p.flush();
            return;
        }
        show();
    }

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS);
    }
}
