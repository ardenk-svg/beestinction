package com.michiganecodata.beestinction.sim;

/**
 * OVERVIEW:      The kinds of popups the game shows (CLAUDE.md B.11). Every one of them pauses the game.
 *                Routine activity never produces a notification.
 * INVARIANTS:    none
 * COLLABORATORS: Notification, UiGameListener (core).
 */
public enum NotificationType {
    PESTICIDE_DEATHS,
    DISEASE_OUTBREAK,
    FREEZE_DEATHS,
    WINTER_DIE_OFF,
    NEW_SPECIES,
    GAME_OVER
}
