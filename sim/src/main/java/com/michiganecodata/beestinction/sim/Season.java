package com.michiganecodata.beestinction.sim;

/**
 * OVERVIEW:      The four Michigan seasons, in order. A run starts at the beginning of SPRING.
 * INVARIANTS:    Declaration order is the order seasons occur in; next() wraps WINTER back to SPRING.
 * COLLABORATORS: GameClock (decides the current season), FlowerType (bloom seasons), WinterSystem.
 */
public enum Season {
    SPRING, SUMMER, FALL, WINTER;

    private static final Season[] VALUES = values();

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the season that follows this one (WINTER -> SPRING).
     */
    public Season next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}
