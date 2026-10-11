package com.michiganecodata.beestinction.sim.config;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceConfigTest {

    @Test
    void defaultsMatchTheD7Placeholders() {
        BalanceConfig c = BalanceConfig.defaults();
        assertEquals(4, c.ticksPerSecond());
        assertEquals(35, c.ticksPerDay());
        assertEquals(28, c.daysPerSeason());
        assertEquals(42, c.lifespanDays());
        assertEquals(21, c.forageAgeDays());
        assertEquals(50, c.startingWorkers());
        assertEquals(500, c.startingNectar());
        assertEquals(2, c.queenStarvationDays());
        assertEquals(70, c.pesticideKillPercentMin());
        assertEquals(80, c.pesticideKillPercentMax());
        assertEquals(14, c.pesticideToxicDays());
        assertEquals(40, c.diseaseKillPercentMin());
        assertEquals(70, c.diseaseKillPercentMax());
        assertEquals(10, c.conversionValuePercentMin());
        assertEquals(30, c.conversionValuePercentMax());
        assertEquals(2, c.freezeAfterWinterDays());
        assertEquals(4, c.winterDieOffDay());
        assertEquals(200, c.skipTicksPerFrame());
        assertEquals(12, c.maxCatchUpTicksPerFrame());
        assertEquals(new DifficultyProfile(100, 5, 110, 50), c.difficultyProfile(DifficultyLevel.EASY));
        assertEquals(new DifficultyProfile(150, 8, 100, 100), c.difficultyProfile(DifficultyLevel.MEDIUM));
        assertEquals(new DifficultyProfile(200, 12, 85, 150), c.difficultyProfile(DifficultyLevel.HARD));
        for (BalanceConfig.Key k : BalanceConfig.Key.values()) {
            assertEquals(k.placeholder(), c.get(k), k.jsonName());
        }
    }

    @Test
    void keyNamesAreUniqueAndFindable() {
        Set<String> names = new HashSet<>();
        for (BalanceConfig.Key k : BalanceConfig.Key.values()) {
            assertTrue(names.add(k.jsonName()), "duplicate key name " + k.jsonName());
            assertSame(k, BalanceConfig.Key.fromJsonName(k.jsonName()));
        }
        assertNull(BalanceConfig.Key.fromJsonName("noSuchKey"));
    }

    @Test
    void toBuilderMakesAnEqualCopyThatCanBeChanged() {
        BalanceConfig c = BalanceConfig.defaults();
        assertEquals(c, c.toBuilder().build());
        BalanceConfig changed = c.toBuilder().set("milliTilesPerTick", 170).build();
        assertEquals(170, changed.milliTilesPerTick());
        assertEquals(50, c.milliTilesPerTick());
    }

    @Test
    void buildRejectsMissingKeysAndBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new BalanceConfig.Builder().build());
        assertThrows(IllegalArgumentException.class,
                () -> BalanceConfig.defaultsBuilder().set(BalanceConfig.Key.TICKS_PER_DAY, 0).build());
        assertThrows(IllegalArgumentException.class,
                () -> BalanceConfig.defaultsBuilder().set(BalanceConfig.Key.STARTING_NECTAR, -1).build());
        assertThrows(IllegalArgumentException.class,
                () -> BalanceConfig.defaultsBuilder().set(BalanceConfig.Key.DISEASE_KILL_PERCENT_MIN, 90).build());
        assertThrows(IllegalArgumentException.class,
                () -> BalanceConfig.defaultsBuilder().set(BalanceConfig.Key.FORAGE_AGE_DAYS, 42).build());
        assertThrows(IllegalArgumentException.class, () -> BalanceConfig.defaultsBuilder().set("nope", 1));
    }
}
