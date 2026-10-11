package com.michiganecodata.beestinction.sim.events;

import com.michiganecodata.beestinction.sim.GameState;

/**
 * OVERVIEW:      A random event that happens to the colony (the event hierarchy). EventScheduler creates events
 *                at the start of a day, and Simulation applies them immediately.
 * INVARIANTS:    Events are created and applied within the same tick; they hold no references to UI.
 * COLLABORATORS: EventScheduler, Simulation, GameState; subclasses PesticideSprayEvent, DiseaseOutbreakEvent.
 */
public abstract class GameEvent {

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the kind of event.
     */
    public abstract EventType type();

    /**
     * REQUIRES: s != null
     * MODIFIES: s
     * EFFECTS:  Applies the event to the game.
     */
    public abstract void apply(GameState s);
}
