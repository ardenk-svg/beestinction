package com.michiganecodata.beestinction.sim.orders;

/**
 * OVERVIEW:      The kinds of player orders (CLAUDE.md B.7). A standing forage order is a FORAGE order with
 *                standing = true.
 * INVARIANTS:    none
 * COLLABORATORS: Order and subclasses, OrderRecord, GameLog.
 */
public enum OrderType {
    FORAGE, RECALL, CANCEL_STANDING, BUILD_COMB, CONVERT_BEES, SET_FEEDING_LEVEL
}
