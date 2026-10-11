package com.michiganecodata.beestinction.sim.log;

/**
 * OVERVIEW:      UI inputs that do not change the simulation but are logged for analytics (CLAUDE.md C.7).
 * INVARIANTS:    none
 * COLLABORATORS: UiEventRecord, GameLog, Simulation.recordUiEvent, the UI in core.
 */
public enum UiEventType {
    SPEED_1X, SPEED_2X, SPEED_3X, PAUSE, RESUME, SKIP_START, SKIP_END, POPUP_DISMISSED
}
