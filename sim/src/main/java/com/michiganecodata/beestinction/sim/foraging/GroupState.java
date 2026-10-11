package com.michiganecodata.beestinction.sim.foraging;

/**
 * OVERVIEW:      Where a forager group is in its trip: flying out, collecting at the patch, or flying home.
 * INVARIANTS:    none
 * COLLABORATORS: ForagerGroup, ForagingSystem, RecallOrder.
 */
public enum GroupState {
    OUTBOUND, FORAGING, RETURNING
}
