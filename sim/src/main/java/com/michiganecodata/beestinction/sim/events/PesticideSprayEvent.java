package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.GameState;

/**
 * OVERVIEW:      A patch is sprayed with pesticide, with no warning (from a wild bee's point of view). It stays
 *                toxic for cfg.pesticideToxicDays() days; a respray resets the timer. No notification is posted
 *                when spraying: the player only learns about it from the PESTICIDE_DEATHS popup after bees die
 *                (posted by ForagingSystem).
 * INVARIANTS:    patchId names a patch of the map the event is applied to.
 * COLLABORATORS: FlowerPatch.spray, GameState, EventScheduler, ForagingSystem.
 */
public final class PesticideSprayEvent extends GameEvent {
    private final int patchId;

    /**
     * REQUIRES: patchId is the id of a patch on the map
     * MODIFIES: nothing
     * EFFECTS:  Creates a spray event for the patch.
     */
    public PesticideSprayEvent(int patchId) {
        this.patchId = patchId;
    }

    public int patchId() {
        return patchId;
    }

    @Override
    public EventType type() {
        return EventType.PESTICIDE_SPRAY;
    }

    /**
     * REQUIRES: s != null
     * MODIFIES: the sprayed FlowerPatch
     * EFFECTS:  Sprays the patch at the current tick: it becomes toxic for
     *           cfg.pesticideToxicDays() * cfg.ticksPerDay() ticks (a respray resets the timer).
     *           Posts no notification. Does nothing if no patch has this id.
     */
    @Override
    public void apply(GameState s) {
        // TODO(human): implement per RME above.
    }
}
