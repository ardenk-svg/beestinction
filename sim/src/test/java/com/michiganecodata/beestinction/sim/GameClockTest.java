package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameClockTest {

    private static GameClock clockAt(long tick) {
        GameClock c = GameClock.fromConfig(BalanceConfig.defaults());
        for (long i = 0; i < tick; i++) {
            c.advance();
        }
        return c;
    }

    @Test
    void startsAtTickZeroInSpringOfYearZero() {
        GameClock c = clockAt(0);
        assertEquals(0, c.tick());
        assertEquals(0, c.dayOfRun());
        assertEquals(Season.SPRING, c.season());
        assertEquals(0, c.year());
    }

    @Test
    void dayBoundaryIsAt35Ticks() {
        assertEquals(0, clockAt(34).dayOfRun());
        assertEquals(34, clockAt(34).tickOfDay());
        assertEquals(1, clockAt(35).dayOfRun());
        assertEquals(0, clockAt(35).tickOfDay());
    }

    @Test
    void weekBoundaryIsAt245Ticks() {
        assertEquals(0, clockAt(244).weekOfRun());
        assertEquals(1, clockAt(245).weekOfRun());
        assertEquals(7, clockAt(245).dayOfRun());
    }

    @Test
    void seasonBoundaryIsAt980Ticks() {
        assertEquals(Season.SPRING, clockAt(979).season());
        assertEquals(27, clockAt(979).dayOfSeason());
        assertEquals(Season.SUMMER, clockAt(980).season());
        assertEquals(0, clockAt(980).dayOfSeason());
        assertEquals(980, clockAt(0).ticksPerSeason());
    }

    @Test
    void aYearIs3920Ticks() {
        assertEquals(3920, clockAt(0).ticksPerYear());
        GameClock lastTick = clockAt(3919);
        assertEquals(0, lastTick.year());
        assertEquals(Season.WINTER, lastTick.season());
        GameClock nextYear = clockAt(3920);
        assertEquals(1, nextYear.year());
        assertEquals(Season.SPRING, nextYear.season());
    }

    @Test
    void seasonsFollowTheMichiganOrderAndWrapAround() {
        assertEquals(Season.SUMMER, Season.SPRING.next());
        assertEquals(Season.FALL, Season.SUMMER.next());
        assertEquals(Season.WINTER, Season.FALL.next());
        assertEquals(Season.SPRING, Season.WINTER.next());
        Season[] expected = {Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER, Season.SPRING};
        GameClock c = clockAt(0);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], c.season(), "season " + i);
            for (int t = 0; t < 980; t++) {
                c.advance();
            }
        }
    }

    @Test
    void isDayStartAndIsSeasonStart() {
        assertTrue(clockAt(0).isDayStart());
        assertTrue(clockAt(0).isSeasonStart());
        assertFalse(clockAt(1).isDayStart());
        assertFalse(clockAt(1).isSeasonStart());
        assertTrue(clockAt(35).isDayStart());
        assertFalse(clockAt(35).isSeasonStart());
        assertTrue(clockAt(980).isDayStart());
        assertTrue(clockAt(980).isSeasonStart());
        assertFalse(clockAt(981).isSeasonStart());
    }
}
