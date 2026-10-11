package com.michiganecodata.beestinction.sim;

import com.michiganecodata.beestinction.sim.foraging.ForagerGroup;
import com.michiganecodata.beestinction.sim.log.OrderRecord;
import com.michiganecodata.beestinction.sim.log.UiEventType;
import com.michiganecodata.beestinction.sim.orders.OrderType;
import com.michiganecodata.beestinction.sim.orders.SetFeedingLevelOrder;
import com.michiganecodata.beestinction.sim.stats.RunSummary;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the wiring in Simulation (CLAUDE.md D.5): listeners, the order pipeline, notifications, game over. */
class SimulationTest {

    /** Records every callback as a string, in order. */
    private static final class RecordingListener implements GameListener {
        final List<String> events = new ArrayList<>();
        final List<Notification> notifications = new ArrayList<>();
        RunSummary summary;

        @Override
        public void onNotification(Notification n) {
            events.add("notification:" + n.type());
            notifications.add(n);
        }

        @Override
        public void onDayStart(int day) {
            events.add("day:" + day);
        }

        @Override
        public void onSeasonChanged(Season season) {
            events.add("season:" + season);
        }

        @Override
        public void onGameOver(RunSummary s) {
            events.add("gameOver");
            summary = s;
        }
    }

    @Test
    void firstTickAnnouncesSpringThenDayZero() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        RecordingListener l = new RecordingListener();
        sim.addListener(l);
        sim.tick();
        assertEquals(Arrays.asList("season:SPRING", "day:0"), l.events);
        assertEquals(1, sim.currentTick());
    }

    @Test
    void daysStartEvery35TicksAndSeasonsEvery980() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        RecordingListener l = new RecordingListener();
        sim.addListener(l);
        sim.runTicks(71);
        assertEquals(Arrays.asList("season:SPRING", "day:0", "day:1", "day:2"), l.events);

        Simulation sim2 = TestFixtures.newSimulation(TestFixtures.SEED);
        List<Season> seasons = new ArrayList<>();
        sim2.addListener(new GameListener() {
            @Override
            public void onSeasonChanged(Season season) {
                seasons.add(season);
            }
        });
        // Run a year and a tick, unless the (human-implemented) rules end the game earlier.
        int ran = sim2.runTicks(3921);
        if (ran == 3921) {
            assertEquals(Arrays.asList(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER, Season.SPRING),
                    seasons);
        }
    }

    @Test
    void ordersAreStampedWithTheCurrentTickAndLoggedWhenThatTickRuns() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.runTicks(5);
        SetFeedingLevelOrder order = new SetFeedingLevelOrder(1);
        sim.submit(order);
        assertEquals(5, order.tick());
        assertTrue(sim.log().orders().isEmpty());
        sim.tick();
        assertEquals(1, sim.log().orders().size());
        OrderRecord r = sim.log().orders().get(0);
        assertEquals(5, r.tick());
        assertEquals(OrderType.SET_FEEDING_LEVEL, r.type());
        assertEquals("{\"level\":1}", r.paramsJson());
    }

    @Test
    void uiEventsAreStampedWithTheCurrentTick() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.runTicks(7);
        sim.recordUiEvent(UiEventType.SPEED_2X);
        assertEquals(7, sim.log().uiEvents().get(0).tick());
        assertEquals(UiEventType.SPEED_2X, sim.log().uiEvents().get(0).type());
    }

    @Test
    void notificationsAreDispatchedAfterTheTickInPostOrderThenCleared() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        RecordingListener l = new RecordingListener();
        sim.addListener(l);
        sim.tick();
        l.events.clear();
        GameState state = (GameState) sim.view();
        state.postNotification(Notification.popup(NotificationType.DISEASE_OUTBREAK, "a", "first"));
        state.postNotification(Notification.popup(NotificationType.NEW_SPECIES, "b", "second"));
        assertTrue(l.notifications.isEmpty(), "listeners must not be called before the tick runs");
        sim.tick();
        assertEquals(Arrays.asList("notification:DISEASE_OUTBREAK", "notification:NEW_SPECIES"), l.events);
        assertTrue(state.pendingNotifications().isEmpty());
        sim.tick();
        assertEquals(2, l.notifications.size(), "notifications are delivered once");
    }

    @Test
    void endRunEarlyEndsTheGameAndAnnouncesItOnce() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        RecordingListener l = new RecordingListener();
        sim.addListener(l);
        sim.runTicks(40);
        l.events.clear();
        sim.endRunEarly();
        assertTrue(sim.isGameOver());
        assertEquals(Arrays.asList("notification:GAME_OVER", "gameOver"), l.events);
        assertNull(l.summary.cause());
        assertTrue(l.summary.isGameOver());
        assertSame(l.summary, sim.log().summary());
        assertSame(l.summary, sim.summary());

        sim.endRunEarly();
        long tick = sim.currentTick();
        sim.tick();
        sim.runTicks(100);
        assertEquals(tick, sim.currentTick(), "ticks after game over do nothing");
        assertEquals(Arrays.asList("notification:GAME_OVER", "gameOver"), l.events);
    }

    @Test
    void ordersSubmittedAfterGameOverAreIgnored() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.endRunEarly();
        SetFeedingLevelOrder order = new SetFeedingLevelOrder(1);
        sim.submit(order);
        assertEquals(-1, order.tick());
        assertTrue(sim.log().orders().isEmpty());
    }

    @Test
    void runTicksReturnsTheNumberOfTicksRun() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        assertEquals(10, sim.runTicks(10));
        sim.endRunEarly();
        assertEquals(0, sim.runTicks(10));
    }

    @Test
    void groupPositionsAreSnapshottedBeforeForagingMovesThem() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        GameState state = (GameState) sim.view();
        ForagerGroup g = TestFixtures.addGroup(state, 1, 30, TestFixtures.NEAR_POS, false);
        g.setPosition(12_345, 6_789);
        assertFalse(g.prevXMilli() == g.posXMilli());
        sim.tick();
        // ForagingSystem may move the group after the snapshot, but prev must be where it was before the tick.
        assertEquals(12_345, g.prevXMilli());
        assertEquals(6_789, g.prevYMilli());
    }

    @Test
    void eachRunGetsItsOwnCopyOfTheMap() {
        GameSetup setup = TestFixtures.setup(TestFixtures.SEED);
        Simulation a = Simulation.create(setup);
        Simulation b = Simulation.create(setup);
        assertFalse(a.view().map() == b.view().map());
        assertFalse(a.view().map().patches().get(0) == setup.map().patches().get(0));
    }
}
