package com.michiganecodata.beestinction.sim.colony;

/**
 * OVERVIEW:      Read-only view of an AgeHistogram, for the UI, the renderer, and test invariant checks.
 * INVARIANTS:    Same as AgeHistogram.
 * COLLABORATORS: AgeHistogram (implements it), GameStateView.
 */
public interface AgeHistogramView {
    /** Number of age slots (ages 0 .. lifespanDays-1). */
    int lifespanDays();

    /** Every living bee, including the "dying today" pool. */
    int total();

    /** Bees of exactly `age` days (0 for ages outside 0..lifespanDays-1). The dying pool is not included. */
    int count(int age);

    /** Bees that reached the end of their lifespan and will die over the course of today. */
    int dying();

    /** Bees aged `age` days or older, not counting the dying pool. */
    int countAtLeastAge(int age);

    /** Bees younger than `age` days. */
    int countBelowAge(int age);
}
