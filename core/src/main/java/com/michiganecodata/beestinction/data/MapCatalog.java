package com.michiganecodata.beestinction.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.michiganecodata.beestinction.sim.config.FlowerType;
import com.michiganecodata.beestinction.sim.world.GameMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OVERVIEW:      The maps the player can choose from, with display metadata (id, name, description) read from each
 *                .tmx file's map properties. Reading the metadata only parses XML; the full map (with its tileset
 *                texture) is loaded when the player starts a run, and cached.
 *                DECISION: the list of map files is a constant here (MAP_FILES) rather than a directory scan,
 *                because listing asset folders is not reliable on the web. Add new maps to MAP_FILES.
 * INVARIANTS:    entries are in MAP_FILES order and have unique ids.
 * COLLABORATORS: TiledMapConverter, GameMap, FlowerType, MapSelectScreen, GameServices.
 */
public final class MapCatalog {
    /** Every playable map, as paths inside assets/. */
    public static final String[] MAP_FILES = {"maps/test_map.tmx"};

    /**
     * OVERVIEW:      One map's display metadata and file path.
     * INVARIANTS:    Immutable; fields non-null.
     * COLLABORATORS: MapCatalog, MapSelectScreen.
     */
    public static final class Entry {
        private final String id;
        private final String displayName;
        private final String description;
        private final String path;

        Entry(String id, String displayName, String description, String path) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.path = path;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public String description() {
            return description;
        }

        public String path() {
            return path;
        }
    }

    private final List<Entry> entries;
    private final Map<String, GameMap> loaded = new LinkedHashMap<>();

    private MapCatalog(List<Entry> entries) {
        this.entries = Collections.unmodifiableList(entries);
    }

    /**
     * REQUIRES: Gdx.files is available
     * MODIFIES: nothing
     * EFFECTS:  Reads the metadata of every map in MAP_FILES. Throws IllegalArgumentException if a map lacks its
     *           id or displayName property, or two maps share an id.
     */
    public static MapCatalog loadDefault() {
        List<Entry> entries = new ArrayList<>();
        for (String path : MAP_FILES) {
            XmlReader.Element root = new XmlReader().parse(Gdx.files.internal(path));
            Map<String, String> props = new LinkedHashMap<>();
            XmlReader.Element properties = root.getChildByName("properties");
            if (properties != null) {
                for (XmlReader.Element p : properties.getChildrenByName("property")) {
                    props.put(p.getAttribute("name", ""), p.getAttribute("value", ""));
                }
            }
            String id = props.get("id");
            String name = props.get("displayName");
            if (id == null || id.isEmpty() || name == null || name.isEmpty()) {
                throw new IllegalArgumentException(path + ": needs map properties \"id\" and \"displayName\"");
            }
            for (Entry e : entries) {
                if (e.id().equals(id)) {
                    throw new IllegalArgumentException(path + ": duplicate map id \"" + id + "\"");
                }
            }
            String description = props.get("description");
            entries.add(new Entry(id, name, description == null ? "" : description, path));
        }
        return new MapCatalog(entries);
    }

    public List<Entry> entries() {
        return entries;
    }

    /** The entry with this id, or null. */
    public Entry find(String id) {
        for (Entry e : entries) {
            if (e.id().equals(id)) {
                return e;
            }
        }
        return null;
    }

    /**
     * REQUIRES: entry is one of entries(); flowers contains every species the map uses
     * MODIFIES: this (cache)
     * EFFECTS:  Returns the GameMap for the entry, loading it the first time. (Each run works on its own
     *           freshCopy(), so sharing the cached map between runs is safe.)
     */
    public GameMap loadMap(Entry entry, List<FlowerType> flowers) {
        GameMap map = loaded.get(entry.id());
        if (map == null) {
            Map<String, FlowerType> byId = new LinkedHashMap<>();
            for (FlowerType f : flowers) {
                byId.put(f.id(), f);
            }
            map = TiledMapConverter.load(entry.path(), byId);
            if (!map.id().equals(entry.id())) {
                throw new IllegalArgumentException(entry.path() + ": id changed while loading");
            }
            loaded.put(entry.id(), map);
        }
        return map;
    }
}
