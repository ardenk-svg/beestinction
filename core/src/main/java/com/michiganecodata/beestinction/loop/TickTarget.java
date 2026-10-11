package com.michiganecodata.beestinction.loop;

/**
 * OVERVIEW:      What a TickDriver drives. In the game this is an adapter over Simulation (see GameScreen); in
 *                tests it is a fake. Keeping it an interface keeps TickDriver free of libGDX and the sim.
 * INVARIANTS:    none
 * COLLABORATORS: TickDriver, GameScreen, TickDriverTest.
 */
public interface TickTarget {

    /** Runs one simulation tick. */
    void tick();

    /** True once the game has ended (no more ticks should run). */
    boolean isGameOver();

    /** True while a popup that pauses the game is waiting for the player. */
    boolean hasPendingPause();

    /** True if the season is spring (ends a skip-to-spring fast-forward). */
    boolean isSpring();
}
