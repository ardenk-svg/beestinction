package com.michiganecodata.beestinction.sim.world;

import com.michiganecodata.beestinction.sim.testutil.TestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pending until FogOfWar is implemented. Remove @Tag("pending") when it is. */
@Tag("pending")
class FogOfWarTest {

    @Test
    void everythingStartsHidden() {
        FogOfWar fog = new FogOfWar(21, 21);
        for (int x = 0; x < 21; x++) {
            for (int y = 0; y < 21; y++) {
                assertFalse(fog.isRevealed(x, y));
            }
        }
    }

    @Test
    void revealsAFilledCircleAroundAPoint() {
        FogOfWar fog = new FogOfWar(21, 21);
        fog.revealAround(new TilePos(10, 10), 3);
        assertTrue(fog.isRevealed(10, 10));
        assertTrue(fog.isRevealed(10, 13));
        assertTrue(fog.isRevealed(13, 10));
        assertTrue(fog.isRevealed(7, 10));
        assertTrue(fog.isRevealed(12, 12)); // distance^2 = 8 <= 9
        assertFalse(fog.isRevealed(13, 13)); // distance^2 = 18 > 9
        assertFalse(fog.isRevealed(10, 14));
    }

    @Test
    void revealedTilesStayRevealed() {
        FogOfWar fog = new FogOfWar(21, 21);
        fog.revealAround(new TilePos(5, 5), 1);
        fog.revealAround(new TilePos(15, 15), 1);
        assertTrue(fog.isRevealed(5, 5));
        assertTrue(fog.isRevealed(15, 15));
    }

    @Test
    void edgesAreClippedAndOutsideIsNeverRevealed() {
        FogOfWar fog = new FogOfWar(21, 21);
        fog.revealAround(new TilePos(0, 0), 2);
        assertTrue(fog.isRevealed(0, 0));
        assertTrue(fog.isRevealed(2, 0));
        assertFalse(fog.isRevealed(-1, 0));
        assertFalse(fog.isRevealed(0, -1));
        assertFalse(fog.isRevealed(21, 0));
    }

    @Test
    void patchesInRevealedAreasCountAsDiscovered() {
        GameMap map = TestFixtures.tinyMap();
        FogOfWar fog = new FogOfWar(map.width(), map.height());
        FlowerPatch near = map.patchById(TestFixtures.NEAR_PATCH_ID);
        FlowerPatch far = map.patchById(TestFixtures.FAR_PATCH_ID);
        assertFalse(fog.isPatchDiscovered(near));
        fog.revealAround(map.hivePos(), 3);
        assertTrue(fog.isPatchDiscovered(near));
        assertFalse(fog.isPatchDiscovered(far));
    }
}
