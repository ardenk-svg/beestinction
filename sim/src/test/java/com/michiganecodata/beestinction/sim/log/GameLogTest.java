package com.michiganecodata.beestinction.sim.log;

import com.michiganecodata.beestinction.sim.DeathCause;
import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.Simulation;
import com.michiganecodata.beestinction.sim.orders.BuildCombOrder;
import com.michiganecodata.beestinction.sim.orders.CancelStandingOrder;
import com.michiganecodata.beestinction.sim.orders.ConvertBeesOrder;
import com.michiganecodata.beestinction.sim.orders.ForageOrder;
import com.michiganecodata.beestinction.sim.orders.RecallOrder;
import com.michiganecodata.beestinction.sim.orders.SetFeedingLevelOrder;
import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import com.michiganecodata.beestinction.sim.world.TilePos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLogTest {

    /** Every key the log is allowed to contain. Adding a field to the log means consciously adding it here. */
    private static final Set<String> ALLOWED_KEYS = new HashSet<>(Arrays.asList(
            "version", "runId", "seed", "mapId", "difficulty", "orders", "uiEvents", "summary",
            "tick", "type", "accepted", "params",
            "beeCount", "x", "y", "standing", "groupId", "level",
            "score", "daysSurvived", "gameOver", "cause", "peakPopulation", "dailyPopulation", "deathsByCause",
            "totalNectarCollected", "speciesDiscovered"));

    static {
        for (DeathCause c : DeathCause.values()) {
            ALLOWED_KEYS.add(c.name());
        }
    }

    private static Simulation playShortGame() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.runTicks(3);
        sim.submit(new SetFeedingLevelOrder(1));
        sim.submit(new ForageOrder(5, TestFixtures.NEAR_POS, true));
        sim.submit(new RecallOrder(1));
        sim.submit(new CancelStandingOrder(1));
        sim.submit(new BuildCombOrder(2));
        sim.submit(new ConvertBeesOrder(3));
        sim.tick();
        sim.recordUiEvent(UiEventType.PAUSE);
        sim.recordUiEvent(UiEventType.RESUME);
        sim.runTicks(40);
        sim.endRunEarly();
        sim.recordUiEvent(UiEventType.POPUP_DISMISSED);
        return sim;
    }

    @Test
    void jsonContainsVersionSeedMapDifficultyOrdersUiEventsAndSummary() {
        Simulation sim = playShortGame();
        String json = sim.log().toJson();
        assertTrue(json.contains("\"version\":\"" + TestFixtures.VERSION + "\""), json);
        assertTrue(json.contains("\"seed\":\"" + TestFixtures.SEED + "\""), json);
        assertTrue(json.contains("\"mapId\":\"" + TestFixtures.TINY_MAP_ID + "\""), json);
        assertTrue(json.contains("\"difficulty\":\"MEDIUM\""), json);
        assertTrue(json.contains("{\"tick\":3,\"type\":\"SET_FEEDING_LEVEL\",\"accepted\":"), json);
        assertTrue(json.contains("\"params\":{\"level\":1}"), json);
        assertTrue(json.contains("\"params\":{\"beeCount\":5,\"x\":12,\"y\":10,\"standing\":true}"), json);
        assertTrue(json.contains("{\"tick\":4,\"type\":\"PAUSE\"}"), json);
        assertTrue(json.contains("\"type\":\"POPUP_DISMISSED\""), json);
        assertTrue(json.contains("\"summary\":{\"score\":"), json);
        assertEquals(new LinkedHashSet<>(Arrays.asList("version", "runId", "seed", "mapId", "difficulty", "orders",
                "uiEvents", "summary")), topLevelKeys(json));
        assertEquals(6, sim.log().orders().size());
        assertEquals(3, sim.log().uiEvents().size());
    }

    @Test
    void logHasNoPersonalFields() {
        String json = playShortGame().log().toJson();
        for (String key : allKeys(json)) {
            assertTrue(ALLOWED_KEYS.contains(key), "unexpected key in the log: \"" + key + "\"");
        }
    }

    @Test
    void runIdIsNotTheGameSeed() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        String runId = sim.log().runId();
        assertEquals(32, runId.length());
        assertNotEquals(Long.toString(TestFixtures.SEED), runId);
        assertNotEquals(Long.toHexString(TestFixtures.SEED), runId);

        // Same game seed, different run-ID seed: different run IDs (the ID does not come from the game seed).
        GameSetup a = TestFixtures.setup(TestFixtures.SEED);
        GameSetup b = new GameSetup(a.seed(), a.map(), a.difficulty(), a.balance(), a.flowerTypes(),
                a.gameVersion(), a.runIdSeed() + 1);
        assertNotEquals(new GameLog(a).runId(), new GameLog(b).runId());

        // Different game seeds, same run-ID seed: the run ID does not change with the game seed.
        assertEquals(new GameLog(a).runId(), new GameLog(a.withSeed(TestFixtures.OTHER_SEED)).runId());
    }

    @Test
    void partialLogHasNullSummary() {
        Simulation sim = TestFixtures.newSimulation(TestFixtures.SEED);
        sim.runTicks(10);
        assertTrue(sim.log().toJson().endsWith("\"summary\":null}"));
        assertFalse(sim.isGameOver());
    }

    // ----- A tiny JSON key scanner (enough for logs written by JsonWriter) -----

    private static Set<String> topLevelKeys(String json) {
        return keys(json, true);
    }

    private static Set<String> allKeys(String json) {
        return keys(json, false);
    }

    private static Set<String> keys(String json, boolean topLevelOnly) {
        Set<String> out = new LinkedHashSet<>();
        List<Character> stack = new ArrayList<>();
        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') {
                stack.add(c);
                i++;
            } else if (c == '}' || c == ']') {
                stack.remove(stack.size() - 1);
                i++;
            } else if (c == '"') {
                StringBuilder s = new StringBuilder();
                i++;
                while (json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') {
                        i++;
                    }
                    s.append(json.charAt(i));
                    i++;
                }
                i++;
                boolean isKey = i < json.length() && json.charAt(i) == ':'
                        && !stack.isEmpty() && stack.get(stack.size() - 1) == '{';
                if (isKey && (!topLevelOnly || stack.size() == 1)) {
                    out.add(s.toString());
                }
            } else {
                i++;
            }
        }
        return out;
    }

    @Test
    void scannerSanityCheck() {
        assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b")), topLevelKeys("{\"a\":{\"c\":1},\"b\":[\"x\"]}"));
        assertEquals(new LinkedHashSet<>(Arrays.asList("a", "c", "b")), allKeys("{\"a\":{\"c\":1},\"b\":[\"x\"]}"));
        assertTrue(new ForageOrder(1, new TilePos(0, 0), false).toString().contains("FORAGE"));
    }
}
