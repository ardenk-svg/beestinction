package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until DiscoveryLog is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class DiscoveryLogTest {

    @Test
    void firstCollectionPerSpeciesReturnsTrueAndLaterOnesFalse() {
        DiscoveryLog log = new DiscoveryLog();
        assertTrue(log.markCollected(TestFixtures.flower("clover")));
        assertFalse(log.markCollected(TestFixtures.flower("clover")));
        assertTrue(log.markCollected(TestFixtures.flower("goldenrod")));
        assertFalse(log.markCollected(TestFixtures.flower("goldenrod")));
    }

    @Test
    void discoveredIdsAreSorted() {
        DiscoveryLog log = new DiscoveryLog();
        log.markCollected(TestFixtures.flower("pumpkin"));
        log.markCollected(TestFixtures.flower("clover"));
        log.markCollected(TestFixtures.flower("dandelion"));
        assertEquals(Arrays.asList("clover", "dandelion", "pumpkin"), new ArrayList<>(log.discoveredIds()));
    }
}
