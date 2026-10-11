package com.michiganecodata.beestinction.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.michiganecodata.beestinction.platform.LogUploader;
import com.michiganecodata.beestinction.sim.log.GameLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * OVERVIEW:      Desktop LogUploader: writes each run's log as JSON files in a local folder (by default logs/
 *                under the working directory, which `./gradlew lwjgl3:run` sets to the repository root):
 *                <runId>-start.json when a run starts and <runId>.json when it ends. Never throws: failures are
 *                logged and ignored. The logs/ folder is git-ignored.
 * INVARIANTS:    directory is non-null.
 * COLLABORATORS: LogUploader, GameLog, Lwjgl3Launcher.
 */
public final class LocalFileLogUploader implements LogUploader {
    private final Path directory;

    /**
     * REQUIRES: directory != null
     * MODIFIES: nothing
     * EFFECTS:  Creates an uploader writing into `directory` (created on first use).
     */
    public LocalFileLogUploader(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
    }

    @Override
    public void uploadRunStart(GameLog partial) {
        write(partial.runId() + "-start.json", partial);
    }

    @Override
    public void uploadRunLog(GameLog complete) {
        write(complete.runId() + ".json", complete);
    }

    private void write(String fileName, GameLog log) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(fileName);
            Files.write(file, log.toJson().getBytes(StandardCharsets.UTF_8));
            info("Wrote " + file.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            info("Could not write game log " + fileName + ": " + e);
        }
    }

    private static void info(String message) {
        if (Gdx.app != null) {
            Gdx.app.log("LocalFileLogUploader", message);
        } else {
            System.out.println(message);
        }
    }
}
