package com.michiganecodata.beestinction.sim.log;

import com.michiganecodata.beestinction.sim.GameSetup;
import com.michiganecodata.beestinction.sim.orders.Order;
import com.michiganecodata.beestinction.sim.stats.RunSummary;
import com.michiganecodata.beestinction.sim.util.GameRandom;
import com.michiganecodata.beestinction.sim.util.JsonWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OVERVIEW:      The anonymized record of one run (CLAUDE.md C.7): game version, a random run ID, the seed, map,
 *                and difficulty, every order (tick, type, accepted, parameters), every UI input that does not
 *                change the simulation (tick, type), and the end-of-run summary. Because the simulation is
 *                deterministic, seed + orders reconstructs the whole game, so logs stay small.
 *                It holds NO personal information: no names, accounts, IP-derived data, or wall-clock times.
 *                DECISION: the seed is written as a JSON string because JavaScript cannot represent every
 *                64-bit integer exactly.
 * INVARIANTS:    runId is derived from GameSetup.runIdSeed() (seeded separately from the game seed).
 *                Orders and UI events are kept in the order they were recorded. summary is null until the run
 *                ends.
 * COLLABORATORS: GameSetup, Order, OrderRecord, UiEventRecord, RunSummary, JsonWriter, GameRandom, Simulation,
 *                LogUploader (core).
 */
public final class GameLog {
    private final String version;
    private final String runId;
    private final long seed;
    private final String mapId;
    private final String difficulty;
    private final List<OrderRecord> orders = new ArrayList<>();
    private final List<UiEventRecord> uiEvents = new ArrayList<>();
    private RunSummary summary;

    /**
     * REQUIRES: setup != null
     * MODIFIES: nothing
     * EFFECTS:  Creates an empty log for a run of `setup`, with a run ID generated from setup.runIdSeed().
     */
    public GameLog(GameSetup setup) {
        Objects.requireNonNull(setup, "setup");
        this.version = setup.gameVersion();
        this.runId = makeRunId(setup.runIdSeed());
        this.seed = setup.seed();
        this.mapId = setup.map().id();
        this.difficulty = setup.difficulty().name();
    }

    private static String makeRunId(long runIdSeed) {
        GameRandom rng = new GameRandom(runIdSeed);
        String a = Long.toHexString(rng.nextLong());
        String b = Long.toHexString(rng.nextLong());
        StringBuilder sb = new StringBuilder(32);
        for (int i = a.length(); i < 16; i++) {
            sb.append('0');
        }
        sb.append(a);
        for (int i = b.length(); i < 16; i++) {
            sb.append('0');
        }
        sb.append(b);
        return sb.toString();
    }

    /**
     * REQUIRES: order != null and has been stamped with a tick
     * MODIFIES: this
     * EFFECTS:  Appends a record of the order (its tick, type, parameters, and whether it was accepted).
     */
    public void recordOrder(Order order, boolean accepted) {
        JsonWriter params = new JsonWriter();
        params.beginObject();
        order.writeParams(params);
        params.endObject();
        orders.add(new OrderRecord(order.tick(), order.type(), accepted, params.toString()));
    }

    /**
     * REQUIRES: tick >= 0, type != null
     * MODIFIES: this
     * EFFECTS:  Appends a UI event record.
     */
    public void recordUiEvent(long tick, UiEventType type) {
        uiEvents.add(new UiEventRecord(tick, type));
    }

    /**
     * REQUIRES: summary != null
     * MODIFIES: this
     * EFFECTS:  Stores the end-of-run summary.
     */
    public void setSummary(RunSummary summary) {
        this.summary = Objects.requireNonNull(summary, "summary");
    }

    public String version() {
        return version;
    }

    /** A random 32-hex-digit ID for this run. It is not the game seed. */
    public String runId() {
        return runId;
    }

    public long seed() {
        return seed;
    }

    public String mapId() {
        return mapId;
    }

    public String difficulty() {
        return difficulty;
    }

    public List<OrderRecord> orders() {
        return Collections.unmodifiableList(orders);
    }

    public List<UiEventRecord> uiEvents() {
        return Collections.unmodifiableList(uiEvents);
    }

    /** The summary, or null while the run is still going. */
    public RunSummary summary() {
        return summary;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the whole log as a JSON object string:
     *           {"version":..,"runId":..,"seed":"..","mapId":..,"difficulty":..,
     *            "orders":[{"tick":..,"type":..,"accepted":..,"params":{..}}],
     *            "uiEvents":[{"tick":..,"type":..}], "summary":{..} or null}
     */
    public String toJson() {
        JsonWriter w = new JsonWriter();
        w.beginObject();
        w.name("version").value(version);
        w.name("runId").value(runId);
        w.name("seed").value(Long.toString(seed));
        w.name("mapId").value(mapId);
        w.name("difficulty").value(difficulty);
        w.name("orders").beginArray();
        for (OrderRecord r : orders) {
            w.beginObject();
            w.name("tick").value(r.tick());
            w.name("type").value(r.type().name());
            w.name("accepted").value(r.accepted());
            w.name("params").rawValue(r.paramsJson());
            w.endObject();
        }
        w.endArray();
        w.name("uiEvents").beginArray();
        for (UiEventRecord r : uiEvents) {
            w.beginObject();
            w.name("tick").value(r.tick());
            w.name("type").value(r.type().name());
            w.endObject();
        }
        w.endArray();
        w.name("summary");
        if (summary == null) {
            w.nullValue();
        } else {
            summary.write(w);
        }
        w.endObject();
        return w.toString();
    }
}
