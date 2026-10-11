package com.michiganecodata.beestinction;

/**
 * OVERVIEW:      The game's version string, shown on the main menu and written into every GameLog.
 *                Keep it in step with projectVersion in gradle.properties.
 * INVARIANTS:    none
 * COLLABORATORS: GameServices, GameLog (through GameSetup).
 */
public final class AppVersion {
    public static final String VERSION = "0.1.0";

    private AppVersion() {
    }
}
