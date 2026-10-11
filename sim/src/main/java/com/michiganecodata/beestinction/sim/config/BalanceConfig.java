package com.michiganecodata.beestinction.sim.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * OVERVIEW:      Every tunable number in the game (CLAUDE.md D.7), plus one DifficultyProfile per difficulty
 *                level. Nothing tunable is hard-coded in the rules: they read it from here.
 *                At runtime the values come from assets/data/balance.json (BalanceLoader in core). defaults()
 *                returns the D.7 placeholders, which tests use. The values are placeholders and are NOT tuned.
 * INVARIANTS:    Immutable. Every Key has a value >= 0; ticksPerSecond, ticksPerDay, daysPerSeason, and
 *                lifespanDays are > 0; every min <= its max; forageAgeDays < lifespanDays; there is a profile
 *                for every DifficultyLevel.
 * COLLABORATORS: Key (the list of names), DifficultyProfile, Builder, BalanceLoader (core), TestFixtures.
 */
public final class BalanceConfig {

    /**
     * OVERVIEW:      The name (as used in balance.json) and D.7 placeholder of every tunable value.
     *                Declaration order matches the D.7 table.
     * INVARIANTS:    jsonName values are unique.
     * COLLABORATORS: BalanceConfig, BalanceLoader (core).
     */
    public enum Key {
        TICKS_PER_SECOND("ticksPerSecond", 4, true),
        TICKS_PER_DAY("ticksPerDay", 35, true),
        DAYS_PER_SEASON("daysPerSeason", 28, true),
        LIFESPAN_DAYS("lifespanDays", 42, true),
        FORAGE_AGE_DAYS("forageAgeDays", 21, false),
        STARTING_WORKERS("startingWorkers", 50, false),
        STARTING_NECTAR("startingNectar", 500, false),
        QUEEN_DAILY_RATION("queenDailyRation", 5, false),
        WORKER_DAILY_RATION("workerDailyRation", 1, false),
        QUEEN_STARVATION_DAYS("queenStarvationDays", 2, true),
        UNFED_WORKER_DEATH_PERCENT("unfedWorkerDeathPercent", 20, false),
        EGGS_PER_DAY_BASE("eggsPerDayBase", 10, false),
        EGGS_PER_DAY_PER_FEEDING_LEVEL("eggsPerDayPerFeedingLevel", 5, false),
        MAX_FEEDING_LEVEL("maxFeedingLevel", 3, false),
        RAISE_COST_PER_BEE("raiseCostPerBee", 4, false),
        BROOD_THRESHOLD_POINTS("broodThresholdPoints", 700, false),
        BROOD_BASE_POINTS_PER_TICK("broodBasePointsPerTick", 1, false),
        BROOD_NURSE_BONUS_PER_100_NURSES("broodNurseBonusPer100Nurses", 1, false),
        COMB_START_SIZE("combStartSize", 1, false),
        COMB_POPULATION_PER_SIZE("combPopulationPerSize", 200, false),
        COMB_NECTAR_PER_SIZE("combNectarPerSize", 1000, false),
        COMB_EXPANSION_NECTAR_COST("combExpansionNectarCost", 800, false),
        COMB_EXPANSION_BEE_TICKS("combExpansionBeeTicks", 3500, false),
        MILLI_TILES_PER_TICK("milliTilesPerTick", 50, false),
        FOG_REVEAL_RADIUS_TILES("fogRevealRadiusTiles", 3, false),
        STARTING_REVEAL_RADIUS_TILES("startingRevealRadiusTiles", 6, false),
        YIELD_RANDOM_PERCENT("yieldRandomPercent", 10, false),
        PESTICIDE_KILL_PERCENT_MIN("pesticideKillPercentMin", 70, true),
        PESTICIDE_KILL_PERCENT_MAX("pesticideKillPercentMax", 80, true),
        PESTICIDE_TOXIC_DAYS("pesticideToxicDays", 14, true),
        DISEASE_KILL_PERCENT_MIN("diseaseKillPercentMin", 40, true),
        DISEASE_KILL_PERCENT_MAX("diseaseKillPercentMax", 70, true),
        GRACE_PERIOD_DAYS("gracePeriodDays", 14, false),
        CONVERSION_WINDOW_START_DAY_OF_FALL("conversionWindowStartDayOfFall", 14, false),
        CONVERSION_VALUE_PERCENT_MIN("conversionValuePercentMin", 10, true),
        CONVERSION_VALUE_PERCENT_MAX("conversionValuePercentMax", 30, true),
        FREEZE_AFTER_WINTER_DAYS("freezeAfterWinterDays", 2, true),
        WINTER_DIE_OFF_DAY("winterDieOffDay", 4, true),
        SKIP_TICKS_PER_FRAME("skipTicksPerFrame", 200, false),
        MAX_CATCH_UP_TICKS_PER_FRAME("maxCatchUpTicksPerFrame", 12, false);

