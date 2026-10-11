package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameMapTest {

    @Test
    void freshCopyHasTheSameLayoutAndNewPatchObjects() {
        GameMap map = TestFixtures.tinyMap();
        GameMap copy = map.freshCopy();
        assertEquals(map.id(), copy.id());
        assertEquals(map.width(), copy.width());
        assertEquals(map.hivePos(), copy.hivePos());
        assertEquals(map.patches().size(), copy.patches().size());
        for (int i = 0; i < map.patches().size(); i++) {
            FlowerPatch a = map.patches().get(i);
            FlowerPatch b = copy.patches().get(i);
            assertNotSame(a, b);
            assertEquals(a.id(), b.id());
            assertSame(a.type(), b.type());
            assertEquals(a.pos(), b.pos());
            assertEquals(a.size(), b.size());
            assertEquals(0, b.toxicUntilTick());
            assertEquals(0, b.nectarRemainingToday());
        }
    }

    @Test
    void tilesAreRowMajorWithYUp() {
        TileType[] tiles = new TileType[6];
        Arrays.fill(tiles, TileType.GRASS);
        tiles[1 * 3 + 2] = TileType.WATER; // (x=2, y=1)
        GameMap map = new GameMap("m", "M", "", 3, 2, tiles, new TilePos(0, 0), Collections.emptyList(), 100);
        assertEquals(TileType.WATER, map.tileAt(2, 1));
        assertEquals(TileType.GRASS, map.tileAt(2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> map.tileAt(3, 0));
    }

    @Test
    void patchLookups() {
        GameMap map = TestFixtures.tinyMap();
        assertEquals(TestFixtures.NEAR_POS, map.patchById(TestFixtures.NEAR_PATCH_ID).pos());
        assertEquals(TestFixtures.MID_PATCH_ID, map.patchAt(TestFixtures.MID_POS).id());
        assertNull(map.patchAt(TestFixtures.EMPTY_POS));
        assertNull(map.patchById(999));
    }

    @Test
    void rejectsInvalidMaps() {
        TileType[] tiles = new TileType[4];
        Arrays.fill(tiles, TileType.GRASS);
        List<FlowerPatch> none = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> new GameMap("m", "M", "", 2, 2, tiles, new TilePos(5, 5), none, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new GameMap("m", "M", "", 3, 2, tiles, new TilePos(0, 0), none, 100));
        List<FlowerPatch> dup = new ArrayList<>();
        dup.add(new FlowerPatch(1, TestFixtures.flower("clover"), new TilePos(0, 0), 1));
        dup.add(new FlowerPatch(1, TestFixtures.flower("clover"), new TilePos(1, 1), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GameMap("m", "M", "", 2, 2, tiles, new TilePos(0, 0), dup, 100));
    }
}
