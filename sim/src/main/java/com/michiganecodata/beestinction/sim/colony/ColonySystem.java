package com.michiganecodata.beestinction.sim.colony;

import com.michiganecodata.beestinction.sim.GameState;
import com.michiganecodata.beestinction.sim.config.BalanceConfig;

import java.util.Objects;

/**
 * OVERVIEW:      The colony rules: setting up the starting colony, aging, old-age deaths, feeding (queen first),
 *                starvation, egg laying, brood development and emergence, comb building, and capacities.
 *                Daily totals (old-age deaths, rations, eggs) are computed at the start of the day and spread
 *                over the day's ticks with IntSmoothing.
 *                A system may keep per-day scratch values (for example today's ration total) in its own fields,
 *                but anything the UI or tests need must live in GameState.
 * INVARIANTS:    cfg is non-null. Uses no randomness except through the GameState's GameRandom.
 * COLLABORATORS: GameState, Hive and its parts, BalanceConfig, IntSmoothing, StatsRecorder (recordDeaths,
 *                recordBirths), WinterSystem (broodDiesOnEmergence), Simulation (calls this system).
 */
public final class ColonySystem {
    private final BalanceConfig cfg;
    // TODO(human): add per-day fields here if you need them (e.g. today's ration and egg totals).

    /**
     * REQUIRES: cfg != null
     * MODIFIES: nothing
     * EFFECTS:  Creates the colony system.
     */
    public ColonySystem(BalanceConfig cfg) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    /**
     * REQUIRES: s was just created (tick 0, no tick has run yet)
     * MODIFIES: s.hive()
     * EFFECTS:  Sets up the starting colony (CLAUDE.md B.2):
     *           - sets the nectar capacity from the comb (comb.nectarCapacity(cfg));
     *           - deposits cfg.startingNectar() (anything beyond capacity is lost);
     *           - adds cfg.startingWorkers() workers spread evenly across ages 0..lifespanDays-1: every age gets
     *             startingWorkers / lifespanDays bees, and the remainder goes one bee each to the youngest ages.
     *           DECISION: the starting workers have mixed ages so that some can forage on the first day.
     *           Starting workers are not births (do not call stats.recordBirths for them).
     */
    public void initializeColony(GameState s) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: s.clock().isDayStart()
     * MODIFIES: s.hive(), every ForagerGroup's bees, s.stats(), possibly s (endGame), this (per-day totals)
     * EFFECTS:  Start-of-day colony work:
     *           1. Except on day 0: records whether the queen got her full ration yesterday
     *              (queen.recordDay); if queen.hasStarved(cfg.queenStarvationDays()), calls
     *              s.endGame(DeathCause.QUEEN_STARVED).
     *           2. Begins aging: beginDay() on the hive's workers and on every group's bees (groups keep aging
     *              while away).
     *           3. Computes today's totals for onTick to spread over the day's ticks: the queen's ration
     *              (cfg.queenDailyRation()), the workers' rations (cfg.workerDailyRation() per worker), and the
     *              egg target (queen.dailyEggTarget(cfg, season); 0 in winter).
     *           TODO(human) decisions to document: whether bees away from the hive eat, how extra feeding levels
     *           increase consumption, and when the raise cost (cfg.raiseCostPerBee()) is paid.
     */
    public void onDayStart(GameState s) {
        // TODO(human): implement per RME above.
    }

    /**
     * REQUIRES: onDayStart(s) has run for the current day
     * MODIFIES: s.hive(), every ForagerGroup's bees, s.stats()
     * EFFECTS:  One tick of colony work, in this order:
     *           1. Old-age deaths: removeDyingSlice for the hive and every group; record OLD_AGE deaths.
     *           2. Feeding: withdraw this tick's slice of the queen's ration FIRST, then the workers' slice.
     *              The queen has priority over the whole day: workers may only eat nectar that is not needed
     *              for the rest of the queen's daily ration, so when nectar is short the queen is still fully
     *              fed. Track whether the queen got her full ration today.
     *           3. Starvation: workers who go unfed die (cfg.unfedWorkerDeathPercent() per day without food);
     *              record STARVATION deaths.
     *           4. Egg laying: lay this tick's slice of the egg target (none in winter).
     *           5. Brood development: brood.develop(number of house bees as nurses, cfg).
     *           6. Emergence: collect batches that reached cfg.broodThresholdPoints(); emerged bees join the
     *              age-0 cohort (record them with stats.recordBirths) unless
     *              s.rules().winter().broodDiesOnEmergence(season), in which case they die. The population must
     *              never exceed the comb's population capacity.
     *           7. Comb building: comb.progress(1).
     *           8. Capacities: set the nectar capacity from the comb.
     */
    public void onTick(GameState s) {
        // TODO(human): implement per RME above.
    }
}
