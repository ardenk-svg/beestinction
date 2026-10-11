package com.michiganecodata.beestinction.platform;

import com.michiganecodata.beestinction.sim.log.GameLog;

/**
 * OVERVIEW:      A LogUploader that does nothing. Used by tests, as a fallback, and on the web until the Firestore
 *                uploader is built in Milestone 5.
 * INVARIANTS:    Stateless.
 * COLLABORATORS: LogUploader.
 */
public final class NoOpLogUploader implements LogUploader {

    @Override
    public void uploadRunStart(GameLog partial) {
        // Intentionally empty.
    }

    @Override
    public void uploadRunLog(GameLog complete) {
        // Intentionally empty.
    }
}
