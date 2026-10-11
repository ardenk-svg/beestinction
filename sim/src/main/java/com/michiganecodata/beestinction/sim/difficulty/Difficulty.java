package com.michiganecodata.beestinction.sim.difficulty;

import com.michiganecodata.beestinction.sim.events.EventType;

/**
 * OVERVIEW:      Strategy interface for difficulty. The chosen level sets a base, and an escalation counter rises
 *                over time on top of it, so the game keeps getting harder until survival is impossible.
 *                Every method takes the 0-based day of the run and returns integers only.
 * INVARIANTS:    Implementations are deterministic functions of their configuration and the day.
 * COLLABORATORS: ScalingDifficulty (the implementation), EventScheduler, events, ForagingSystem, ScoreKeeper.
 */
public interface Difficulty {

    /** Returns true during the grace period at the start of a run (no random events). */
    boolean inGracePeriod(int day);

    /** Returns the escalation (in percent) added on top of the base by `day`. Never decreases over time. */
    int escalation(int day);

    /** Returns the score multiplier in percent (100 = 1.0x) on `day`. */
    int scoreMultiplierPercent(int day);

    /** Returns the chance (basis points, 100 bp = 1%) that an event of type t happens on `day`. */
    int eventChanceBasisPoints(EventType t, int day);

    /** Returns how far (in percent) event severities shift toward their maximum on `day`. */
    int severityShiftPercent(int day);

    /** Returns nectar yield per collection on `day`, in percent of the species' base yield. */
    int nectarYieldPercent(int day);

    /** Returns extra daily mortality on `day`, in basis points. */
    int extraMortalityBasisPoints(int day);
}
