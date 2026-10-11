package com.michiganecodata.beestinction.loop;

/**
 * OVERVIEW:      Game speed: 1x, 2x, or 3x the base rate of 4 ticks per second. The speed toggle cycles
 *                1x -> 2x -> 3x -> 1x.
 * INVARIANTS:    multiplier() is 1, 2, or 3.
 * COLLABORATORS: TickDriver, SpeedToggleButton.
 */
public enum SpeedSetting {
    X1(1), X2(2), X3(3);

    private final int multiplier;

    SpeedSetting(int multiplier) {
        this.multiplier = multiplier;
    }

    /** The speed multiplier (1, 2, or 3). */
    public int multiplier() {
        return multiplier;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the next speed in the cycle 1x -> 2x -> 3x -> 1x.
     */
    public SpeedSetting next() {
        SpeedSetting[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    /** A label such as "1x". */
    public String label() {
        return multiplier + "x";
    }
}
