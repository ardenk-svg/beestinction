package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.config.FlowerType;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * OVERVIEW:      The flower species the colony has successfully collected nectar from at least once. The first
 *                collection of a species triggers the NEW_SPECIES popup.
 * INVARIANTS:    Species are only ever added, never removed. Iteration order is deterministic (sorted by id).
 * COLLABORATORS: ForagingSystem (marks collections), StatsRecorder (species discovered), FlowerType.
 */
public final class DiscoveryLog {
    private final TreeSet<String> discovered = new TreeSet<>();

    /**
     * REQUIRES: t != null
     * MODIFIES: this
     * EFFECTS:  Records that species t was collected. Returns true the first time a species is recorded and
     *           false every time after that.
     */
    public boolean markCollected(FlowerType t) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns an unmodifiable view of the ids of every species collected so far, sorted by id.
     */
    public Set<String> discoveredIds() {
        // TODO(human): implement per RME above.
        return Collections.emptySet();
    }
}
