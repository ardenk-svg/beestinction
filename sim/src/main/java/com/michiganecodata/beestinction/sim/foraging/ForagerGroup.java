package com.michiganecodata.beestinction.sim.foraging;

import com.michiganecodata.beestinction.sim.colony.AgeHistogram;
import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.TilePos;

import java.util.Objects;

/**
 * OVERVIEW:      A group of foragers away from the hive on one trip (or on a standing order that repeats trips).
 *                It carries its own age cohorts, keeps aging while away, and can lose bees on the way.
 *                Positions are fixed-point milli-tiles (1 tile = 1000). The renderer draws the group at a point
 *                between prev and current position (interpolation); Simulation copies the current position into
 *                prev at the start of each tick's foraging step (snapshotPosition), so ForagingSystem only sets
 *                the new current position.
 *                This class is plain data (getters and setters); the trip rules live in ForagingSystem.
 * INVARIANTS:    id > 0 and unique among the groups of a run; bees and target are non-null; targetPatch is
 *                null when scouting an empty tile; nectarCarried >= 0; winterTicksOutside >= 0;
 *                0 <= travelTicksDone <= travelTicksTotal. A group with no bees must be removed from GameState.
 * COLLABORATORS: AgeHistogram, TilePos, FlowerPatch, GroupState, ForagingSystem, GameState, orders,
 *                GroupRenderer (core).
 */
public final class ForagerGroup {
    private final int id;
    private final AgeHistogram bees;
    private final TilePos target;
    private final FlowerPatch targetPatch;
    private GroupState state;
    private boolean standing;
    private boolean cancelRequested;
    private int nectarCarried;
    private int winterTicksOutside;
    private int posXMilli;
    private int posYMilli;
    private int prevXMilli;
    private int prevYMilli;
    private int travelTicksTotal;
    private int travelTicksDone;

    /**
     * REQUIRES: id > 0; bees, target, home != null; targetPatch is null or located at target
     * MODIFIES: nothing
     * EFFECTS:  Creates a group leaving the hive: state OUTBOUND, positioned at the center of the home tile
     *           (current and previous position equal), carrying no nectar, with no travel time set yet.
     */
    public ForagerGroup(int id, AgeHistogram bees, TilePos target, FlowerPatch targetPatch, boolean standing,
                        TilePos home) {
        if (id <= 0) {
            throw new IllegalArgumentException("group id must be positive: " + id);
        }
        this.id = id;
        this.bees = Objects.requireNonNull(bees, "bees");
        this.target = Objects.requireNonNull(target, "target");
        this.targetPatch = targetPatch;
        this.standing = standing;
        this.state = GroupState.OUTBOUND;
        Objects.requireNonNull(home, "home");
        this.posXMilli = home.centerXMilli();
        this.posYMilli = home.centerYMilli();
        this.prevXMilli = posXMilli;
        this.prevYMilli = posYMilli;
    }

    public int id() {
        return id;
    }

    /** The bees in this group, by age. */
    public AgeHistogram bees() {
        return bees;
    }

    /** The tile the group is flying to. */
    public TilePos target() {
        return target;
    }

    /** The patch the group is foraging, or null when scouting an empty tile. */
    public FlowerPatch targetPatch() {
        return targetPatch;
    }

    public GroupState state() {
        return state;
    }

    public void setState(GroupState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    /** True for a standing order: the group repeats trips until cancelled. */
    public boolean isStanding() {
        return standing;
    }

    public void setStanding(boolean standing) {
        this.standing = standing;
    }

    /** True once the player cancelled the standing order: finish this trip, then stop. */
    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public void setCancelRequested(boolean cancelRequested) {
        this.cancelRequested = cancelRequested;
    }

    public int nectarCarried() {
        return nectarCarried;
    }

    public void setNectarCarried(int nectarCarried) {
        this.nectarCarried = nectarCarried;
    }

    /** Ticks this group has spent outside the hive during winter. */
    public int winterTicksOutside() {
        return winterTicksOutside;
    }

    public void setWinterTicksOutside(int winterTicksOutside) {
        this.winterTicksOutside = winterTicksOutside;
    }

    public int posXMilli() {
        return posXMilli;
    }

    public int posYMilli() {
        return posYMilli;
    }

    public int prevXMilli() {
        return prevXMilli;
    }

    public int prevYMilli() {
        return prevYMilli;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Sets the current position (milli-tiles). The previous position is not changed.
     */
    public void setPosition(int xMilli, int yMilli) {
        this.posXMilli = xMilli;
        this.posYMilli = yMilli;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Copies the current position into the previous position. Called by Simulation once per tick,
     *           before ForagingSystem.onTick, so the renderer can interpolate between the two.
     */
    public void snapshotPosition() {
        this.prevXMilli = posXMilli;
        this.prevYMilli = posYMilli;
    }

    /** Ticks the current leg (out or back) takes in total. */
    public int travelTicksTotal() {
        return travelTicksTotal;
    }

    public void setTravelTicksTotal(int travelTicksTotal) {
        this.travelTicksTotal = travelTicksTotal;
    }

    /** Ticks of the current leg already flown. */
    public int travelTicksDone() {
        return travelTicksDone;
    }

    public void setTravelTicksDone(int travelTicksDone) {
        this.travelTicksDone = travelTicksDone;
    }

    @Override
    public String toString() {
        return "ForagerGroup[" + id + " " + state + " -> " + target + (standing ? " standing" : "") + "]";
    }
}
