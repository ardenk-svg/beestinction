package com.michiganecodata.beestinction.data;

import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the real data files in assets/data parse (with the same loaders the game uses) and that the
 * placeholder balance values match BalanceConfig.defaults(), so tests and the game agree. Uses libGDX's JSON
 * parser only (no window or GL needed).
 */
class DataFilesTest {

    private static String read(String relative) throws IOException {
        Path p = Paths.get(System.getProperty("beestinction.assetsDir", "../assets"), relative);
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    void balanceJsonParsesAndMatchesTheDefaults() throws IOException {
        BalanceConfig fromFile = BalanceLoader.parse(read("data/balance.json"));
        assertEquals(BalanceConfig.defaults(), fromFile,
                "assets/data/balance.json and BalanceConfig.defaults() disagree. If you tuned a value on purpose, "
                        + "update both (and the README), or change this test.");
    }

    @Test
    void flowersJsonParses() throws IOException {
        List<FlowerType> flowers = FlowerTypeLoader.parse(read("data/flowers.json"));
        assertEquals(7, flowers.size());
        FlowerType clover = null;
        for (FlowerType f : flowers) {
            if (f.id().equals("clover")) {
                clover = f;
            }
        }
        assertTrue(clover != null && clover.bloomsIn(Season.SPRING) && clover.bloomsIn(Season.SUMMER));
    }

    @Test
    void loadersReportBadData() {
        assertThrows(IllegalArgumentException.class, () -> BalanceLoader.parse("{\"ticksPerDay\": 35}"));
        assertThrows(IllegalArgumentException.class, () -> BalanceLoader.parse("{\"noSuchKey\": 1}"));
        assertThrows(IllegalArgumentException.class, () -> FlowerTypeLoader.parse(
                "{\"flowers\":[{\"id\":\"x\",\"displayName\":\"X\",\"bloomSeasons\":[\"MONSOON\"],"
                        + "\"nectarPerDayPerSize\":1,\"potential\":\"LOW\",\"factId\":null}]}"));
    }
}
