package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.Season;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;

/**
 * OVERVIEW:      The colony's queen. She is immortal unless she starves, immune to disease, never forages, and
 *                feeds herself. She lays eggs every day except in winter; a higher feeding level means more eggs.
 *                The game is over when she goes queenStarvationDays (2) consecutive days without her full ration.
 * INVARIANTS:    0 <= feedingLevel; consecutiveUnfedDays >= 0.
 * COLLABORATORS: Hive, ColonySystem, SetFeedingLevelOrder, BalanceConfig.
 */
public final class Queen {
    private int feedingLevel;
    private int consecutiveUnfedDays;

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Creates a fed queen at feeding level 0 (baseline feeding: survival only).
     */
    public Queen() {
        this.feedingLevel = 0;
        this.consecutiveUnfedDays = 0;
    }

    /** Extra feeding level chosen by the player (0 = baseline). */
    public int feedingLevel() {
        return feedingLevel;
    }

    /** Number of days in a row the queen did not get her full ration. */
    public int consecutiveUnfedDays() {
        return consecutiveUnfedDays;
    }

    /**
     * REQUIRES: level >= 0 (SetFeedingLevelOrder.validate checks level <= maxFeedingLevel)
     * MODIFIES: this
     * EFFECTS:  Sets the feeding level.
     */
    public void setFeedingLevel(int level) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Records how the previous day went: a fully fed day resets consecutiveUnfedDays to 0; a day
     *           without the full ration adds 1.
     */
    public void recordDay(boolean fullyFed) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: limitDays > 0
     * MODIFIES: nothing
     * EFFECTS:  Returns true if the queen has gone at least limitDays consecutive days without her full ration.
     */
    public boolean hasStarved(int limitDays) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: cfg, season != null
     * MODIFIES: nothing
     * EFFECTS:  Returns how many eggs the queen aims to lay today: 0 in WINTER, otherwise
     *           cfg.eggsPerDayBase() + feedingLevel * cfg.eggsPerDayPerFeedingLevel().
     */
    public int dailyEggTarget(BalanceConfig cfg, Season season) {
        // TODO(human): implement per RME above.
        return 0;
    }
}
