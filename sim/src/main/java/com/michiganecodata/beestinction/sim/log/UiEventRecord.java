package com.michiganecodata.beestinction.sim.log;

import java.util.Objects;

/**
 * OVERVIEW:      One logged UI input: the tick it happened at and its type. Never wall-clock time.
 * INVARIANTS:    Immutable. tick >= 0; type non-null.
 * COLLABORATORS: GameLog, UiEventType.
 */
public final class UiEventRecord {
    private final long tick;
    private final UiEventType type;

    /**
     * REQUIRES: tick >= 0, type != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the record.
     */
    public UiEventRecord(long tick, UiEventType type) {
        this.tick = tick;
        this.type = Objects.requireNonNull(type, "type");
    }

    public long tick() {
        return tick;
    }

    public UiEventType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UiEventRecord)) {
            return false;
        }
        UiEventRecord r = (UiEventRecord) o;
        return tick == r.tick && type == r.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tick, type);
    }
}
