package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.stats.RunSummary;

/**
 * OVERVIEW:      Observer interface: the simulation announces what happened, and the UI, logger, and tests
 *                subscribe with Simulation.addListener. The simulation never calls UI code directly.
 *                All methods have no-op defaults, so a listener overrides only what it needs.
 * INVARIANTS:    Listeners are called by Simulation only, after a tick's systems have run (never in the
 *                middle of a system). Listeners must not modify the GameState.
 * COLLABORATORS: Simulation (dispatches), Notification, RunSummary, UiGameListener (core).
 */
public interface GameListener {

    /**
     * REQUIRES: n != null
     * MODIFIES: whatever the listener owns (never the GameState)
     * EFFECTS:  Called once for every notification posted during a tick, in the order they were posted.
     */
    default void onNotification(Notification n) {
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: whatever the listener owns
     * EFFECTS:  Called at the start of every day (day 0 is the first day of the run).
     */
    default void onDayStart(int day) {
    }

    /**
     * REQUIRES: season != null
     * MODIFIES: whatever the listener owns
     * EFFECTS:  Called when a new season begins, including SPRING at the very first tick.
     */
    default void onSeasonChanged(Season season) {
    }

    /**
     * REQUIRES: summary != null
     * MODIFIES: whatever the listener owns
     * EFFECTS:  Called exactly once, when the run ends.
     */
    default void onGameOver(RunSummary summary) {
    }
}
