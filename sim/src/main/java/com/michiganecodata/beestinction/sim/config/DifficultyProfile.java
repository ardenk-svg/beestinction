package com.michiganecodata.beestinction.sim.config;

/**
 * OVERVIEW:      The base numbers for one difficulty level (CLAUDE.md D.7). ScalingDifficulty adds escalation
 *                over time on top of these.
 * INVARIANTS:    Immutable. All values are >= 0.
 * COLLABORATORS: BalanceConfig (holds one per DifficultyLevel), ScalingDifficulty.
 */
public final class DifficultyProfile {
    private final int baseMultiplierPercent;
    private final int escalationPercentPerWeek;
    private final int yieldPercent;
    private final int eventBaseBasisPoints;

    /**
     * REQUIRES: every argument >= 0
     * MODIFIES: nothing
     * EFFECTS:  Creates a profile. Throws IllegalArgumentException if any argument is negative.
     */
    public DifficultyProfile(int baseMultiplierPercent, int escalationPercentPerWeek, int yieldPercent,
                             int eventBaseBasisPoints) {
        if (baseMultiplierPercent < 0 || escalationPercentPerWeek < 0 || yieldPercent < 0
                || eventBaseBasisPoints < 0) {
            throw new IllegalArgumentException("difficulty profile values must be >= 0");
        }
        this.baseMultiplierPercent = baseMultiplierPercent;
        this.escalationPercentPerWeek = escalationPercentPerWeek;
        this.yieldPercent = yieldPercent;
        this.eventBaseBasisPoints = eventBaseBasisPoints;
    }

    /** Score multiplier at the start of a run, in percent (100 = 1.0x). */
    public int baseMultiplierPercent() {
        return baseMultiplierPercent;
    }

    /** How much difficulty rises per in-game week, in percent. */
    public int escalationPercentPerWeek() {
        return escalationPercentPerWeek;
    }

    /** Nectar yield per collection, in percent of the species' base yield. */
    public int yieldPercent() {
        return yieldPercent;
    }

    /** Base daily chance of a random event, in basis points (100 bp = 1%). */
    public int eventBaseBasisPoints() {
        return eventBaseBasisPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DifficultyProfile)) {
            return false;
        }
        DifficultyProfile p = (DifficultyProfile) o;
        return baseMultiplierPercent == p.baseMultiplierPercent
                && escalationPercentPerWeek == p.escalationPercentPerWeek
                && yieldPercent == p.yieldPercent
                && eventBaseBasisPoints == p.eventBaseBasisPoints;
    }

    @Override
    public int hashCode() {
        int h = baseMultiplierPercent;
        h = 31 * h + escalationPercentPerWeek;
        h = 31 * h + yieldPercent;
        h = 31 * h + eventBaseBasisPoints;
        return h;
    }

    @Override
    public String toString() {
        return "DifficultyProfile[base=" + baseMultiplierPercent + "%, escalation=" + escalationPercentPerWeek
                + "%/week, yield=" + yieldPercent + "%, events=" + eventBaseBasisPoints + "bp]";
    }
}