        private final String jsonName;
        private final int placeholder;
        private final boolean fixedByDesign;

        Key(String jsonName, int placeholder, boolean fixedByDesign) {
            this.jsonName = jsonName;
            this.placeholder = placeholder;
            this.fixedByDesign = fixedByDesign;
        }

        /** The key's name in balance.json, for example "ticksPerDay". */
        public String jsonName() {
            return jsonName;
        }

        /** The D.7 placeholder value. */
        public int placeholder() {
            return placeholder;
        }

        /** True if D.7 marks this value "Fixed by design" (do not tune it). */
        public boolean isFixedByDesign() {
            return fixedByDesign;
        }

        /**
         * REQUIRES: nothing
         * MODIFIES: nothing
         * EFFECTS:  Returns the Key whose jsonName equals name, or null if there is none.
         */
        public static Key fromJsonName(String name) {
            for (Key k : values()) {
                if (k.jsonName.equals(name)) {
                    return k;
                }
            }
            return null;
        }
    }

    private final int[] values;
    private final EnumMap<DifficultyLevel, DifficultyProfile> difficulty;

    private BalanceConfig(int[] values, EnumMap<DifficultyLevel, DifficultyProfile> difficulty) {
        this.values = values;
        this.difficulty = difficulty;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a config holding the D.7 placeholder values and difficulty profiles.
     */
    public static BalanceConfig defaults() {
        return defaultsBuilder().build();
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a new Builder pre-filled with the D.7 placeholders.
     */
    public static Builder defaultsBuilder() {
        Builder b = new Builder();
        for (Key k : Key.values()) {
            b.set(k, k.placeholder());
        }
        b.difficulty(DifficultyLevel.EASY, new DifficultyProfile(100, 5, 110, 50));
        b.difficulty(DifficultyLevel.MEDIUM, new DifficultyProfile(150, 8, 100, 100));
        b.difficulty(DifficultyLevel.HARD, new DifficultyProfile(200, 12, 85, 150));
        return b;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a Builder pre-filled with this config's values, for making a modified copy.
     */
    public Builder toBuilder() {
        Builder b = new Builder();
        for (Key k : Key.values()) {
            b.set(k, get(k));
        }
        for (Map.Entry<DifficultyLevel, DifficultyProfile> e : difficulty.entrySet()) {
            b.difficulty(e.getKey(), e.getValue());
        }
        return b;
    }

    /** Returns the value for key k. */
    public int get(Key k) {
        return values[k.ordinal()];
    }

    /** Returns the base profile for a difficulty level. */
    public DifficultyProfile difficultyProfile(DifficultyLevel level) {
        return difficulty.get(level);
    }

    // ----- Named getters, one per D.7 key -----

    public int ticksPerSecond() { return get(Key.TICKS_PER_SECOND); }
    public int ticksPerDay() { return get(Key.TICKS_PER_DAY); }
    public int daysPerSeason() { return get(Key.DAYS_PER_SEASON); }
    public int lifespanDays() { return get(Key.LIFESPAN_DAYS); }
    public int forageAgeDays() { return get(Key.FORAGE_AGE_DAYS); }
    public int startingWorkers() { return get(Key.STARTING_WORKERS); }
    public int startingNectar() { return get(Key.STARTING_NECTAR); }
    public int queenDailyRation() { return get(Key.QUEEN_DAILY_RATION); }
    public int workerDailyRation() { return get(Key.WORKER_DAILY_RATION); }
    public int queenStarvationDays() { return get(Key.QUEEN_STARVATION_DAYS); }
    public int unfedWorkerDeathPercent() { return get(Key.UNFED_WORKER_DEATH_PERCENT); }
    public int eggsPerDayBase() { return get(Key.EGGS_PER_DAY_BASE); }
    public int eggsPerDayPerFeedingLevel() { return get(Key.EGGS_PER_DAY_PER_FEEDING_LEVEL); }
    public int maxFeedingLevel() { return get(Key.MAX_FEEDING_LEVEL); }
    public int raiseCostPerBee() { return get(Key.RAISE_COST_PER_BEE); }
    public int broodThresholdPoints() { return get(Key.BROOD_THRESHOLD_POINTS); }
    public int broodBasePointsPerTick() { return get(Key.BROOD_BASE_POINTS_PER_TICK); }
    public int broodNurseBonusPer100Nurses() { return get(Key.BROOD_NURSE_BONUS_PER_100_NURSES); }
    public int combStartSize() { return get(Key.COMB_START_SIZE); }
    public int combPopulationPerSize() { return get(Key.COMB_POPULATION_PER_SIZE); }
    public int combNectarPerSize() { return get(Key.COMB_NECTAR_PER_SIZE); }
    public int combExpansionNectarCost() { return get(Key.COMB_EXPANSION_NECTAR_COST); }
    public int combExpansionBeeTicks() { return get(Key.COMB_EXPANSION_BEE_TICKS); }
    public int milliTilesPerTick() { return get(Key.MILLI_TILES_PER_TICK); }
    public int fogRevealRadiusTiles() { return get(Key.FOG_REVEAL_RADIUS_TILES); }
    public int startingRevealRadiusTiles() { return get(Key.STARTING_REVEAL_RADIUS_TILES); }
    public int yieldRandomPercent() { return get(Key.YIELD_RANDOM_PERCENT); }
    public int pesticideKillPercentMin() { return get(Key.PESTICIDE_KILL_PERCENT_MIN); }
    public int pesticideKillPercentMax() { return get(Key.PESTICIDE_KILL_PERCENT_MAX); }
    public int pesticideToxicDays() { return get(Key.PESTICIDE_TOXIC_DAYS); }
    public int diseaseKillPercentMin() { return get(Key.DISEASE_KILL_PERCENT_MIN); }
    public int diseaseKillPercentMax() { return get(Key.DISEASE_KILL_PERCENT_MAX); }
    public int gracePeriodDays() { return get(Key.GRACE_PERIOD_DAYS); }
    public int conversionWindowStartDayOfFall() { return get(Key.CONVERSION_WINDOW_START_DAY_OF_FALL); }
    public int conversionValuePercentMin() { return get(Key.CONVERSION_VALUE_PERCENT_MIN); }
    public int conversionValuePercentMax() { return get(Key.CONVERSION_VALUE_PERCENT_MAX); }
    public int freezeAfterWinterDays() { return get(Key.FREEZE_AFTER_WINTER_DAYS); }
    public int winterDieOffDay() { return get(Key.WINTER_DIE_OFF_DAY); }
    public int skipTicksPerFrame() { return get(Key.SKIP_TICKS_PER_FRAME); }
    public int maxCatchUpTicksPerFrame() { return get(Key.MAX_CATCH_UP_TICKS_PER_FRAME); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BalanceConfig)) {
            return false;
        }
        BalanceConfig c = (BalanceConfig) o;
        return java.util.Arrays.equals(values, c.values) && difficulty.equals(c.difficulty);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Arrays.hashCode(values) + difficulty.hashCode();
    }

    /**
     * OVERVIEW:      Mutable builder for BalanceConfig. Start from BalanceConfig.defaultsBuilder() (D.7 values)
     *                or new Builder() (nothing set), set values, then build().
     * INVARIANTS:    none until build() validates.
     * COLLABORATORS: BalanceConfig, BalanceLoader (core).
     */
    public static final class Builder {
        private final int[] values = new int[Key.values().length];
        private final boolean[] isSet = new boolean[Key.values().length];
        private final EnumMap<DifficultyLevel, DifficultyProfile> difficulty = new EnumMap<>(DifficultyLevel.class);

        /**
         * REQUIRES: k != null
         * MODIFIES: this
         * EFFECTS:  Sets the value for k. Returns this.
         */
        public Builder set(Key k, int value) {
            values[k.ordinal()] = value;
            isSet[k.ordinal()] = true;
            return this;
        }

        /**
         * REQUIRES: jsonName is the name of a Key
         * MODIFIES: this
         * EFFECTS:  Sets the value for the Key named jsonName. Throws IllegalArgumentException for unknown names.
         */
        public Builder set(String jsonName, int value) {
            Key k = Key.fromJsonName(jsonName);
            if (k == null) {
                throw new IllegalArgumentException("unknown balance key: " + jsonName);
            }
            return set(k, value);
        }

        /**
         * REQUIRES: level, profile != null
         * MODIFIES: this
         * EFFECTS:  Sets the profile for a difficulty level. Returns this.
         */
        public Builder difficulty(DifficultyLevel level, DifficultyProfile profile) {
            if (level == null || profile == null) {
                throw new IllegalArgumentException("level and profile must be non-null");
            }
            difficulty.put(level, profile);
            return this;
        }

        /**
         * REQUIRES: nothing
         * MODIFIES: nothing
         * EFFECTS:  Returns the jsonNames of keys that have not been set, in D.7 order.
         */
        public List<String> missingKeys() {
            List<String> missing = new ArrayList<>();
            for (Key k : Key.values()) {
                if (!isSet[k.ordinal()]) {
                    missing.add(k.jsonName());
                }
            }
            return Collections.unmodifiableList(missing);
        }

        /**
         * REQUIRES: nothing
         * MODIFIES: nothing
         * EFFECTS:  Returns the finished config. Throws IllegalArgumentException (naming the problem) if a key or
         *           profile is missing or a value breaks the BalanceConfig invariants.
         */
        public BalanceConfig build() {
            List<String> missing = missingKeys();
            if (!missing.isEmpty()) {
                throw new IllegalArgumentException("balance config is missing keys: " + missing);
            }
            for (DifficultyLevel level : DifficultyLevel.values()) {
                if (!difficulty.containsKey(level)) {
                    throw new IllegalArgumentException("balance config is missing difficulty profile: " + level);
                }
            }
            for (Key k : Key.values()) {
                if (values[k.ordinal()] < 0) {
                    throw new IllegalArgumentException(k.jsonName() + " must be >= 0");
                }
            }
            requirePositive(Key.TICKS_PER_SECOND);
            requirePositive(Key.TICKS_PER_DAY);
            requirePositive(Key.DAYS_PER_SEASON);
            requirePositive(Key.LIFESPAN_DAYS);
            requireMinMax(Key.PESTICIDE_KILL_PERCENT_MIN, Key.PESTICIDE_KILL_PERCENT_MAX);
            requireMinMax(Key.DISEASE_KILL_PERCENT_MIN, Key.DISEASE_KILL_PERCENT_MAX);
            requireMinMax(Key.CONVERSION_VALUE_PERCENT_MIN, Key.CONVERSION_VALUE_PERCENT_MAX);
            if (values[Key.FORAGE_AGE_DAYS.ordinal()] >= values[Key.LIFESPAN_DAYS.ordinal()]) {
                throw new IllegalArgumentException("forageAgeDays must be less than lifespanDays");
            }
            return new BalanceConfig(values.clone(), new EnumMap<>(difficulty));
        }

        private void requirePositive(Key k) {
            if (values[k.ordinal()] <= 0) {
                throw new IllegalArgumentException(k.jsonName() + " must be > 0");
            }
        }

        private void requireMinMax(Key min, Key max) {
            if (values[min.ordinal()] > values[max.ordinal()]) {
                throw new IllegalArgumentException(min.jsonName() + " must be <= " + max.jsonName());
            }
        }
    }
}
