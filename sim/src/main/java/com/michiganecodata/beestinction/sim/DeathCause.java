package com.michiganecodata.beestinction.sim;

/**
 * OVERVIEW:      Why bees died (for the summary's "bees lost by cause"), and why a run ended
 *                (QUEEN_STARVED is the game-over cause).
 * INVARIANTS:    none
 * COLLABORATORS: StatsRecorder, RunSummary, GameState.endGame.
 */
public enum DeathCause {
    OLD_AGE,
    STARVATION,
    PESTICIDE,
    DISEASE,
    FREEZING,
    WINTER_DIE_OFF,
    CONVERTED,
    QUEEN_STARVED
}
