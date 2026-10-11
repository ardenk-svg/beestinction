package com.michiganecodata.beestinction.sim.config;

import com.michiganecodata.beestinction.sim.Season;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * OVERVIEW:      One flower species, as data (composition, not subclasses). Loaded from assets/data/flowers.json
 *                by FlowerTypeLoader in core, or built in code by TestFixtures.
 * INVARIANTS:    Immutable. id and displayName are non-empty; bloomSeasons is non-empty;
 *                nectarPerDayPerSize >= 0; potential is non-null. factId may be null.
 *                Two FlowerTypes are equal if and only if their ids are equal.
 * COLLABORATORS: FlowerPatch (holds one), DiscoveryLog, Season, NectarPotential.
 */
public final class FlowerType {
    private final String id;
    private final String displayName;
    private final EnumSet<Season> bloomSeasons;
    private final int nectarPerDayPerSize;
    private final NectarPotential potential;
    private final String factId;

    /**
     * REQUIRES: id and displayName non-empty, bloomSeasons non-empty, nectarPerDayPerSize >= 0,
     *           potential != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a species (bloomSeasons is copied). factId may be null.
     *           Throws IllegalArgumentException if the REQUIRES clause is broken.
     */
    public FlowerType(String id, String displayName, Set<Season> bloomSeasons, int nectarPerDayPerSize,
                      NectarPotential potential, String factId) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("flower id must be non-empty");
        }
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("flower displayName must be non-empty: " + id);
        }
        if (bloomSeasons == null || bloomSeasons.isEmpty()) {
            throw new IllegalArgumentException("flower must bloom in at least one season: " + id);
        }
        if (nectarPerDayPerSize < 0) {
            throw new IllegalArgumentException("nectarPerDayPerSize must be >= 0: " + id);
        }
        this.id = id;
        this.displayName = displayName;
        this.bloomSeasons = EnumSet.copyOf(bloomSeasons);
        this.nectarPerDayPerSize = nectarPerDayPerSize;
        this.potential = Objects.requireNonNull(potential, "potential");
        this.factId = factId;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    /** Returns an unmodifiable view of the seasons this species blooms in (in Season order). */
    public Set<Season> bloomSeasons() {
        return Collections.unmodifiableSet(bloomSeasons);
    }

    /** Returns true if this species blooms in season s. */
    public boolean bloomsIn(Season s) {
        return bloomSeasons.contains(s);
    }

    /** Nectar a patch of size 1 offers per day (before difficulty yield adjustments). */
    public int nectarPerDayPerSize() {
        return nectarPerDayPerSize;
    }

    public NectarPotential potential() {
        return potential;
    }

    /** Returns the fact ID shown on the discovery popup, or null. */
    public String factId() {
        return factId;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof FlowerType && id.equals(((FlowerType) o).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "FlowerType[" + id + "]";
    }
}
