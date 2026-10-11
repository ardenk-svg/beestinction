package com.michiganecodata.beestinction.sim.testutil;

import com.michiganecodata.beestinction.sim.world.FlowerPatch;
import com.michiganecodata.beestinction.sim.world.GameMap;
import com.michiganecodata.beestinction.sim.world.TileType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that TestMapCopy (the pure-Java copy used by headless tests) still matches assets/maps/test_map.tmx.
 * Parses the .tmx with the JDK's XML parser, following the conventions in CLAUDE.md D.6.
 * If this fails after editing the map, rerun tools/generate_test_map.py (or update TestMapCopy by hand).
 */
class TestMapCopyTest {

    @Test
    void javaCopyMatchesTheTmxFile() throws Exception {
        File tmx = new File(System.getProperty("beestinction.assetsDir", "../assets"), "maps/test_map.tmx");
        assertTrue(tmx.isFile(), "missing " + tmx.getAbsolutePath());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tmx);
        Element map = doc.getDocumentElement();
        int width = Integer.parseInt(map.getAttribute("width"));
        int height = Integer.parseInt(map.getAttribute("height"));
        int tileW = Integer.parseInt(map.getAttribute("tilewidth"));
        int tileH = Integer.parseInt(map.getAttribute("tileheight"));

        GameMap copy = TestFixtures.testMapCopy();
        assertEquals(copy.width(), width);
        assertEquals(copy.height(), height);
        assertEquals(copy.id(), mapProperty(map, "id"));
        assertEquals(copy.displayName(), mapProperty(map, "displayName"));
        assertEquals(copy.milliMilesPerTile(), Integer.parseInt(mapProperty(map, "milliMilesPerTile")));

        // Tileset: gid -> type name.
        Element tileset = (Element) map.getElementsByTagName("tileset").item(0);
        int firstGid = Integer.parseInt(tileset.getAttribute("firstgid"));
        String[] typeByGid = new String[64];
        NodeList tiles = tileset.getElementsByTagName("tile");
        for (int i = 0; i < tiles.getLength(); i++) {
            Element tile = (Element) tiles.item(i);
            typeByGid[firstGid + Integer.parseInt(tile.getAttribute("id"))] = property(tile, "type");
        }

        // Terrain layer (CSV, rows top to bottom).
        Element layer = namedChild(map, "layer", "terrain");
        String csv = layer.getElementsByTagName("data").item(0).getTextContent().trim();
        String[] cells = csv.split("\\s*,\\s*");
        assertEquals(width * height, cells.length);
        for (int row = 0; row < height; row++) {
            int y = height - 1 - row;
            for (int x = 0; x < width; x++) {
                String type = typeByGid[Integer.parseInt(cells[row * width + x].trim())];
                assertEquals(TileType.valueOf(type), copy.tileAt(x, y), "tile (" + x + ", " + y + ")");
            }
        }

        // Hive: exactly one point object.
        Element hiveLayer = namedChild(map, "objectgroup", "hive");
        NodeList hiveObjects = hiveLayer.getElementsByTagName("object");
        assertEquals(1, hiveObjects.getLength());
        Element hive = (Element) hiveObjects.item(0);
        assertEquals(copy.hivePos().x(), tileX(hive, tileW));
        assertEquals(copy.hivePos().y(), tileY(hive, tileH, height));

        // Patches.
        Element patchLayer = namedChild(map, "objectgroup", "patches");
        NodeList objs = patchLayer.getElementsByTagName("object");
        List<FlowerPatch> patches = copy.patches();
        assertEquals(patches.size(), objs.getLength());
        for (int i = 0; i < objs.getLength(); i++) {
            Element o = (Element) objs.item(i);
            FlowerPatch p = copy.patchById(Integer.parseInt(o.getAttribute("id")));
            assertNotNull(p, "patch " + o.getAttribute("id"));
            assertEquals(p.type().id(), property(o, "species"));
            assertEquals(p.size(), Integer.parseInt(property(o, "size")));
            assertEquals(p.pos().x(), tileX(o, tileW));
            assertEquals(p.pos().y(), tileY(o, tileH, height));
        }
    }

    private static int tileX(Element obj, int tileW) {
        return (int) Math.floor(Double.parseDouble(obj.getAttribute("x")) / tileW);
    }

    /** Tiled's y points down; convert to a y-up tile row. */
    private static int tileY(Element obj, int tileH, int height) {
        int rowFromTop = (int) Math.floor(Double.parseDouble(obj.getAttribute("y")) / tileH);
        return height - 1 - rowFromTop;
    }

    private static String mapProperty(Element map, String name) {
        for (Element props : directChildren(map, "properties")) {
            for (Element p : directChildren(props, "property")) {
                if (name.equals(p.getAttribute("name"))) {
                    return p.getAttribute("value");
                }
            }
        }
        return null;
    }

    private static String property(Element owner, String name) {
        NodeList props = owner.getElementsByTagName("property");
        for (int i = 0; i < props.getLength(); i++) {
            Element p = (Element) props.item(i);
            if (name.equals(p.getAttribute("name"))) {
                return p.getAttribute("value");
            }
        }
        return null;
    }

    private static Element namedChild(Element parent, String tag, String name) {
        for (Element e : directChildren(parent, tag)) {
            if (name.equals(e.getAttribute("name"))) {
                return e;
            }
        }
        throw new AssertionError("no <" + tag + " name=\"" + name + "\">");
    }

    private static List<Element> directChildren(Element parent, String tag) {
        List<Element> out = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element && ((Element) children.item(i)).getTagName().equals(tag)) {
                out.add((Element) children.item(i));
            }
        }
        return out;
    }
}
