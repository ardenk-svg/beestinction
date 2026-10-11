package com.michiganecodata.beestinction.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.config.NectarPotential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * OVERVIEW:      Reads assets/data/flowers.json into FlowerType objects (CLAUDE.md D.6):
 *                {"flowers": [ {"id", "displayName", "bloomSeasons": [...], "nectarPerDayPerSize", "potential",
 *                "factId"} ]}. Parsed by hand with JsonReader/JsonValue (no reflection). Enum names are matched
 *                without Enum.valueOf so the web build needs no reflection metadata.
 * INVARIANTS:    Stateless.
 * COLLABORATORS: FlowerType, Season, NectarPotential, BeestinctionGame, DataFilesTest.
 */
public final class FlowerTypeLoader {
    private FlowerTypeLoader() {
    }

    /**
     * REQUIRES: file != null and readable
     * MODIFIES: nothing
     * EFFECTS:  Returns the species in the file, in file order. Throws IllegalArgumentException on bad data.
     */
    public static List<FlowerType> load(FileHandle file) {
        return parse(file.readString("UTF-8"));
    }

    /**
     * REQUIRES: json != null
     * MODIFIES: nothing
     * EFFECTS:  Parses flowers JSON text. Throws IllegalArgumentException (naming the species and field) if a
     *           field is missing or invalid, or two species share an id.
     */
    public static List<FlowerType> parse(String json) {
        JsonValue root = new JsonReader().parse(json);
        JsonValue flowers = root == null ? null : root.get("flowers");
        if (flowers == null || !flowers.isArray()) {
            throw new IllegalArgumentException("flowers.json must have a \"flowers\" array");
        }
        List<FlowerType> out = new ArrayList<>();
        for (JsonValue f = flowers.child; f != null; f = f.next) {
            String id = requireString(f, "id", "?");
            String displayName = requireString(f, "displayName", id);
            JsonValue seasons = f.get("bloomSeasons");
            if (seasons == null || !seasons.isArray()) {
                throw new IllegalArgumentException("flowers.json: " + id + " needs a \"bloomSeasons\" array");
            }
            EnumSet<Season> bloom = EnumSet.noneOf(Season.class);
            for (JsonValue s = seasons.child; s != null; s = s.next) {
                bloom.add(byName(Season.values(), s.asString(), id, "bloomSeasons"));
            }
            JsonValue nectar = f.get("nectarPerDayPerSize");
            if (nectar == null || !nectar.isLong()) {
                throw new IllegalArgumentException("flowers.json: " + id + " needs an integer \"nectarPerDayPerSize\"");
            }
            NectarPotential potential = byName(NectarPotential.values(), requireString(f, "potential", id), id,
                    "potential");
            JsonValue fact = f.get("factId");
            String factId = (fact == null || fact.isNull()) ? null : fact.asString();
            for (FlowerType existing : out) {
                if (existing.id().equals(id)) {
                    throw new IllegalArgumentException("flowers.json: duplicate id \"" + id + "\"");
                }
            }
            out.add(new FlowerType(id, displayName, bloom, nectar.asInt(), potential, factId));
        }
        return Collections.unmodifiableList(out);
    }

    private static String requireString(JsonValue obj, String field, String id) {
        JsonValue v = obj.get(field);
        if (v == null || !v.isString()) {
            throw new IllegalArgumentException("flowers.json: " + id + " needs a string \"" + field + "\"");
        }
        return v.asString();
    }

    private static <E extends Enum<E>> E byName(E[] values, String name, String id, String field) {
        for (E e : values) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        throw new IllegalArgumentException("flowers.json: " + id + "." + field + " has unknown value \"" + name + "\"");
    }
}
