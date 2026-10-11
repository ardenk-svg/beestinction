package com.michiganecodata.beestinction.loop;

import java.util.Objects;

/**
 * OVERVIEW:      The fixed-timestep game loop (CLAUDE.md C.5). Each frame, update(delta) adds the frame's time and
 *                runs as many fixed ticks as are due: ticksPerSecond x speed per second. It caps the ticks run per
 *                frame (so coming back to a throttled browser tab doesn't freeze the game; the backlog is
 *                dropped), supports pause, and runs skip-to-spring as a fast-forward of a fixed batch of ticks
 *                per frame until spring, game over, or a popup. interpolationAlpha() tells the renderer how far
 *                we are between the last tick and the next one.
 *                Pure Java: no libGDX calls, so it is unit-tested without a window. Floats are fine here (core).
 * INVARIANTS:    0 <= pendingTicks; interpolationAlpha() is in [0, 1). No tick runs while paused, after game over,
 *                or while a popup is pending.
 * COLLABORATORS: TickTarget, SpeedSetting, GameScreen (owns one), UiGameListener (pauses it for popups).
 */
public final class TickDriver {
    private static final float MAX_ALPHA = 0.9999f;

    private final TickTarget target;
    private final int ticksPerSecond;
    private final int maxCatchUpTicksPerFrame;
    private final int skipTicksPerFrame;
    private SpeedSetting speed = SpeedSetting.X1;
    private boolean paused;
    private boolean skipping;
    /** Ticks owed, in units of ticks (so changing speed keeps the fraction). */
    private double pendingTicks;
    private int ticksLastUpdate;

    /**
     * REQUIRES: target != null; ticksPerSecond, maxCatchUpTicksPerFrame, skipTicksPerFrame > 0
     * MODIFIES: nothing
     * EFFECTS:  Creates an unpaused driver at 1x.
     */
    public TickDriver(TickTarget target, int ticksPerSecond, int maxCatchUpTicksPerFrame, int skipTicksPerFrame) {
        this.target = Objects.requireNonNull(target, "target");
        if (ticksPerSecond <= 0 || maxCatchUpTicksPerFrame <= 0 || skipTicksPerFrame <= 0) {
            throw new IllegalArgumentException("tick rates must be positive");
        }
        this.ticksPerSecond = ticksPerSecond;
        this.maxCatchUpTicksPerFrame = maxCatchUpTicksPerFrame;
        this.skipTicksPerFrame = skipTicksPerFrame;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this, the target (ticks)
     * EFFECTS:  Advances the loop by one rendered frame of deltaSeconds (negative or NaN counts as 0).
     *           - Game over: runs nothing and ends any skip.
     *           - Skipping: runs up to skipTicksPerFrame ticks, stopping (and ending the skip) at spring, game
     *             over, or a pending popup. A pause also halts the skip until unpaused.
     *           - Paused or popup pending: runs nothing.
     *           - Otherwise: runs the ticks that are due (deltaSeconds x ticksPerSecond x speed, plus any
     *             fraction left from earlier frames), at most maxCatchUpTicksPerFrame; a larger backlog is
     *             dropped. Stops early if a tick ends the game, pauses the driver, or opens a popup.
     */
    public void update(float deltaSeconds) {
        ticksLastUpdate = 0;
        if (target.isGameOver()) {
            skipping = false;
            return;
        }
        if (paused) {
            return;
        }
        if (skipping) {
            runSkipBatch();
            return;
        }
        if (target.hasPendingPause()) {
            return;
        }
        double delta = (deltaSeconds > 0) ? deltaSeconds : 0; // also rejects NaN
        pendingTicks += delta * ticksPerSecond * speed.multiplier();
        while (pendingTicks >= 1.0 && ticksLastUpdate < maxCatchUpTicksPerFrame) {
            target.tick();
            ticksLastUpdate++;
            pendingTicks -= 1.0;
            if (target.isGameOver() || paused || target.hasPendingPause()) {
                break;
            }
        }
        if (pendingTicks >= 1.0) {
            pendingTicks -= Math.floor(pendingTicks); // drop the backlog, keep the fraction for interpolation
        }
    }

    private void runSkipBatch() {
        pendingTicks = 0;
        for (int i = 0; i < skipTicksPerFrame; i++) {
            if (target.isSpring() || target.isGameOver() || target.hasPendingPause()) {
                skipping = false;
                return;
            }
            target.tick();
            ticksLastUpdate++;
            if (paused) {
                return;
            }
        }
        if (target.isSpring() || target.isGameOver() || target.hasPendingPause()) {
            skipping = false;
        }
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Moves to the next speed (1x -> 2x -> 3x -> 1x) and returns it.
     */
    public SpeedSetting cycleSpeed() {
        speed = speed.next();
        return speed;
    }

    public SpeedSetting speed() {
        return speed;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Pauses or resumes the loop.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Starts the skip-to-spring fast-forward (and unpauses). Does nothing after game over.
     *           Every tick still runs; they are just spread over frames quickly.
     */
    public void startSkip() {
        if (target.isGameOver()) {
            return;
        }
        skipping = true;
        paused = false;
    }

    public boolean isSkipping() {
        return skipping;
    }

    /** How many ticks the last update() ran. */
    public int ticksLastUpdate() {
        return ticksLastUpdate;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns how far the loop is between the last tick and the next, in [0, 1). The renderer draws
     *           groups at prev + (current - prev) * alpha.
     */
    public float interpolationAlpha() {
        float a = (float) pendingTicks;
        if (a < 0f) {
            return 0f;
        }
        return Math.min(a, MAX_ALPHA);
    }
}
