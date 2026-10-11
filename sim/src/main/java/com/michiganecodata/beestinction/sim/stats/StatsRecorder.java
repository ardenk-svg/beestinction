package com.michiganecodata.beestinction.sim.stats;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameStateView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * OVERVIEW:      Collects the run's statistics as the game is played: population each day, bees lost by cause,
 *                births, and nectar collected, and builds the RunSummary.
 *                The rules call recordDeaths, recordBirths, and recordNectarCollected whenever those happen;
 *                Simulation calls recordDay at the start of each day.
 * INVARIANTS:    Every count is >= 0 and never decreases. dailyPopulation has one entry per recorded day.
 * COLLABORATORS: RunSummary, GameStateView, DeathCause, Simulation, the rules.
 */
public final class StatsRecorder {
    private final List<Integer> dailyPopulation = new ArrayList<>();
    private final EnumMap<DeathCause, Integer> deaths = new EnumMap<>(DeathCause.class);
    private int peakPopulation;
    private int totalBirths;
    private long totalNectarCollected;

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Records the population at the start of a day and updates the peak population.
     */
    public void recordDay(GameStateView v) {
        int population = v.population();
        dailyPopulation.add(population);
        peakPopulation = Math.max(peakPopulation, population);
    }

    /**
     * REQUIRES: cause != null, n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n bees lost to `cause`. Throws IllegalArgumentException if n < 0.
     */
    public void recordDeaths(DeathCause cause, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("deaths must be >= 0: " + n);
        }
        if (n == 0) {
            return;
        }
        Integer old = deaths.get(cause);
        deaths.put(cause, (old == null ? 0 : old) + n);
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n bees that emerged from brood (starting workers are not births).
     *           Throws IllegalArgumentException if n < 0.
     */
    public void recordBirths(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("births must be >= 0: " + n);
        }
        totalBirths += n;
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: this
     * EFFECTS:  Adds n nectar delivered to the hive. Throws IllegalArgumentException if n < 0.
     */
    public void recordNectarCollected(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("nectar must be >= 0: " + n);
        }
        totalNectarCollected += n;
    }

    /** Bees lost to one cause so far. */
    public int deaths(DeathCause cause) {
        Integer n = deaths.get(cause);
        return n == null ? 0 : n;
    }

    /** Bees lost to all causes so far (including conversion). */
    public int totalDeaths() {
        int sum = 0;
        for (Integer n : deaths.values()) {
            sum += n;
        }
        return sum;
    }

    public int totalBirths() {
        return totalBirths;
    }

    public long totalNectarCollected() {
        return totalNectarCollected;
    }

    public int peakPopulation() {
        return peakPopulation;
    }

    /** Unmodifiable view of the population recorded at the start of each day. */
    public List<Integer> dailyPopulation() {
        return Collections.unmodifiableList(dailyPopulation);
    }

    /**
     * REQUIRES: v != null
     * MODIFIES: nothing
     * EFFECTS:  Returns a RunSummary of the run so far. daysSurvived is v.dayOfRun() (full days completed);
     *           the cause is v.gameOverCause() (null if the game is not over or was ended early).
     *           DECISION: "days survived" counts completed days (a run that ends on day 5, 0-based, survived 5).
     */
    public RunSummary buildSummary(GameStateView v) {
        int peak = Math.max(peakPopulation, v.population());
        List<String> species = new ArrayList<>(v.discoveredSpeciesIds());
        return new RunSummary(v.score(), v.dayOfRun(), v.isGameOver(), v.gameOverCause(), peak, dailyPopulation,
                deaths, totalNectarCollected, species);
    }
}
