package com.michiganecodata.beestinction.sim.util;

/**
 * OVERVIEW:      A seeded, deterministic pseudo-random number generator (SplitMix64). Every random decision in
 *                the simulation goes through a GameRandom, so the same seed always produces the same game on
 *                desktop, on the web, and in tests.
 * INVARIANTS:    The output sequence depends only on the seed and on the sequence of calls made.
 *                Every public method that returns a random value consumes exactly one 64-bit draw per attempt,
 *                regardless of its arguments (nextInt may retry to avoid bias).
 * COLLABORATORS: Used by GameState (the game's generator), GameLog (run IDs, seeded separately), and the
 *                rules (EventScheduler, ForagingSystem, WinterSystem, events).
 */
public final class GameRandom {
    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private long state;

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Creates a generator whose sequence is fully determined by `seed`.
     */
    public GameRandom(long seed) {
        this.state = seed;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Returns the next 64-bit value in the sequence (any long value is possible).
     */
    public long nextLong() {
        long z = (state += GOLDEN_GAMMA);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * REQUIRES: bound > 0
     * MODIFIES: this
     * EFFECTS:  Returns a uniformly distributed int in [0, bound). Throws IllegalArgumentException if
     *           bound <= 0. Uses rejection sampling, so it is unbiased for every bound.
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive: " + bound);
        }
        int bits;
        int value;
        do {
            bits = (int) (nextLong() >>> 33); // 31 random bits, never negative
            value = bits % bound;
        } while (bits - value + (bound - 1) < 0); // reject the incomplete final block (overflow check)
        return value;
    }

    /**
     * REQUIRES: min <= max, and max - min < Integer.MAX_VALUE
     * MODIFIES: this
     * EFFECTS:  Returns a uniformly distributed int in [min, max]. Returns min when min == max (still consuming
     *           one draw, so call sequences stay aligned). Throws IllegalArgumentException if min > max.
     */
    public int rangeInclusive(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min > max: " + min + " > " + max);
        }
        long span = (long) max - (long) min + 1L;
        if (span > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range too large: " + min + ".." + max);
        }
        return min + nextInt((int) span);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Returns true with probability pct/100. pct <= 0 is always false and pct >= 100 is always true.
     *           Always consumes one draw, whatever pct is.
     */
    public boolean chancePercent(int pct) {
        int roll = nextInt(100);
        return roll < pct;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Returns true with probability bp/10000 (1 basis point = 0.01%). bp <= 0 is always false and
     *           bp >= 10000 is always true. Always consumes one draw, whatever bp is.
     */
    public boolean chanceBasisPoints(int bp) {
        int roll = nextInt(10_000);
        return roll < bp;
    }
}
