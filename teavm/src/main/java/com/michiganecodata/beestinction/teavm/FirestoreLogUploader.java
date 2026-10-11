package com.michiganecodata.beestinction.teavm;

import com.michiganecodata.beestinction.platform.LogUploader;
import com.michiganecodata.beestinction.sim.log.GameLog;

/**
 * OVERVIEW:      Web LogUploader that will write anonymized logs to Cloud Firestore (CLAUDE.md Part G,
 *                Milestone 5): a small document in "runStarts" when a run begins and the full GameLog in "runLogs"
 *                when it ends, using Firebase Anonymous Authentication on the free Spark plan (no Cloud Functions).
 *                Whether it talks to the Firestore REST API or uses TeaVM JavaScript interop is decided in
 *                Milestone 5. Until then TeaVMLauncher uses NoOpLogUploader.
 * INVARIANTS:    Must never block or crash the game; must never send personal information.
 * COLLABORATORS: LogUploader, GameLog, TeaVMLauncher.
 */
public final class FirestoreLogUploader implements LogUploader {

    /**
     * REQUIRES: partial != null
     * MODIFIES: the "runStarts" Firestore collection
     * EFFECTS:  Creates a runStarts document (version, seed, map, difficulty). Never throws.
     */
    @Override
    public void uploadRunStart(GameLog partial) {
        // TODO(human): implement in Milestone 5 (CLAUDE.md Part G).
    }

    /**
     * REQUIRES: complete != null and has its summary
     * MODIFIES: the "runLogs" Firestore collection
     * EFFECTS:  Creates a runLogs document with complete.toJson(). Never throws.
     */
    @Override
    public void uploadRunLog(GameLog complete) {
        // TODO(human): implement in Milestone 5 (CLAUDE.md Part G).
    }
}
