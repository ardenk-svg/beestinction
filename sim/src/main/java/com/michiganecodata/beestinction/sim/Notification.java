package com.michiganecodata.beestinction.sim;

import java.util.Objects;

/**
 * OVERVIEW:      An immutable message from the simulation to the player, such as "pesticide killed 40 bees".
 *                Systems post notifications to GameState's outbox; Simulation dispatches them to every
 *                GameListener after the tick finishes.
 * INVARIANTS:    type, title, and message are non-null. factId and abstractionNote may be null.
 * COLLABORATORS: NotificationType, GameState (outbox), GameListener, NotificationDialog (core).
 */
public final class Notification {
    private final NotificationType type;
    private final String title;
    private final String message;
    private final boolean pausesGame;
    private final String factId;
    private final String abstractionNote;

    /**
     * REQUIRES: type, title, message != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a notification. factId (a fact from the team's fact list) and abstractionNote (a note
     *           explaining one of the game's simplifications) may be null.
     */
    public Notification(NotificationType type, String title, String message, boolean pausesGame,
                        String factId, String abstractionNote) {
        this.type = Objects.requireNonNull(type, "type");
        this.title = Objects.requireNonNull(title, "title");
        this.message = Objects.requireNonNull(message, "message");
        this.pausesGame = pausesGame;
        this.factId = factId;
        this.abstractionNote = abstractionNote;
    }

    /**
     * REQUIRES: type, title, message != null
     * MODIFIES: nothing
     * EFFECTS:  Returns a notification that pauses the game, with no fact and no abstraction note.
     *           All popups in CLAUDE.md B.11 pause the game.
     */
    public static Notification popup(NotificationType type, String title, String message) {
        return new Notification(type, title, message, true, null, null);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a copy of this notification with the given fact ID (null to remove it).
     */
    public Notification withFactId(String newFactId) {
        return new Notification(type, title, message, pausesGame, newFactId, abstractionNote);
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns a copy of this notification with the given abstraction note (null to remove it).
     */
    public Notification withAbstractionNote(String note) {
        return new Notification(type, title, message, pausesGame, factId, note);
    }

    public NotificationType type() {
        return type;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    public boolean pausesGame() {
        return pausesGame;
    }

    /** Returns the fact ID, or null if there is none. */
    public String factId() {
        return factId;
    }

    /** Returns the abstraction note, or null if there is none. */
    public String abstractionNote() {
        return abstractionNote;
    }

    @Override
    public String toString() {
        return "Notification[" + type + ": " + title + "]";
    }
}
