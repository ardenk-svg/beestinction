package com.michiganecodata.beestinction.sim.testutil;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameStateView;
import com.michiganecodata.beestinction.sim.colony.AgeHistogram;
import com.michiganecodata.beestinction.sim.colony.AgeHistogramView;
import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * OVERVIEW:      Asserts the game invariants of CLAUDE.md E.4 on a GameStateView. Create one checker per game and
 *                call check() repeatedly (for example once per day): it remembers the first state it saw (for the
 *                conservation check) and the state at game over (to check nothing changes afterwards).
 *                - No negative counts anywhere (bees, brood, nectar, capacity).
 *                - Nectar <= storage capacity; population <= population capacity.
 *                - Conservation: births - deaths (all causes, including conversion) = change in total workers.
 *                - At most one group per id, and every group has bees > 0.
 *                - After game over, the state doesn't change.
 * INVARIANTS:    none
 * COLLABORATORS: GameStateView, SmokeTest.
 */
public final class InvariantChecker {
    private boolean haveBaseline;
    private long baselineWorkers;
    private long baselineBirths;
    private long baselineDeaths;
    private String frozenFingerprint;

    /**
     * REQUIRES: v != null
     * MODIFIES: this
     * EFFECTS:  Fails the current test (with the tick in the message) if any invariant is broken.
     */
    public void check(GameStateView v) {
        String at = " (tick " + v.tick() + ")";

        nonNegative("nectar", v.nectar(), at);
        nonNegative("nectar capacity", v.nectarCapacity(), at);
        nonNegative("population", v.population(), at);
        nonNegative("population capacity", v.populationCapacity(), at);
        nonNegative("workers in hive", v.workersInHive(), at);
        nonNegative("house bees", v.houseBees(), at);
        nonNegative("available foragers", v.availableForagers(), at);
        nonNegative("bees out", v.beesOut(), at);
        nonNegative("brood", v.broodCount(), at);
        nonNegative("score", v.score(), at);
        checkHistogram("hive", v.hiveWorkers(), at);

        assertTrue(v.nectar() <= v.nectarCapacity(),
                "nectar " + v.nectar() + " exceeds capacity " + v.nectarCapacity() + at);
        assertTrue(v.population() <= v.populationCapacity(),
                "population " + v.population() + " exceeds capacity " + v.populationCapacity() + at);

        Set<Integer> ids = new HashSet<>();
        for (ForagerGroup g : v.groups()) {
            assertTrue(ids.add(g.id()), "duplicate group id " + g.id() + at);
            AgeHistogram bees = g.bees();
            assertTrue(bees.total() > 0, "group " + g.id() + " has no bees but was not removed" + at);
            checkHistogram("group " + g.id(), bees, at);
            nonNegative("group " + g.id() + " nectar", g.nectarCarried(), at);
        }

        long workers = (long) v.workersInHive() + v.beesOut();
        long births = v.totalBirths();
        long deaths = (long) v.totalDeaths() - v.deaths(DeathCause.QUEEN_STARVED);
        if (!haveBaseline) {
            haveBaseline = true;
            baselineWorkers = workers;
            baselineBirths = births;
            baselineDeaths = deaths;
        } else {
            long expected = baselineWorkers + (births - baselineBirths) - (deaths - baselineDeaths);
            assertEquals(expected, workers,
                    "conservation broken: births - deaths does not match the change in workers" + at);
        }

        if (v.isGameOver()) {
            String fp = fingerprint(v);
            if (frozenFingerprint == null) {
                frozenFingerprint = fp;
            } else {
                assertEquals(frozenFingerprint, fp, "state changed after game over" + at);
            }
        }
    }

    private static void checkHistogram(String what, AgeHistogramView h, String at) {
        nonNegative(what + " dying pool", h.dying(), at);
        long sum = h.dying();
        for (int age = 0; age < h.lifespanDays(); age++) {
            int c = h.count(age);
            if (c < 0) {
                fail(what + " has " + c + " bees of age " + age + at);
            }
            sum += c;
        }
        assertEquals(sum, h.total(), what + " total() does not match the sum of its cohorts" + at);
    }

    private static void nonNegative(String what, long value, String at) {
        if (value < 0) {
            fail(what + " is negative: " + value + at);
        }
    }

    private static String fingerprint(GameStateView v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v.tick()).append('|').append(v.nectar()).append('|').append(v.nectarCapacity())
                .append('|').append(v.population()).append('|').append(v.workersInHive())
                .append('|').append(v.broodCount()).append('|').append(v.score())
                .append('|').append(v.totalBirths()).append('|').append(v.totalDeaths())
                .append('|').append(v.feedingLevel()).append('|').append(v.combSize())
                .append('|').append(v.discoveredSpeciesIds());
        for (ForagerGroup g : v.groups()) {
            sb.append("|g").append(g.id()).append(':').append(g.bees().total()).append(':')
                    .append(g.posXMilli()).append(',').append(g.posYMilli()).append(':').append(g.state());
        }
        return sb.toString();
    }
}
