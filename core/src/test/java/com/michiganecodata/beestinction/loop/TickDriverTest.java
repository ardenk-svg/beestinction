package com.michiganecodata.beestinction.loop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickDriverTest {
    private static final int TICKS_PER_SECOND = 4;
    private static final int MAX_CATCH_UP = 12;
    private static final int SKIP_BATCH = 200;
    private static final float FRAME = 1f / 60f;

    /** A fake game: counts ticks; spring, game over, and a popup start at configurable tick counts. */
    private static final class FakeTarget implements TickTarget {
        int ticks;
        int springAt = Integer.MAX_VALUE;
        int gameOverAt = Integer.MAX_VALUE;
        int popupAt = Integer.MAX_VALUE;
        boolean popupDismissed;

        @Override
        public void tick() {
            if (isGameOver()) {
                throw new AssertionError("ticked after game over");
            }
            ticks++;
        }

        @Override
        public boolean isGameOver() {
            return ticks >= gameOverAt;
        }

        @Override
        public boolean hasPendingPause() {
            return ticks >= popupAt && !popupDismissed;
        }

        @Override
        public boolean isSpring() {
            return ticks >= springAt;
        }
    }

    private static TickDriver driver(FakeTarget t) {
        return new TickDriver(t, TICKS_PER_SECOND, MAX_CATCH_UP, SKIP_BATCH);
    }

    @Test
    void speedCycles1x2x3xThenBackTo1x() {
        TickDriver d = driver(new FakeTarget());
        assertEquals(SpeedSetting.X1, d.speed());
        assertEquals(SpeedSetting.X2, d.cycleSpeed());
        assertEquals(SpeedSetting.X3, d.cycleSpeed());
        assertEquals(SpeedSetting.X1, d.cycleSpeed());
    }

    @Test
    void runsFourTicksPerSecondTimesTheSpeed() {
        FakeTarget t = new FakeTarget();
        TickDriver d = driver(t);
        d.update(0.25f);
        assertEquals(1, t.ticks);
        d.update(0.125f);
        d.update(0.125f);
        assertEquals(2, t.ticks);
        d.cycleSpeed(); // 2x
        d.update(0.25f);
        assertEquals(4, t.ticks);
        d.cycleSpeed(); // 3x
        d.update(0.25f);
        assertEquals(7, t.ticks);
    }

    @Test
    void smallFramesAddUpOverTime() {
        FakeTarget t = new FakeTarget();
        TickDriver d = driver(t);
        for (int i = 0; i < 600; i++) { // 10 seconds at 60 fps
            d.update(FRAME);
        }
        assertTrue(t.ticks >= 39 && t.ticks <= 40, "ran " + t.ticks);
    }

    @Test
    void pausedRunsZeroTicks() {
        FakeTarget t = new FakeTarget();
        TickDriver d = driver(t);
        d.setPaused(true);
        assertTrue(d.isPaused());
        d.update(10f);
        assertEquals(0, t.ticks);
        d.setPaused(false);
        d.update(0.25f);
        assertEquals(1, t.ticks, "time spent paused is not owed afterwards");
    }

    @Test
    void catchUpIsCappedPerFrame() {
        FakeTarget t = new FakeTarget();
        TickDriver d = driver(t);
        d.update(60f); // a minute in a hidden tab
        assertEquals(MAX_CATCH_UP, t.ticks);
        assertEquals(MAX_CATCH_UP, d.ticksLastUpdate());
        d.update(FRAME);
        assertTrue(t.ticks <= MAX_CATCH_UP + 1, "the backlog was dropped, not carried over");
    }

    @Test
    void noTicksWhileAPopupIsPending() {
        FakeTarget t = new FakeTarget();
        t.popupAt = 2;
        TickDriver d = driver(t);
        d.update(5f);
        assertEquals(2, t.ticks, "stops right after the tick that opened the popup");
        d.update(5f);
        assertEquals(2, t.ticks);
        t.popupDismissed = true;
        d.update(0.25f);
        assertEquals(3, t.ticks);
    }

    @Test
    void stopsAtGameOver() {
        FakeTarget t = new FakeTarget();
        t.gameOverAt = 3;
        TickDriver d = driver(t);
        d.update(10f);
        assertEquals(3, t.ticks);
        d.update(10f);
        assertEquals(3, t.ticks);
    }

    @Test
    void skipRunsBatchesAndStopsAtSpring() {
        FakeTarget t = new FakeTarget();
        t.springAt = 450;
        TickDriver d = driver(t);
        d.setPaused(true);
        d.startSkip();
        assertTrue(d.isSkipping());
        assertFalse(d.isPaused(), "starting a skip unpauses");
        d.update(FRAME);
        assertEquals(SKIP_BATCH, t.ticks);
        d.update(FRAME);
        assertEquals(2 * SKIP_BATCH, t.ticks);
        d.update(FRAME);
        assertEquals(450, t.ticks);
        assertFalse(d.isSkipping());
        d.update(0.25f);
        assertEquals(451, t.ticks, "normal speed again after the skip");
    }

    @Test
    void skipStopsAtGameOver() {
        FakeTarget t = new FakeTarget();
        t.gameOverAt = 250;
        TickDriver d = driver(t);
        d.startSkip();
        for (int i = 0; i < 5; i++) {
            d.update(FRAME);
        }
        assertEquals(250, t.ticks);
        assertFalse(d.isSkipping());
    }

    @Test
    void skipStopsAtAPendingPopup() {
        FakeTarget t = new FakeTarget();
        t.popupAt = 120;
        TickDriver d = driver(t);
        d.startSkip();
        d.update(FRAME);
        assertEquals(120, t.ticks);
        assertFalse(d.isSkipping());
    }

    @Test
    void skipDoesNotStartAfterGameOver() {
        FakeTarget t = new FakeTarget();
        t.gameOverAt = 0;
        TickDriver d = driver(t);
        d.startSkip();
        assertFalse(d.isSkipping());
    }

    @Test
    void alphaStaysInZeroToOne() {
        FakeTarget t = new FakeTarget();
        TickDriver d = driver(t);
        float[] deltas = {0f, FRAME, 0.1f, 0.249f, 0.25f, 0.26f, 1f, 3.3f, 100f, -1f, Float.NaN};
        for (int speed = 0; speed < 3; speed++) {
            for (float delta : deltas) {
                d.update(delta);
                float a = d.interpolationAlpha();
                assertTrue(a >= 0f && a < 1f, "alpha " + a + " after delta " + delta);
            }
            d.cycleSpeed();
        }
        TickDriver fresh = driver(new FakeTarget());
        fresh.update(0.125f); // half a tick at 1x
        assertEquals(0.5f, fresh.interpolationAlpha(), 0.001f);
    }
}
