package com.michiganecodata.beestinction.sim.difficulty;

import com.michiganecodata.beestinction.sim.GameClock;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;
import com.michiganecodata.beestinction.sim.config.DifficultyLevel;
import com.michiganecodata.beestinction.sim.config.DifficultyProfile;
import com.michiganecodata.beestinction.sim.events.EventType;

import java.util.Objects;

/**
 * OVERVIEW:      The Difficulty implementation: a DifficultyProfile (the level's base numbers) plus escalation
 *                that rises every week (profile.escalationPercentPerWeek()). Easy, Medium, and Hard are three
 *                configurations of this one class.
 *                TODO(human) decision: the exact curves (CLAUDE.md Part I). They must keep rising until survival
 *                is impossible, Hard must yield less nectar than Easy, and harder levels score faster.
 * INVARIANTS:    profile and cfg are non-null.
 * COLLABORATORS: Difficulty, DifficultyProfile, BalanceConfig, EventType, GameClock (days per week).
 */
public final class ScalingDifficulty implements Difficulty {
    private final DifficultyProfile profile;
    private final BalanceConfig cfg;

    /**
     * REQUIRES: profile, cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Creates a difficulty from a level's base profile.
     */
    public ScalingDifficulty(DifficultyProfile profile, BalanceConfig cfg) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    /**
     * REQUIRES: level, cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the Difficulty for `level`, built from cfg's profile for that level.
     */
    public static Difficulty forLevel(DifficultyLevel level, BalanceConfig cfg) {
        return new ScalingDifficulty(cfg.difficultyProfile(level), cfg);
    }

    public DifficultyProfile profile() {
        return profile;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns true if day < cfg.gracePeriodDays().
     */
    @Override
    public boolean inGracePeriod(int day) {
        // TODO(human): implement per RME above.
        return false;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the escalation in percent: profile.escalationPercentPerWeek() for every completed week
     *           (day / GameClock.DAYS_PER_WEEK weeks). 0 on day 0; never decreases as day grows; rises over time
     *           for every level.
     */
    @Override
    public int escalation(int day) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the score multiplier in percent: the base (profile.baseMultiplierPercent()) raised by the
     *           escalation. Higher on harder levels on the same day, and rises over time.
     */
    @Override
    public int scoreMultiplierPercent(int day) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: t != null, day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the daily chance of event type t in basis points: 0 during the grace period, otherwise
     *           profile.eventBaseBasisPoints() raised by the escalation. Never above 10000.
     */
    @Override
    public int eventChanceBasisPoints(EventType t, int day) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns how far event severities shift toward their maximum, in percent (0..100), rising with
     *           the escalation.
     */
    @Override
    public int severityShiftPercent(int day) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns the nectar yield in percent: profile.yieldPercent(), possibly lowered by escalation.
     *           Hard yields less than Easy on the same day.
     */
    @Override
    public int nectarYieldPercent(int day) {
        // TODO(human): implement per RME above.
        return 0;
    }

    /**
     * REQUIRES: day >= 0
     * MODIFIES: nothing
     * EFFECTS:  Returns extra daily worker mortality in basis points, rising with the escalation (0 at the start).
     */
    @Override
    public int extraMortalityBasisPoints(int day) {
        // TODO(human): implement per RME above.
        return 0;
    }
}
