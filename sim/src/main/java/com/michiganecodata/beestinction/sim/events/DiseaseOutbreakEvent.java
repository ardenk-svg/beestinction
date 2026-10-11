package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.GameState;

/**
 * OVERVIEW:      A hive-wide disease outbreak. It kills 40-70% of workers (in the hive and in groups away), with
 *                randomness; the queen is immune. It is meant to feel random and powerful.
 * INVARIANTS:    none
 * COLLABORATORS: GameState, AgeHistogram.killPercent, Difficulty.severityShiftPercent, StatsRecorder,
 *                EventScheduler.
 */
public final class DiseaseOutbreakEvent extends GameEvent {

    @Override
    public EventType type() {
        return EventType.DISEASE_OUTBREAK;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: s (hive workers, every group's bees, stats, outbox)
     * EFFECTS:  Rolls a kill percentage between cfg.diseaseKillPercentMin() and cfg.diseaseKillPercentMax()
     *           with s.random(), shifted toward the maximum by s.difficulty().severityShiftPercent(day), and
     *           kills that percentage of the workers in the hive and in every group (killPercent). The queen and
     *           brood are unaffected. Records DISEASE deaths and posts a DISEASE_OUTBREAK notification.
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }
}
