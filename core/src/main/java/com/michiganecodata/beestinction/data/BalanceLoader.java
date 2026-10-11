package com.michiganecodata.beestinction.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.config.DifficultyProfile;

/**
 * OVERVIEW:      Reads assets/data/balance.json into a BalanceConfig (CLAUDE.md D.6): a flat object whose keys are
 *                the D.7 names, plus a "difficulty" object with "easy", "medium", and "hard" profiles.
 *                Parses with libGDX JsonReader/JsonValue by hand (no reflection, CLAUDE.md A.5 rule 6).
 *                Every key must be present and every value must be an integer, so the JSON file is the single
 *                source of truth at runtime; mistakes are reported with the key name.
 * INVARIANTS:    Stateless.
 * COLLABORATORS: BalanceConfig, DifficultyProfile, BeestinctionGame, DataFilesTest.
 */
public final class BalanceLoader {
    private static final String[] PROFILE_KEYS = {
            "baseMultiplierPercent", "escalationPercentPerWeek", "yieldPercent", "eventBaseBasisPoints"};

    private BalanceLoader() {
    }

    /**
     * REQUIRES: file != null and readable
     * MODIFIES: nothing
     * EFFECTS:  Returns the config in the file. Throws IllegalArgumentException (naming the problem) if the file
     *           is not valid balance JSON.
     */
    public static BalanceConfig load(FileHandle file) {
        return parse(file.readString("UTF-8"));
    }

    /**
     * REQUIRES: json != null
     * MODIFIES: nothing
     * EFFECTS:  Parses balance JSON text into a BalanceConfig. Throws IllegalArgumentException if a key is
     *           unknown or missing, a value is not an integer, or the values break BalanceConfig's invariants.
     */
    public static BalanceConfig parse(String json) {
        JsonValue root = new JsonReader().parse(json);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("balance.json must be a JSON object");
        }
        BalanceConfig.Builder b = new BalanceConfig.Builder();
        for (JsonValue child = root.child; child != null; child = child.next) {
            if ("difficulty".equals(child.name)) {
                continue;
            }
            if (BalanceConfig.Key.fromJsonName(child.name) == null) {
                throw new IllegalArgumentException("balance.json: unknown key \"" + child.name + "\"");
            }
            b.set(child.name, requireInt(child, child.name));
        }
        JsonValue difficulty = root.get("difficulty");
        if (difficulty == null || !difficulty.isObject()) {
            throw new IllegalArgumentException("balance.json: missing \"difficulty\" object");
        }
        for (DifficultyLevel level : DifficultyLevel.values()) {
            String name = level.name().toLowerCase();
            JsonValue p = difficulty.get(name);
            if (p == null || !p.isObject()) {
                throw new IllegalArgumentException("balance.json: missing difficulty profile \"" + name + "\"");
            }
            int[] v = new int[PROFILE_KEYS.length];
            for (int i = 0; i < PROFILE_KEYS.length; i++) {
                JsonValue field = p.get(PROFILE_KEYS[i]);
                if (field == null) {
                    throw new IllegalArgumentException("balance.json: difficulty." + name + " is missing \""
                            + PROFILE_KEYS[i] + "\"");
                }
                v[i] = requireInt(field, "difficulty." + name + "." + PROFILE_KEYS[i]);
            }
            b.difficulty(level, new DifficultyProfile(v[0], v[1], v[2], v[3]));
        }
        if (!b.missingKeys().isEmpty()) {
            throw new IllegalArgumentException("balance.json is missing keys: " + b.missingKeys());
        }
        return b.build();
    }

    private static int requireInt(JsonValue v, String what) {
        if (!v.isLong()) {
            throw new IllegalArgumentException("balance.json: \"" + what + "\" must be an integer, got " + v);
        }
        long value = v.asLong();
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("balance.json: \"" + what + "\" is out of range");
        }
        return (int) value;
    }
}
