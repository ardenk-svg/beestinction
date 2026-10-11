package com.michiganecodata.beestinction.platform;

import com.michiganecodata.beestinction.sim.log.GameLog;

/**
 * OVERVIEW:      Sends anonymized game logs somewhere (dependency inversion for platform services, CLAUDE.md C.2).
 *                Each platform provides its own implementation: desktop writes local JSON files
 *                (LocalFileLogUploader), the web will upload to Firestore in Milestone 5 (FirestoreLogUploader),
 *                and tests use NoOpLogUploader. The sim and the tests never need network access.
 * INVARIANTS:    Implementations must never block the game for long and must never throw: they catch and log
 *                their own errors.
 * COLLABORATORS: GameLog, GameScreen (run start), SummaryScreen (run end), BeestinctionGame.
 */
public interface LogUploader {

    /**
     * REQUIRES: partial != null (its summary is still null)
     * MODIFIES: whatever the implementation writes to
     * EFFECTS:  Records that a run started (version, seed, map, difficulty). Never throws.
     */
    void uploadRunStart(GameLog partial);

    /**
     * REQUIRES: complete != null and has its summary
     * MODIFIES: whatever the implementation writes to
     * EFFECTS:  Records the full log of a finished run. Never throws.
     */
    void uploadRunLog(GameLog complete);
}
