package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.colony.AgeHistogramView;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.difficulty.Difficulty;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;

import java.util.List;
import java.util.Set;

/**
 * OVERVIEW:      Read-only interface over GameState for the UI, the renderer, order validation, and test checks.
 *                It exposes values, not the mutable parts. (ForagerGroup objects in groups() are the live
 *                groups: read them, never change them, outside the sim.)
 * INVARIANTS:    Calling any method never changes the game.
 * COLLABORATORS: GameState (implements it), Order.validate, StatsRecorder, WinterSystem.isSkipAvailable,
 *                the UI and renderers in core, InvariantChecker and RandomPlayer (tests).
 */
public interface GameStateView {

    // ----- Setup -----

    BalanceConfig balance();

    DifficultyLevel difficultyLevel();

    Difficulty difficulty();

    /** The run's own copy of the map (patch state included). */
    GameMap map();

    // ----- Time -----

    /** The current tick (the tick running now, or the next one to run between ticks). */
    long tick();

    int dayOfRun();

    int tickOfDay();

    int weekOfRun();

    Season season();

    int dayOfSeason();

    int year();

    // ----- Colony -----

    int nectar();

    int nectarCapacity();

    /** Adult workers in the hive plus workers away in groups (queen and brood not counted). */
    int population();

    int populationCapacity();

    int workersInHive();

    /** Workers in the hive younger than the forage age. */
    int houseBees();

    /** Workers in the hive old enough to be sent out. */
    int availableForagers();

    /** Workers away from the hive in groups. */
    int beesOut();

    /** Eggs and larvae still developing. */
    int broodCount();

    /** The hive's workers by age (read-only). */
    AgeHistogramView hiveWorkers();

    int feedingLevel();

    int combSize();

    boolean isCombExpanding();

    // ----- World -----

    /** Every group currently away from the hive, in a fixed order (unmodifiable). */
    List<ForagerGroup> groups();

    /** The group with this id, or null. */
    ForagerGroup findGroup(int groupId);

    boolean isRevealed(int x, int y);

    boolean isPatchDiscovered(FlowerPatch p);

    /** IDs of species collected at least once, sorted. */
    Set<String> discoveredSpeciesIds();

    // ----- Score and statistics -----

    long score();

    /** Bees that emerged from brood so far (starting workers are not births). */
    int totalBirths();

    /** Bees lost to every cause so far, including conversion. */
    int totalDeaths();

    int deaths(DeathCause cause);

    // ----- Winter -----

    boolean isConversionWindowOpen();

    boolean isSkipAvailable();

    // ----- Game over -----

    boolean isGameOver();

    /** Why the game ended, or null (still running, or ended early). */
    DeathCause gameOverCause();
}
