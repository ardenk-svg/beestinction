package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.Season;

/**
 * OVERVIEW:      Placeholder heads-up display across the top of the game screen (CLAUDE.md B.12): nectar and
 *                storage, population and capacity, season and day, score, the age pipeline, the speed toggle, and
 *                pause. Bound to GameStateView and refreshed every frame; the humans will design the real HUD.
 * INVARIANTS:    Shows the values from the last refresh().
 * COLLABORATORS: AgePipelinePanel, SpeedToggleButton, GameStateView, GameScreen.
 */
public final class Hud {
    private final Table table = new Table();
    private final Label nectar;
    private final Label population;
    private final Label calendar;
    private final Label score;
    private final Label status;
    private final TextButton pause;
    private final AgePipelinePanel pipeline;
    private final long[] last = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
    private String lastStatus = null;

    /**
     * REQUIRES: all arguments non-null
     * MODIFIES: nothing
     * EFFECTS:  Builds the HUD. onPauseToggle runs when the pause button is pressed.
     */
    public Hud(Skin skin, SpeedToggleButton speed, Runnable onPauseToggle) {
        nectar = new Label("", skin);
        population = new Label("", skin);
        calendar = new Label("", skin, "title");
        score = new Label("", skin);
        status = new Label("", skin, "warning");
        pipeline = new AgePipelinePanel(skin);
        pause = UiFields.button(skin, "Pause", onPauseToggle);

        table.setBackground(skin.getDrawable("panel"));
        table.pad(6, 10, 6, 10);
        table.defaults().padRight(18);
        table.add(calendar).left();
        table.add(nectar).left();
        table.add(population).left();
        table.add(score).left().expandX();
        table.add(speed.actor()).width(100).padRight(6);
        table.add(pause).width(90).padRight(0);
        table.row();
        table.add(pipeline.actor()).colspan(4).left().padTop(4);
        table.add(status).colspan(2).right().padTop(4).padRight(0);
    }

    public Actor actor() {
        return table;
    }

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Updates every label from the view (text is only rebuilt when a value changes).
     */
    public void refresh(GameStateView v, boolean paused, boolean skipping) {
        if (changed(0, v.nectar()) | changed(1, v.nectarCapacity())) {
            nectar.setText("Nectar " + v.nectar() + " / " + v.nectarCapacity());
        }
        if (changed(2, v.population()) | changed(3, v.populationCapacity())) {
            population.setText("Population " + v.population() + " / " + v.populationCapacity());
        }
        if (changed(4, v.season().ordinal()) | changed(5, v.dayOfSeason()) | changed(6, v.year())) {
            calendar.setText(seasonName(v.season()) + ", day " + (v.dayOfSeason() + 1) + "  (year " + (v.year() + 1)
                    + ")");
        }
        if (changed(7, v.score())) {
            score.setText("Score " + v.score());
        }
        pipeline.refresh(v);
        String s = v.isGameOver() ? "Game over" : skipping ? "Skipping to spring..." : paused ? "Paused" : "";
        if (!s.equals(lastStatus)) {
            status.setText(s);
            pause.setText(paused ? "Resume" : "Pause");
            lastStatus = s;
        }
    }

    private boolean changed(int i, long value) {
        if (last[i] == value) {
            return false;
        }
        last[i] = value;
        return true;
    }

    /** "Spring", "Summer", "Fall", or "Winter". */
    public static String seasonName(Season s) {
        String n = s.name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }
}
