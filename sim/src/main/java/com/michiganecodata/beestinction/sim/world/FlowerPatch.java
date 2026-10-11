package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.FlowerType;

import java.util.Objects;

/**
 * OVERVIEW:      One flower patch on the map: a species (FlowerType data), a position, and a size, plus per-run
 *                state: how much nectar is left to collect today, and until which tick it is toxic from
 *                pesticide. Its daily nectar capacity is shared by every bee that forages there that day.
 * INVARIANTS:    id is unique within its map; type and pos are non-null; size > 0;
 *                0 <= nectarRemainingToday; toxicUntilTick >= 0 (0 means never sprayed).
 *                The patch is toxic at tick t exactly when t < toxicUntilTick.
 * COLLABORATORS: FlowerType, TilePos, GameMap, ForagingSystem, PesticideSprayEvent, FogOfWar.
 */
public final class FlowerPatch {
    private final int id;
    private final FlowerType type;
    private final TilePos pos;
    private final int size;
    private int nectarRemainingToday;
    private long toxicUntilTick;

    /**
     * REQUIRES: type, pos != null; size > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates a patch with no nectar available yet (resetDailyCapacity sets it each day) that has
     *           never been sprayed. Throws IllegalArgumentException if size <= 0.
     */
    public FlowerPatch(int id, FlowerType type, TilePos pos, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("patch size must be positive: " + size);
        }
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.pos = Objects.requireNonNull(pos, "pos");
        this.size = size;
        this.nectarRemainingToday = 0;
        this.toxicUntilTick = 0;
    }

    public int id() {
        return id;
    }

    public FlowerType type() {
        return type;
    }

    public TilePos pos() {
        return pos;
    }

    public int size() {
        return size;
    }

    /** Nectar still available to collect today. */
    public int nectarRemainingToday() {
        return nectarRemainingToday;
    }

    /** The first tick at which the patch is safe again (0 if it was never sprayed). */
    public long toxicUntilTick() {
        return toxicUntilTick;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: nothing
     * EFFECTS:  Returns true if this patch's species blooms in season s.
     */
    public boolean isBlooming(Season s) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: tick >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns true if the patch is still toxic at `tick`, i.e. tick < toxicUntilTick.
     */
    public boolean isToxic(long tick) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: tick >= 0, toxicTicks > 0
     * MODIFIES: this
     * EFFECTS:  Sprays the patch at `tick`: it becomes toxic until tick + toxicTicks. Spraying a patch that is
     *           already toxic resets the timer to tick + toxicTicks (a respray).
     */
    public void spray(long tick, int toxicTicks) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: s != null, yieldPercent >= 0
     * MODIFIES: this
     * EFFECTS:  Starts a new day. If the species blooms in s, today's capacity becomes
     *           type.nectarPerDayPerSize() * size * yieldPercent / 100 (floor); otherwise it becomes 0.
     *           Nectar left over from yesterday is discarded.
     */
    public void resetDailyCapacity(Season s, int yieldPercent) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: requested >= 0
     * MODIFIES: this
     * EFFECTS:  Takes up to `requested` nectar from today's remaining capacity and returns the amount actually
     *           taken (min(requested, nectarRemainingToday)). Never makes nectarRemainingToday negative.
     */
    public int take(int requested) {
        // TODO(human): implement per RME above.
        return 0;
    }

    @Override
    public String toString() {
        return "FlowerPatch[" + id + " " + type.id() + " size " + size + " at " + pos + "]";
    }
}
