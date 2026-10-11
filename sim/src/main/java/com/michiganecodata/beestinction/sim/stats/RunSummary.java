package com.michiganecodata.beestinction.sim.stats;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OVERVIEW:      The end-of-run summary (CLAUDE.md B.13): score, days survived, cause of game over, population
 *                over time, nectar collected, bees lost by cause, and species discovered. Shown on the summary
 *                screen and written into the GameLog. Built by StatsRecorder.buildSummary.
 * INVARIANTS:    Immutable. daysSurvived >= 0; peakPopulation >= 0; deathsByCause has an entry (>= 0) for every
 *                DeathCause; speciesDiscovered is sorted. cause is null if the run has not ended, or if it was
 *                ended early by the player (debug end). Value semantics (equals compares every field).
 * COLLABORATORS: StatsRecorder, GameLog, JsonWriter, SummaryScreen (core).
 */
public final class RunSummary {
    private final long score;
    private final int daysSurvived;
    private final boolean gameOver;
    private final DeathCause cause;
    private final int peakPopulation;
    private final List<Integer> dailyPopulation;
    private final EnumMap<DeathCause, Integer> deathsByCause;
    private final long totalNectarCollected;
    private final List<String> speciesDiscovered;

    /**
     * REQUIRES: the RunSummary invariants hold for the arguments (lists and map non-null)
     * MODIFIES: nothing
     * EFFECTS:  Creates a summary. Collections are copied; missing causes count as 0; species are sorted.
     */
    public RunSummary(long score, int daysSurvived, boolean gameOver, DeathCause cause, int peakPopulation,
                      List<Integer> dailyPopulation, Map<DeathCause, Integer> deathsByCause,
                      long totalNectarCollected, List<String> speciesDiscovered) {
        this.score = score;
        this.daysSurvived = daysSurvived;
        this.gameOver = gameOver;
        this.cause = cause;
        this.peakPopulation = peakPopulation;
        this.dailyPopulation = Collections.unmodifiableList(new ArrayList<>(dailyPopulation));
        this.deathsByCause = new EnumMap<>(DeathCause.class);
        for (DeathCause c : DeathCause.values()) {
            Integer n = deathsByCause.get(c);
            this.deathsByCause.put(c, n == null ? 0 : n);
        }
        this.totalNectarCollected = totalNectarCollected;
        List<String> species = new ArrayList<>(speciesDiscovered);
        Collections.sort(species);
        this.speciesDiscovered = Collections.unmodifiableList(species);
    }

    public long score() {
        return score;
    }

    /** Full days completed before the run ended (0-based day number at the end). */
    public int daysSurvived() {
        return daysSurvived;
    }

    /** True if the run has ended. */
    public boolean isGameOver() {
        return gameOver;
    }

    /** The cause of game over, or null (not over yet, or ended early). */
    public DeathCause cause() {
        return cause;
    }

    public int peakPopulation() {
        return peakPopulation;
    }

    /** Population sampled at the start of every day, in day order. */
    public List<Integer> dailyPopulation() {
        return dailyPopulation;
    }

    /** Bees lost by cause (every cause present). */
    public Map<DeathCause, Integer> deathsByCause() {
        return Collections.unmodifiableMap(deathsByCause);
    }

    public long totalNectarCollected() {
        return totalNectarCollected;
    }

    /** IDs of species discovered, sorted. */
    public List<String> speciesDiscovered() {
        return speciesDiscovered;
    }

    /**
     * REQUIRES: w != null and positioned where a value may be written
     * MODIFIES: w
     * EFFECTS:  Writes this summary as one JSON object.
     */
    public void write(JsonWriter w) {
        w.beginObject();
        w.name("score").value(score);
        w.name("daysSurvived").value(daysSurvived);
        w.name("gameOver").value(gameOver);
        w.name("cause").value(cause == null ? null : cause.name());
        w.name("peakPopulation").value(peakPopulation);
        w.name("dailyPopulation").beginArray();
        for (Integer p : dailyPopulation) {
            w.value(p);
        }
        w.endArray();
        w.name("deathsByCause").beginObject();
        for (Map.Entry<DeathCause, Integer> e : deathsByCause.entrySet()) {
            w.name(e.getKey().name()).value(e.getValue());
        }
        w.endObject();
        w.name("totalNectarCollected").value(totalNectarCollected);
        w.name("speciesDiscovered").beginArray();
        for (String s : speciesDiscovered) {
            w.value(s);
        }
        w.endArray();
        w.endObject();
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns this summary as a JSON string.
     */
    public String toJson() {
        JsonWriter w = new JsonWriter();
        write(w);
        return w.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RunSummary)) {
            return false;
        }
        RunSummary r = (RunSummary) o;
        return score == r.score && daysSurvived == r.daysSurvived && gameOver == r.gameOver && cause == r.cause
                && peakPopulation == r.peakPopulation && dailyPopulation.equals(r.dailyPopulation)
                && deathsByCause.equals(r.deathsByCause) && totalNectarCollected == r.totalNectarCollected
                && speciesDiscovered.equals(r.speciesDiscovered);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, daysSurvived, gameOver, cause, peakPopulation, dailyPopulation, deathsByCause,
                totalNectarCollected, speciesDiscovered);
    }

    @Override
    public String toString() {
        return "RunSummary" + toJson();
    }
}
