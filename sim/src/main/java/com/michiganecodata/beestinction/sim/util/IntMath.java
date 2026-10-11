package com.michiganecodata.beestinction.sim.util;

/**
 * OVERVIEW:      Integer-only math helpers. The simulation never uses float or double (CLAUDE.md A.5 rule 2),
 *                so square roots, clamping, and percentages are done here with exact integer arithmetic.
 * INVARIANTS:    Stateless; every method is a pure function.
 * COLLABORATORS: Used throughout the sim rules.
 */
public final class IntMath {
    private IntMath() {
    }

    /**
     * REQUIRES: n >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns floor(sqrt(n)) exactly, using integer Newton iteration. Throws
     *           IllegalArgumentException if n < 0.
     */
    public static long isqrt(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("isqrt of negative number: " + n);
        }
        if (n < 2) {
            return n;
        }
        long x = n;
        // x + n / x can exceed Long.MAX_VALUE by one; as an unsigned value it is still correct, so use >>> 1.
        long y = (x + n / x) >>> 1;
        while (y < x) {
            x = y;
            y = (x + n / x) >>> 1;
        }
        return x;
    }

    /**
     * REQUIRES: lo <= hi
     * MODIFIES: nothing
     * EFFECTS:  Returns v limited to the range [lo, hi].
     */
    public static int clamp(int v, int lo, int hi) {
        if (v < lo) {
            return lo;
        }
        return Math.min(v, hi);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns floor(value * pct / 100), computed without int overflow. Rounds toward negative
     *           infinity for negative results.
     */
    public static int percentOf(int value, int pct) {
        return (int) Math.floorDiv((long) value * (long) pct, 100L);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns floor(value * bp / 10000), where bp is in basis points (1 bp = 0.01%), computed without
     *           int overflow. Rounds toward negative infinity for negative results.
     */
    public static int basisPointsOf(int value, int bp) {
        return (int) Math.floorDiv((long) value * (long) bp, 10_000L);
    }
}
