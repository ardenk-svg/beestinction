package com.michiganecodata.beestinction.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.stats.RunSummary;
import com.michiganecodata.beestinction.ui.UiFields;

import java.util.List;
import java.util.Map;

/**
 * OVERVIEW:      Placeholder end-of-run summary (CLAUDE.md B.13): score, days survived, cause of game over,
 *                population over time (a simple line chart), nectar collected, bees lost by cause, and species
 *                discovered. Opening it sends the complete GameLog to the LogUploader. "Main menu" returns to the
 *                main menu.
 * INVARIANTS:    The log is uploaded exactly once, when the screen is created.
 * COLLABORATORS: Simulation, RunSummary, LogUploader, BeestinctionGame.
 */
public final class SummaryScreen extends StageScreen {
    private static final Color CHART_BG = new Color(0.05f, 0.05f, 0.06f, 1f);
    private static final Color CHART_LINE = new Color(1f, 0.78f, 0.2f, 1f);

    private final RunSummary summary;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Actor chartArea = new Actor();

    public SummaryScreen(BeestinctionGame game, Simulation sim) {
        super(game);
        summary = sim.summary();
        services.logUploader().uploadRunLog(sim.log());

        root.add(new Label("Run summary", skin, "title")).colspan(2).padBottom(16).row();
        addRow("Score", Long.toString(summary.score()));
        addRow("Days survived", Integer.toString(summary.daysSurvived()));
        addRow("Cause", causeText(summary));
        addRow("Peak population", Integer.toString(summary.peakPopulation()));
        addRow("Nectar collected", Long.toString(summary.totalNectarCollected()));
        addRow("Bees lost", deathsText(summary));
        addRow("Species discovered", speciesText(summary.speciesDiscovered()));
        root.add(new Label("Population over time", skin, "small")).colspan(2).padTop(16).row();
        root.add(chartArea).colspan(2).size(460, 120).padTop(4).row();
        Table buttons = new Table();
        buttons.add(UiFields.button(skin, "Main menu", game::showMainMenu)).width(180).height(44);
        root.add(buttons).colspan(2).padTop(24);
    }

    private void addRow(String name, String value) {
        root.add(new Label(name, skin, "small")).right().padRight(16).padBottom(4);
        Label v = new Label(value, skin);
        v.setWrap(true);
        root.add(v).width(420).left().padBottom(4).row();
    }

    private static String causeText(RunSummary s) {
        if (!s.isGameOver()) {
            return "The run is still going";
        }
        if (s.cause() == null) {
            return "Ended early";
        }
        if (s.cause() == DeathCause.QUEEN_STARVED) {
            return "The queen starved";
        }
        return s.cause().name();
    }

    private static String deathsText(RunSummary s) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<DeathCause, Integer> e : s.deathsByCause().entrySet()) {
            if (e.getValue() > 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(e.getKey().name().toLowerCase().replace('_', ' ')).append(' ').append(e.getValue());
            }
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    private String speciesText(List<String> ids) {
        if (ids.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            String name = id;
            for (FlowerType f : services.flowers()) {
                if (f.id().equals(id)) {
                    name = f.displayName();
                }
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        drawPopulationChart();
    }

    private void drawPopulationChart() {
        Vector2 origin = chartArea.localToStageCoordinates(new Vector2(0, 0));
        float w = chartArea.getWidth();
        float h = chartArea.getHeight();
        shapes.setProjectionMatrix(stage.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(CHART_BG);
        shapes.rect(origin.x, origin.y, w, h);
        shapes.end();
        List<Integer> pop = summary.dailyPopulation();
        if (pop.size() < 2) {
            return;
        }
        int max = 1;
        for (int p : pop) {
            max = Math.max(max, p);
        }
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(CHART_LINE);
        for (int i = 1; i < pop.size(); i++) {
            float x0 = origin.x + w * (i - 1) / (pop.size() - 1);
            float x1 = origin.x + w * i / (pop.size() - 1);
            float y0 = origin.y + h * pop.get(i - 1) / max;
            float y1 = origin.y + h * pop.get(i) / max;
            shapes.line(x0, y0, x1, y1);
        }
        shapes.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        super.dispose();
    }
}
