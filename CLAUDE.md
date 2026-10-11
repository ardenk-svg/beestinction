# Beestinction — Project Spec and Instructions for Claude Code

Beestinction is a single-player bumblebee colony survival game made by Michigan EcoData. It is written in Java with an object-oriented design, rendered with libGDX, and playable in the browser through a TeaVM web build hosted on GitHub Pages.

Spec version 0.1 (October 2026). Base Java package: `com.michiganecodata.beestinction` (rename if the team prefers; keep it consistent).

This file has two audiences. **Part A** tells Claude Code what its job is. **Parts B–I** are the game and architecture spec that everyone works from.

---

## Part A — Instructions for Claude Code

### A.1 Your role

You are setting up the project, not building the game. The human developers will implement almost all of the game logic themselves, as a learning and portfolio project. Your job is to:

1. Create the repository layout, the Gradle multi-module build, the launchers, and the asset folders (Part D).
2. Create every class, interface, enum, and method in Part D as a **skeleton**: correct packages, fields, and signatures, a class-level comment, and an RME comment on every public and protected method (A.4).
3. Implement the **architecture**: the code that connects the pieces so the game runs end to end with the game rules stubbed (A.2).
4. Write the **test suites** in Part E against the RME specs, so the humans can test their code as they write it.
5. Set up **CI/CD** (Part F).
6. Write a `README.md` covering build, run, and test commands, the module layout, the RME convention, and how to enable pending tests.

When something in this spec is ambiguous, choose the simplest option that keeps the sim deterministic, leave a `// DECISION:` comment explaining the choice, and list it in the README under "Open decisions". Do not invent new gameplay.

### A.2 What you DO implement (infrastructure and wiring)

- Gradle build, module dependencies, desktop launcher, TeaVM web build.
- `BeestinctionGame` and screen transitions: MainMenu → MapSelect → Game → Summary → MainMenu.
- `TickDriver`: fixed-timestep accumulator, speed toggle (1x → 2x → 3x → 1x), pause, skip-to-spring fast-forward spread over frames, catch-up cap, interpolation alpha, auto-pause when libGDX calls `pause()` (hidden tab).
- `Simulation.tick()` orchestration: calling subsystem methods in the exact order in D.5. Subsystem bodies are stubs.
- Observer plumbing: `GameListener`, listener registration, the notification outbox, and dispatch. `UiGameListener` pauses the driver and opens a dialog for notifications that pause the game.
- The order pipeline: UI → `Simulation.submit()` (stamped with the current tick) → `OrderQueue` → validated and applied at that tick → recorded in `GameLog`.
- Infrastructure classes: `GameRandom`, `IntMath`, `IntSmoothing`, `GameClock`, `OrderQueue`, `StatsRecorder` (recording and summary building), `GameLog` and `JsonWriter` (hand-written JSON), `ReplayRunner`.
- Data-loading plumbing: `BalanceLoader`, `FlowerTypeLoader`, `TiledMapConverter`, `MapCatalog`. These read files and build sim objects; they are plumbing, not rules, so implement them fully.
- One small placeholder Tiled map (`assets/maps/test_map.tmx`) following D.6, plus placeholder `balance.json` and `flowers.json`.
- **Placeholder** rendering and UI so the walking skeleton is visible and usable: terrain and patches as colored shapes, the hive as a marker, fog as a dark overlay, bee groups as dots at interpolated positions, HUD labels bound to the sim, the speed toggle, a notification dialog, and a minimal order panel (select a patch or tile, enter a bee count, Send / Standing / Recall). Keep it plain; the humans will design the real UI.
- `LogUploader` interface, `NoOpLogUploader`, and desktop `LocalFileLogUploader`. Leave `FirestoreLogUploader` as a stub (Milestone 5).
- CI and deploy workflows (Part F).

### A.3 What you do NOT implement

Leave these as stubs marked `// TODO(human)`:

- All colony rules: aging, roles, egg laying, brood development, feeding and consumption, starvation, comb expansion.
- Foraging: movement math, travel time, patch capacity sharing, yields, pesticide exposure, fog reveal, discoveries.
- `EventScheduler` rolls, `GameEvent.apply()` bodies, the `Difficulty` curves, scoring, winter and conversion rules.
- `Order.validate()` and `Order.apply()` bodies.
- Final UI layout, art, and polish.
- Balance tuning. Use the placeholder values in D.7 and do not tune them.

**Stub rules:**
- A stub must never throw. Return a neutral value (0, `false`, an empty list, unchanged state) so the game still runs end to end.
- Every stub has a full RME and the line `// TODO(human): implement per RME above.`

### A.4 RME comment format

Class level:

```java
/**
 * OVERVIEW:      What this class represents and what it is responsible for.
 * INVARIANTS:    Conditions that must hold for every valid object of this class.
 * COLLABORATORS: The classes it works with.
 */
```

Method level:

```java
/**
 * REQUIRES: Preconditions the caller must guarantee (or "nothing").
 * MODIFIES: Which objects or fields change (or "nothing").
 * EFFECTS:  What the method does and returns, including edge cases.
 */
```

Example:

```java
/**
 * REQUIRES: newborns >= 0
 * MODIFIES: this
 * EFFECTS:  Adds `newborns` bees to the age-0 cohort.
 */
public void addNewborns(int newborns) {
    // TODO(human): implement per RME above.
}
```

### A.5 Hard rules for all code

1. The `sim` module is pure Java with **no libGDX imports**, so it runs headless in tests and CI.
2. **Integer-only simulation.** No `float` or `double` anywhere in `sim` state or rules. Use integer percent or basis points (1 bp = 0.01%). Sub-tile positions use fixed-point milli-tiles. Rendering in `core` may use floats.
3. **Randomness only through `GameRandom`.** Never use `Math.random`, `java.util.Random`, `System.currentTimeMillis()`, or `System.nanoTime()` inside `sim`.
4. **Deterministic iteration.** Use arrays, `ArrayList`, `LinkedHashMap`, `TreeMap`, or `EnumMap`. Never iterate a `HashMap` or `HashSet` where the order affects results.
5. **Time inside the sim is the tick counter only.** Orders and log records carry tick numbers, never wall-clock time.
6. **No reflection-based serialization** (no Jackson, Gson, Kryo, or libGDX `Json` object mapping). Parse with libGDX `JsonReader`/`JsonValue` in `core`; write JSON by hand with `JsonWriter`.
7. **No threads** in `sim` or `core`.
8. **Composition over inheritance** for data variety: flower species are data, not subclasses. Inheritance is used only for the `Order` (Command) hierarchy, the `GameEvent` hierarchy, and libGDX screens.
9. Use the Java language level that gdx-liftoff selects for a TeaVM-enabled project, and check TeaVM's supported JDK classes before using newer APIs.
10. Do not add dependencies beyond those in D.1 without asking.

### A.6 Definition of done for the setup

- `./gradlew build` succeeds, and `./gradlew test` passes with pending tests excluded.
- `./gradlew lwjgl3:run` opens the game: main menu → map select (map + difficulty) → game screen with the placeholder map, HUD, and speed toggle. Ticks advance, orders can be submitted, and a debug key ends the run and shows the summary screen.
- `./gradlew teavm:build` produces the web build, and `teavm:run` serves it locally with the same behavior as desktop (use the equivalent tasks if gdx-liftoff names them differently).
- Both workflows exist, and CI passes on a test pull request.
- The README is complete, including the list of open decisions.

---

## Part B — Game design spec

### B.1 Concept

The player commands a wild bumblebee colony in a Michigan-inspired landscape. They send worker bees out to collect nectar, raise new bees, expand the hive, and prepare for winter, while threats such as pesticides and disease escalate until the colony can no longer survive. The colony always dies eventually; the score measures how well the player did. Short real-world facts about Michigan bees appear alongside major events.

### B.2 Starting a run

- The player picks one of **1–3 handmade, Michigan-inspired maps**. Each map is a **4-mile radius** around a fixed hive location, so choosing a map is choosing the starting location. Maps should support different playstyles (for example, orchard country with heavy spraying versus scattered urban gardens). Maps built from real Michigan data are a possible later add-on.
- The player picks a difficulty: **Easy, Medium, or Hard**.
- Each run uses a new random seed. Tests and replays use fixed seeds.
- The hive starts with a fixed number of workers, the queen, and some starting nectar (values in D.7).
- The run starts at the beginning of spring (assumption; see Part I).

### B.3 Time

| Unit | Ticks | Real time at 1x |
|---|---|---|
| Tick | 1 | 0.25 s (4 ticks per second) |
| Day | 35 | 8.75 s |
| Week | 245 | 61.25 s |
| Season (4 weeks) | 980 | about 4 min |
| Year (4 seasons, 16 weeks) | 3,920 | about 16 min |

- Speed toggle cycles **1x → 2x → 3x → 1x**. The player can also pause.
- Rendering runs at the display frame rate and interpolates bee-group positions between ticks.
- Numbers on screen (births, deaths, nectar, score) change every tick. Daily totals are spread across the day's ticks with integer smoothing (C.4).
- Seasons follow the Michigan cycle: spring → summer → fall → winter. The season decides which flower species are blooming.

### B.4 Map, flowers, and exploration

- The map is a tile grid. Flower patches have a species and a size.
- **Fog of war:** the map is hidden except near the hive. Areas are revealed when bees travel there. There is no separate scout unit: scouts are ordinary foragers sent into unexplored areas.
- Each patch has a **daily nectar capacity** based on its species and size. That capacity is shared among all bees foraging there that day, so sending more bees to one patch has diminishing returns.
- The first time bees successfully collect from a species, a popup (which pauses the game) shows information about the species and a qualitative nectar potential: low, medium, or high. Routine collections produce no popup.

### B.5 The colony

- **Queen:** immortal unless she starves. She is immune to disease, does not forage, and can eat on her own (no workers needed). She lays eggs, except in winter.
- **Workers:** stored as counts in **age cohorts by day**, never as individual objects. Lifespan is 6 weeks (42 days); bees die of old age at the end of it.
- **Roles by age:** younger bees are house bees (nursing, building); bees at or above the forage age (D.7) are foragers available for orders. This is an intentional simplification; real bumblebees divide work mainly by body size.
- **Brood:** eggs are tracked as batches. Each batch gains development points every tick: base points plus a bonus that grows with the number of nurse bees. When a batch reaches the threshold, its bees emerge as age-0 workers. More nurses means faster reproduction.
- **Comb (hive size):** caps both total population and nectar storage. The player can order the hive expanded; expansion is expensive in nectar.

### B.6 Nectar, feeding, and game over

- Nectar is the only resource for now. Pollen may be added to flower species in a later version.
- Bees consume nectar to survive. The player controls a feeding level: the baseline covers survival, and extra feeding raises the queen's egg-laying rate.
- **The queen is fed first**, then the workers. Workers who go unfed die.
- **Game over:** the queen goes **2 consecutive days** without her full ration.
- Nectar is never negative and never above storage capacity; nectar returned beyond capacity is lost (assumption; see Part I).

### B.7 Orders

Every player action that changes the game is an `Order` object stamped with the tick it was issued.

| Order | Contents | Rules |
|---|---|---|
| Forage | bee count, target tile or patch | One trip. Valid only if enough foragers are in the hive. |
| Standing forage | bee count, target patch | Repeats trips until cancelled. Does **not** refill as bees die; it shrinks until it runs out. Keeps going in winter (the player must cancel it). |
| Recall | group | The group turns around and comes home. |
| Cancel standing order | group | The group finishes its current trip and stops. |
| Build comb | bee count | Expands the hive; expensive in nectar. |
| Convert bees | bee count | Only during the conversion window (B.9). |
| Set feeding level | level | Changes extra feeding, which changes egg laying. |

**Foraging trips:** travel time depends on distance and is tuned for gameplay, so a round trip takes about a couple of in-game days. Bees forage at the patch, then return and deposit nectar, with slight randomness in the amount. A group carries its own age cohorts, keeps aging while away, and can lose bees to old age mid-trip.

### B.8 Threats

All frequencies and severities scale with the `Difficulty` object (B.10). There is a **grace period** at the start of each run with no random events, and **no random events in winter**.

- **Pesticide:** a patch (for example, a pumpkin patch) is sprayed at random with **no warning**, from the perspective of a wild bee. It stays toxic for **2 weeks**, and a respray resets the timer. Of the bees that forage there while it is toxic, **70–80%** die. The player gets a popup **after** the deaths, so they can recall groups and stop sending bees there. There is no signal for when it is safe again; the player has to test with bees.
- **Disease:** a hive-wide outbreak kills **40–70%** of workers, with randomness. The queen is immune. Disease is meant to feel random and powerful.

### B.9 Winter and the conversion mechanic

- **Conversion window:** from mid-fall until winter begins, the player can convert any workers, including old ones, into nectar. A converted bee is worth **10–30% of its raise cost**. This is an intentional abstraction for playability; it is not real bee behavior.
- **When winter starts:**
  - Foraging is impossible.
  - Bees outside the hive that do not get back within **2 winter days** freeze to death.
  - On **winter day 4**, all remaining workers die.
  - Brood still developing dies when it emerges into the cold.
  - Egg laying is completely paused.
  - The queen survives and eats stored nectar.
- **Skip to spring:** after the day-4 die-off, a skip button appears. It fast-forwards: **every tick still runs**, including the queen's nectar consumption. The skip is spread across multiple frames so the browser doesn't freeze, and it stops on game over.
- **Spring:** the queen resumes laying and the colony rebuilds.

### B.10 Difficulty and score

- The chosen level (Easy, Medium, Hard) sets a base. An **escalation counter rises over time** on top of the base.
- The resulting difficulty drives event frequency and severity, death rates, nectar yield (Hard yields less per collection), and the score multiplier. It must keep rising until survival is impossible.
- **Points per day = population × difficulty multiplier.** Points accrue smoothly every tick. Higher difficulty means a higher point rate.

### B.11 Popups

These popups pause the game until the player continues:

- Pesticide deaths
- Disease outbreak
- Freezing deaths
- Winter die-off
- New species discovered
- Game over

There are no popups for routine activity; the player watches nectar and bee counts. Notes explaining the game's abstractions (nectar conversion, the queen eating through winter, the same queen surviving year to year) are bundled into the related major popup only, never shown as standalone popups. Real facts with citations are kept in a team Google Doc for now and added to popups later, so each notification has an optional `factId`.

### B.12 Interface

- The map is the main interface.
- **HUD:**
  - nectar and storage capacity
  - population and population capacity
  - the **age pipeline** (brood, house bees, available foragers, bees out)
  - season and day
  - score
  - speed toggle and pause
- **Controls:**
  - select a patch or tile, choose a bee count, then Send or Standing
  - a list of groups out, with Recall and Cancel
  - feeding level
  - build comb
  - convert bees (visible only during the window)
  - skip to spring (visible after the die-off)
- A "best played on desktop" notice, and a few first-run tooltips (for example: click a patch → choose bees → send).

### B.13 End-of-run summary

The summary screen shows score, days survived, cause of game over, population over time, nectar collected, bees lost by cause, and species discovered. The same summary goes into the anonymized log (C.7).

### B.14 Out of scope for v0.1

Save games (a refresh loses progress), leaderboards, pollen, maps generated from real data, queen foraging, size-based roles, a specific named bee species as the theme, and mobile support.

---

## Part C — Architecture

### C.1 Modules

| Module | Contents | Depends on |
|---|---|---|
| `sim` | All game state and rules. Pure Java, no libGDX. | nothing |
| `core` | libGDX game, screens, rendering, UI, data loaders, platform interfaces | `sim`, libGDX |
| `lwjgl3` | Desktop launcher, local log file uploader | `core` |
| `teavm` | Web launcher and web build, Firestore uploader (M5) | `core`, gdx-teavm |

Generate the project with gdx-liftoff (platforms: Desktop LWJGL3 and HTML via TeaVM), then add the `sim` module.

### C.2 Design patterns

- **Observer:** the sim announces what happened through `GameListener`. The UI, logger, and tests subscribe. The sim never calls UI code.
- **Command:** every player action is an `Order` with a tick, which gives replays and logs for free.
- **Strategy:** `Difficulty` is an interface; Easy, Medium, and Hard are configurations of one implementation.
- **State:** libGDX `Game` and `Screen` handle screen flow.
- **Composition:** `FlowerPatch` holds a `FlowerType` (data); species are not subclasses.
- **Event hierarchy:** an abstract `GameEvent` with subclasses for random events.
- **Dependency inversion for platform services:** `LogUploader` is an interface in `core` with per-platform implementations, so the sim and tests never need network access.

### C.3 Determinism

The same seed, map, difficulty, and order list must always produce the same game, on desktop, on the web, and in tests. This is what makes replays, logs, smoke tests, and the skip feature trustworthy. The hard rules in A.5 enforce it.

### C.4 Integer smoothing

Daily totals (age deaths, consumption, egg laying, points) are computed at the start of each day and spread over its ticks:

```java
// t = tick within the day (0..ticksPerDay-1)
long slice = (dailyTotal * (t + 1)) / ticksPerDay - (dailyTotal * t) / ticksPerDay;
```

The slices always add up exactly to `dailyTotal`, with no lost remainders.

### C.5 Game loop and rendering

- `TickDriver` accumulates frame time and runs as many fixed ticks as are due (4 per second × speed). It caps how many ticks can run per frame, so returning to a throttled tab doesn't freeze the game.
- During skip-to-spring, it runs a fixed batch of ticks per frame (D.7) until spring, game over, or a notification that pauses the game.
- The renderer draws every frame and interpolates each group's position between its previous and current tick positions, using the driver's interpolation alpha.
- When libGDX calls `pause()` (tab hidden or window minimized), the driver pauses.

### C.6 Data files

- `assets/data/balance.json`: every tunable number (D.7). Nothing tunable is hard-coded.
- `assets/data/flowers.json`: flower species data.
- `assets/maps/*.tmx`: Tiled maps (conventions in D.6).
- The loaders in `core` turn these into immutable sim objects. Tests build the same objects in code with `TestFixtures`.

### C.7 Logging and analytics

- Every run produces a `GameLog` with:
  - game version, seed, map ID, and difficulty
  - every order as (tick, type, parameters)
  - every UI input that doesn't change the simulation (speed changes, pauses, skips, dismissed popups) as (tick, type)
  - the end-of-run summary
- Because the sim is deterministic, seed + orders reconstructs the whole game, so logs stay small.
- Logs contain no personal information: a random run ID only, no names, accounts, or IP-derived data.
- Upload happens through `LogUploader`. Desktop writes a local JSON file; web uploads to Firebase in Milestone 5 (Part G).

---

## Part D — Project layout and skeleton

### D.1 Dependencies

- libGDX (and its Scene2D UI), via gdx-liftoff
- gdx-teavm (web backend), via gdx-liftoff
- JUnit 5 (tests only)
- A minimal Scene2D `Skin`: libGDX's default `uiskin` assets (Apache-2.0) or a skin built in code

No other dependencies without asking.

### D.2 Repository tree

```
beestinction/
├── CLAUDE.md                     ← this file
├── README.md
├── LICENSE                       ← MIT
├── build.gradle, settings.gradle, gradle.properties, gradlew*
├── .github/workflows/ci.yml
├── .github/workflows/deploy.yml
├── assets/
│   ├── data/balance.json
│   ├── data/flowers.json
│   ├── maps/test_map.tmx
│   └── ui/                       ← skin files
├── sim/src/main/java/com/michiganecodata/beestinction/sim/...
├── sim/src/test/java/com/michiganecodata/beestinction/sim/...
├── core/src/main/java/com/michiganecodata/beestinction/...
├── core/src/test/java/com/michiganecodata/beestinction/...
├── lwjgl3/src/main/java/com/michiganecodata/beestinction/lwjgl3/...
└── teavm/src/main/java/com/michiganecodata/beestinction/teavm/...
```

### D.3 `sim` module classes

Packages are relative to `com.michiganecodata.beestinction.sim`. **Impl** says who writes the method bodies: **CC** = Claude Code, **H** = humans (stub with RME).

#### Root package

| Class | Impl | Purpose and key members |
|---|---|---|
| `Simulation` | CC | Owns `GameState` and the systems. `static Simulation create(GameSetup)`, `void tick()` (D.5), `void submit(Order)` (stamps the current tick and enqueues it), `void recordUiEvent(UiEventType)`, `void addListener(GameListener)`, `GameStateView view()`, `boolean isGameOver()`, `long currentTick()`, `GameLog log()`, `RunSummary summary()`. |
| `GameSetup` | CC | Immutable: seed, `GameMap`, `DifficultyLevel`, `BalanceConfig`, `List<FlowerType>`, game version. |
| `GameState` | CC (container) | Holds `GameClock`, `Hive`, `GameMap`, `FogOfWar`, `DiscoveryLog`, `List<ForagerGroup>`, `Difficulty`, `GameRandom`, `ScoreKeeper`, `StatsRecorder`, the notification outbox (`List<Notification>`), and game-over flag and cause. Getters plus `void postNotification(Notification)`, `void endGame(DeathCause cause)`. |
| `GameStateView` | CC | Read-only interface over `GameState` for the UI and renderer. |
| `GameClock` | CC | `long tick()`, `void advance()`, `int dayOfRun()`, `int tickOfDay()`, `boolean isDayStart()`, `Season season()`, `int dayOfSeason()`, `int year()`, `boolean isSeasonStart()`. Uses ticks per day and days per season from `BalanceConfig`. |
| `Season` | CC | Enum SPRING, SUMMER, FALL, WINTER, with `next()`. |
| `GameListener` | CC | Interface: `onNotification(Notification)`, `onDayStart(int day)`, `onSeasonChanged(Season)`, `onGameOver(RunSummary)`. Default no-op methods. |
| `Notification` | CC | Immutable: `NotificationType type`, `String title`, `String message`, `boolean pausesGame`, `String factId` (nullable), `String abstractionNote` (nullable). |
| `NotificationType` | CC | Enum PESTICIDE_DEATHS, DISEASE_OUTBREAK, FREEZE_DEATHS, WINTER_DIE_OFF, NEW_SPECIES, GAME_OVER. |
| `DeathCause` | CC | Enum OLD_AGE, STARVATION, PESTICIDE, DISEASE, FREEZING, WINTER_DIE_OFF, CONVERTED, QUEEN_STARVED. |

#### `config`

| Class | Impl | Purpose and key members |
|---|---|---|
| `BalanceConfig` | CC | Immutable holder of every D.7 value, built with `BalanceConfig.Builder`. `static BalanceConfig defaults()` returns the D.7 placeholders. |
| `DifficultyLevel` | CC | Enum EASY, MEDIUM, HARD. |
| `DifficultyProfile` | CC | Immutable per-level numbers (D.7). |
| `FlowerType` | CC | Immutable: `id`, `displayName`, `EnumSet<Season> bloomSeasons`, `nectarPerDayPerSize`, `NectarPotential potential`, `factId`. |
| `NectarPotential` | CC | Enum LOW, MEDIUM, HIGH. |

#### `util`

| Class | Impl | Purpose and key members |
|---|---|---|
| `GameRandom` | CC | Seeded deterministic PRNG (PCG32 or SplitMix64). `GameRandom(long seed)`, `int nextInt(int bound)`, `int rangeInclusive(int min, int max)`, `boolean chancePercent(int pct)`, `boolean chanceBasisPoints(int bp)`, `long nextLong()`. |
| `IntMath` | CC | `static long isqrt(long n)`, `static int clamp(int v, int lo, int hi)`, `static int percentOf(int value, int pct)` (floor). |
| `IntSmoothing` | CC | `static long sliceForTick(long dailyTotal, int tickOfDay, int ticksPerDay)` (C.4). |
| `JsonWriter` | CC | Minimal hand-written JSON builder (objects, arrays, strings with escaping, numbers, booleans). |

#### `colony`

| Class | Impl | Purpose and key members |
|---|---|---|
| `AgeHistogram` | H | Bee counts by age in days, with `lifespanDays` slots. Used by the hive and by each forager group. `int total()`, `int count(int age)`, `void addNewborns(int n)`, `int beginDay()` (everyone ages one day; the over-age cohort moves to a "dying today" pool; returns its size), `int removeDyingSlice(int tickOfDay, int ticksPerDay)` (smoothed old-age deaths; returns how many died this tick), `int countAtLeastAge(int age)`, `int countBelowAge(int age)`, `AgeHistogram takeOldest(int n, int minAge)` (removes and returns the n oldest bees at or above `minAge`), `void merge(AgeHistogram other)`, `int killPercent(int pct)` (removes pct% spread proportionally across ages, deterministically; returns the number killed), `int killAll()`. Invariant: no negative counts. |
| `BroodBatch` | H | `int count`, `int devPoints`, `int layDay`. `void addPoints(int p)`, `boolean isReady(int threshold)`. |
| `BroodNursery` | H | `List<BroodBatch>`. `void layEggs(int count, int day)`, `void develop(int nurseCount, BalanceConfig cfg)` (adds base + nurse-bonus points to every batch), `int collectEmerged(int threshold)` (removes ready batches; returns the number of bees), `int total()`, `int killAll()`. |
| `NectarStore` | H | `int amount()`, `int capacity()`, `void setCapacity(int c)`, `int deposit(int n)` (returns the amount accepted; the rest is lost), `int withdraw(int n)` (returns the amount actually withdrawn; never goes negative). |
| `Comb` | H | `int size()`, `int populationCapacity(BalanceConfig)`, `int nectarCapacity(BalanceConfig)`, `void startExpansion(int bees)`, `void progress(int ticks)`, `boolean isExpanding()`. Expansion details are an open decision (Part I). |
| `Queen` | H | `int feedingLevel`, `int consecutiveUnfedDays`. `void setFeedingLevel(int)`, `void recordDay(boolean fullyFed)`, `boolean hasStarved(int limitDays)`, `int dailyEggTarget(BalanceConfig, Season)` (0 in winter). |
| `Hive` | H | Aggregate: `Queen`, `AgeHistogram workers`, `BroodNursery`, `NectarStore`, `Comb`. `int workersInHive()`, `int houseBees(int forageAge)`, `int availableForagers(int forageAge)`, `int population(List<ForagerGroup> away)`. |
| `ColonySystem` | H | Colony rules. `void onDayStart(GameState)` (begin aging, compute daily rations and egg target, record whether the queen was fully fed yesterday, check queen starvation → `endGame`), `void onTick(GameState)` (old-age death slice, feeding slice with the queen first, starvation deaths, egg-laying slice, brood development, emergence, comb progress, updating capacities). |

#### `world`

| Class | Impl | Purpose and key members |
|---|---|---|
| `GameMap` | CC (container) | Immutable: `id`, `displayName`, `description`, `width`, `height`, `TileType tileAt(int x, int y)`, `TilePos hivePos()`, `List<FlowerPatch> patches()`, `int milliMilesPerTile`. |
| `TileType` | CC | Enum (placeholder set): GRASS, FOREST, FARM, ORCHARD, WATER, URBAN, ROAD, WETLAND. |
| `TilePos` | CC | Immutable x, y; `long distanceSquared(TilePos o)`; `equals` and `hashCode`. |
| `FlowerPatch` | H | `id`, `FlowerType type`, `TilePos pos`, `int size`, `int nectarRemainingToday`, `long toxicUntilTick`. `boolean isBlooming(Season)`, `boolean isToxic(long tick)`, `void spray(long tick, int toxicTicks)`, `void resetDailyCapacity(Season s, int yieldPercent)`, `int take(int requested)` (returns the amount actually taken from today's capacity). |
| `FogOfWar` | H | `boolean isRevealed(int x, int y)`, `void revealAround(TilePos center, int radiusTiles)`, `boolean isPatchDiscovered(FlowerPatch p)`. |
| `DiscoveryLog` | H | `boolean markCollected(FlowerType t)` (true the first time a species is collected), `Set<String> discoveredIds()`. |

#### `foraging`

| Class | Impl | Purpose and key members |
|---|---|---|
| `ForagerGroup` | H | `int id`, `AgeHistogram bees`, `TilePos target`, `FlowerPatch targetPatch` (null for empty-tile scouting), `GroupState state`, `boolean standing`, `boolean cancelRequested`, `int nectarCarried`, `int winterTicksOutside`, fixed-point position `posXMilli`, `posYMilli` and `prevXMilli`, `prevYMilli` (for interpolation), `int travelTicksTotal`, `int travelTicksDone`. |
| `GroupState` | CC | Enum OUTBOUND, FORAGING, RETURNING. |
| `ForagingSystem` | H | `void onDayStart(GameState)` (reset patch daily capacities by season and difficulty yield), `void onTick(GameState)` (move groups, arrivals, foraging with capacity sharing and slight randomness, pesticide exposure kills, returns and deposits, standing re-dispatch, fog reveal, first-collection discovery notifications), `int travelTicks(TilePos from, TilePos to, BalanceConfig cfg)`, `ForagerGroup dispatch(GameState, int beeCount, TilePos target, boolean standing)`. |

#### `orders`

| Class | Impl | Purpose and key members |
|---|---|---|
| `Order` | CC (abstract shell) | `long tick` (set by `Simulation.submit`), `abstract OrderType type()`, `abstract boolean validate(GameStateView s)`, `abstract void apply(GameState s)`, `abstract void writeParams(JsonWriter w)`. |
| `OrderType` | CC | Enum FORAGE, RECALL, CANCEL_STANDING, BUILD_COMB, CONVERT_BEES, SET_FEEDING_LEVEL. |
| `ForageOrder` | H (validate/apply) | `beeCount`, `TilePos target`, `boolean standing`. |
| `RecallOrder` | H | `groupId`. |
| `CancelStandingOrder` | H | `groupId`. |
| `BuildCombOrder` | H | `beeCount`. |
| `ConvertBeesOrder` | H | `beeCount`. Valid only in the conversion window. |
| `SetFeedingLevelOrder` | H | `level`. |
| `OrderQueue` | CC | `void enqueue(Order o)`, `List<Order> drainForTick(long tick)` (preserves submission order). |

#### `events`

| Class | Impl | Purpose and key members |
|---|---|---|
| `GameEvent` | CC (abstract shell) | `abstract EventType type()`, `abstract void apply(GameState s)`. |
| `EventType` | CC | Enum PESTICIDE_SPRAY, DISEASE_OUTBREAK. |
| `PesticideSprayEvent` | H | `patchId`. Applies toxicity (spray or respray). No notification at spray time. |
| `DiseaseOutbreakEvent` | H | Kills 40–70% of workers (hive and groups away), with severity from `Difficulty`. Posts DISEASE_OUTBREAK. |
| `EventScheduler` | H | `List<GameEvent> rollDailyEvents(GameState s)`: none during the grace period or winter; chances from `Difficulty`; picks targets with `GameRandom`. |

#### `difficulty`

| Class | Impl | Purpose and key members |
|---|---|---|
| `Difficulty` | CC (interface) | `boolean inGracePeriod(int day)`, `int escalation(int day)`, `int scoreMultiplierPercent(int day)`, `int eventChanceBasisPoints(EventType t, int day)`, `int severityShiftPercent(int day)`, `int nectarYieldPercent(int day)`, `int extraMortalityBasisPoints(int day)`. |
| `ScalingDifficulty` | H | Implements `Difficulty` from a `DifficultyProfile` (base + escalation over time). `static Difficulty forLevel(DifficultyLevel, BalanceConfig)` is CC. |

#### `season`

| Class | Impl | Purpose and key members |
|---|---|---|
| `WinterSystem` | H | `void onSeasonStart(GameState, Season)`, `void onDayStart(GameState)` (day-4 die-off with WINTER_DIE_OFF notification), `void onTick(GameState)` (freezing of groups outside more than 2 winter days, with FREEZE_DEATHS notification), `boolean isConversionWindowOpen(GameClock)`, `int conversionValue(int beeCount, GameRandom, BalanceConfig)`, `boolean isSkipAvailable(GameStateView)`, `boolean broodDiesOnEmergence(Season)`. |

#### `score`

| Class | Impl | Purpose and key members |
|---|---|---|
| `ScoreKeeper` | H | `long points()`, `void onDayStart(GameState)` (daily points = population × multiplier), `void onTick(GameState)` (adds the smoothed slice). |

#### `stats`

| Class | Impl | Purpose and key members |
|---|---|---|
| `StatsRecorder` | CC | `void recordDay(GameStateView)`, `void recordDeaths(DeathCause, int)`, `void recordNectarCollected(int)`, `RunSummary buildSummary(GameStateView)`. Humans call `recordDeaths` from their rules. |
| `RunSummary` | CC | Immutable: score, days survived, cause of game over, peak population, daily population samples, deaths by cause, total nectar collected, species discovered. `void write(JsonWriter)`. |

#### `log`

| Class | Impl | Purpose and key members |
|---|---|---|
| `GameLog` | CC | Version, run ID (random, from `GameRandom` seeded separately from the game seed), seed, map ID, difficulty, `List<OrderRecord>`, `List<UiEventRecord>`, `RunSummary`. `String toJson()`. |
| `OrderRecord` | CC | Tick, type, accepted flag, params JSON. |
| `UiEventRecord` | CC | Tick, `UiEventType`. |
| `UiEventType` | CC | Enum SPEED_1X, SPEED_2X, SPEED_3X, PAUSE, RESUME, SKIP_START, SKIP_END, POPUP_DISMISSED. |

#### `replay`

| Class | Impl | Purpose and key members |
|---|---|---|
| `ReplayRunner` | CC | `static RunSummary replay(GameSetup setup, List<Order> orders, long maxTicks)`: runs a fresh `Simulation`, submitting each order at its tick. |

### D.4 `core`, `lwjgl3`, and `teavm` classes

| Class | Module and package | Impl | Purpose |
|---|---|---|---|
| `BeestinctionGame` | core, root | CC | `extends Game`. Holds `GameServices`, loads data, opens `MainMenuScreen`. |
| `GameServices` | core, root | CC | `LogUploader`, `MapCatalog`, `BalanceConfig`, flower types, app version. |
| `AppVersion` | core, root | CC | Version string constant (also written into logs). |
| `MainMenuScreen` | core, `screens` | CC placeholder | Title, Play button, desktop notice, anonymous-data notice. |
| `MapSelectScreen` | core, `screens` | CC placeholder | Map list with descriptions plus Easy/Medium/Hard, then Start. |
| `GameScreen` | core, `screens` | CC wiring, H polish | Creates the `Simulation`, `TickDriver`, renderers, HUD, and `UiGameListener`; routes input to orders. |
| `SummaryScreen` | core, `screens` | CC placeholder | Shows the `RunSummary`, triggers log upload, returns to the menu. |
| `TickDriver` | core, `loop` | CC | Pure Java (no Gdx calls, testable). `void update(float deltaSeconds)`, `SpeedSetting cycleSpeed()`, `void setPaused(boolean)`, `boolean isPaused()`, `void startSkip()`, `boolean isSkipping()`, `float interpolationAlpha()`. Drives a `TickTarget` interface (`tick()`, `isGameOver()`, `hasPendingPause()`, `isSpring()`). |
| `SpeedSetting` | core, `loop` | CC | Enum X1, X2, X3 with `next()` and `multiplier()`. |
| `WorldRenderer` | core, `render` | CC placeholder | Terrain, patches, hive, and fog as shapes. |
| `GroupRenderer` | core, `render` | CC placeholder | Bee groups as dots at interpolated positions. |
| `Hud`, `AgePipelinePanel`, `OrderPanel`, `GroupListPanel`, `FeedingControl`, `CombControl`, `ConversionControl`, `SkipButton`, `SpeedToggleButton`, `NotificationDialog`, `FirstRunTips` | core, `ui` | CC placeholder, H polish | Scene2D widgets bound to `GameStateView`; they submit orders through `Simulation`. |
| `UiGameListener` | core, `bridge` | CC | Implements `GameListener`: updates widgets, opens `NotificationDialog` and pauses the driver when `pausesGame`, records POPUP_DISMISSED, goes to the summary on game over. |
| `BalanceLoader` | core, `data` | CC | `JsonReader` → `BalanceConfig`. |
| `FlowerTypeLoader` | core, `data` | CC | `JsonReader` → `List<FlowerType>`. |
| `TiledMapConverter` | core, `data` | CC | libGDX `TmxMapLoader` → `GameMap` (D.6 conventions). |
| `MapCatalog` | core, `data` | CC | The list of available maps with display metadata. |
| `LogUploader` | core, `platform` | CC | Interface: `void uploadRunStart(GameLog partial)`, `void uploadRunLog(GameLog complete)`. Must never block or crash the game. |
| `NoOpLogUploader` | core, `platform` | CC | Does nothing (tests, fallback). |
| `Lwjgl3Launcher` | lwjgl3 | CC | Desktop entry point (from gdx-liftoff). |
| `LocalFileLogUploader` | lwjgl3 | CC | Writes logs as JSON files to a local `logs/` folder. |
| `TeaVMLauncher` | teavm | CC | Web entry point (use the gdx-liftoff name). |
| `FirestoreLogUploader` | teavm | stub | Milestone 5; the humans will build it with Claude's help. Until then, use `NoOpLogUploader` on web. |

### D.5 Order of operations in `Simulation.tick()`

```
tick():
  if gameOver: return
  t = clock.tick()

  if clock.isDayStart():
      if clock.isSeasonStart():
          winter.onSeasonStart(state, clock.season()); notify onSeasonChanged
      colony.onDayStart(state)        // aging begins, daily rations, egg target, queen-starvation check
      foraging.onDayStart(state)      // reset patch capacities
      winter.onDayStart(state)        // winter day counting, day-4 die-off
      score.onDayStart(state)         // compute today's point total
      for e in scheduler.rollDailyEvents(state): e.apply(state)
      stats.recordDay(state); notify onDayStart

  for order in orderQueue.drainForTick(t):
      accepted = order.validate(view)
      if accepted: order.apply(state)
      log.recordOrder(order, accepted)

  colony.onTick(state)                // death slice, feeding (queen first), laying, brood, emergence, comb
  foraging.onTick(state)              // movement, foraging, pesticide exposure, deposits, fog, discoveries
  winter.onTick(state)                // freezing outside
  score.onTick(state)

  dispatch the outbox notifications to listeners in order, then clear the outbox
  if state.isGameOver(): build the summary, notify onGameOver
  clock.advance()
```

Systems never call listeners directly. They post to the outbox, and `Simulation` dispatches after the tick.

### D.6 Data formats

**`balance.json`:** a flat object whose keys match the D.7 names, plus a `difficulty` object with `easy`, `medium`, and `hard` profiles.

**`flowers.json`:**

```json
{
  "flowers": [
    { "id": "clover", "displayName": "White Clover", "bloomSeasons": ["SPRING", "SUMMER"],
      "nectarPerDayPerSize": 10, "potential": "MEDIUM", "factId": null }
  ]
}
```

Placeholder species (bloom seasons are approximate, and the team should verify them): dandelion (spring), apple blossom (spring), white clover (spring–summer), pumpkin (summer), wild bergamot (summer), goldenrod (fall), New England aster (fall).

**Tiled maps (`assets/maps/*.tmx`):**
- Map properties: `id`, `displayName`, `description`, `milliMilesPerTile`.
- Tile layer `terrain`: each tile has a string property `type` matching a `TileType` name.
- Object layer `patches`: point objects with properties `species` (a flower ID) and `size` (int).
- Object layer `hive`: exactly one point object.
- Size: the map covers at least the 4-mile radius around the hive.
- `test_map.tmx` can be small, with a few patches, purely for the skeleton and manual testing.

### D.7 Balance parameters (placeholders, not tuned)

Values marked TBD are guesses to be tuned with headless simulations. Do not tune them during setup.

| Key | Placeholder | Notes |
|---|---|---|
| `ticksPerSecond` | 4 | Fixed by design |
| `ticksPerDay` | 35 | Fixed by design |
| `daysPerSeason` | 28 | Fixed by design |
| `lifespanDays` | 42 | Fixed by design |
| `forageAgeDays` | 21 | TBD |
| `startingWorkers` | 50 | TBD |
| `startingNectar` | 500 | TBD |
| `queenDailyRation` | 5 | TBD |
| `workerDailyRation` | 1 | TBD |
| `queenStarvationDays` | 2 | Fixed by design |
| `unfedWorkerDeathPercent` | 20 | TBD; per day without food |
| `eggsPerDayBase` | 10 | TBD |
| `eggsPerDayPerFeedingLevel` | 5 | TBD |
| `maxFeedingLevel` | 3 | TBD |
| `raiseCostPerBee` | 4 | TBD; nectar cost to raise one bee |
| `broodThresholdPoints` | 700 | TBD |
| `broodBasePointsPerTick` | 1 | TBD |
| `broodNurseBonusPer100Nurses` | 1 | TBD |
| `combStartSize` | 1 | TBD |
| `combPopulationPerSize` | 200 | TBD |
| `combNectarPerSize` | 1000 | TBD |
| `combExpansionNectarCost` | 800 | TBD; "expensive" |
| `combExpansionBeeTicks` | 3500 | TBD |
| `milliTilesPerTick` | 50 | TBD; tune so round trips take about a couple of days |
| `fogRevealRadiusTiles` | 3 | TBD |
| `startingRevealRadiusTiles` | 6 | TBD |
| `yieldRandomPercent` | 10 | TBD; ±% per collection |
| `pesticideKillPercentMin` / `Max` | 70 / 80 | Fixed by design |
| `pesticideToxicDays` | 14 | Fixed by design |
| `diseaseKillPercentMin` / `Max` | 40 / 70 | Fixed by design |
| `gracePeriodDays` | 14 | TBD |
| `conversionWindowStartDayOfFall` | 14 | "Mid-fall"; TBD |
| `conversionValuePercentMin` / `Max` | 10 / 30 | Fixed by design |
| `freezeAfterWinterDays` | 2 | Fixed by design |
| `winterDieOffDay` | 4 | Fixed by design |
| `skipTicksPerFrame` | 200 | TBD |
| `maxCatchUpTicksPerFrame` | 12 | TBD |
| `difficulty.easy` / `medium` / `hard` | `baseMultiplierPercent` 100 / 150 / 200, `escalationPercentPerWeek` 5 / 8 / 12, `yieldPercent` 110 / 100 / 85, `eventBaseBasisPoints` 50 / 100 / 150 | All TBD |

---

## Part E — Testing

### E.1 Setup

- JUnit 5 in `sim` (most tests) and `core` (`TickDriver` only; no libGDX needed).
- Test class tags:
  - `@Tag("pending")`: the feature isn't implemented yet. Excluded from CI.
  - `@Tag("smoke")`: long-running headless games.
- Gradle tasks:
  - `test`: excludes `pending` and `smoke`. This is what CI requires.
  - `pendingTest`: runs only `pending`, for local work in progress.
  - `smokeTest`: runs `smoke` tests that are not also `pending`.
- **Workflow for humans:** implement a class → remove `@Tag("pending")` from its test class → run its tests locally → open a pull request. Once a test class is no longer pending, CI requires it to pass.
- Tests for Claude-implemented infrastructure are **not** pending and must pass from day one.

### E.2 Test helpers (`sim/src/test/.../testutil`)

- `TestFixtures`:
  - `BalanceConfig.defaults()` variants
  - a tiny `GameMap` built in code (hive plus a few patches at known distances)
  - flower types
  - fixed seeds
  - `newSimulation(seed)`
- `RandomPlayer`: a bot that issues random **valid** orders using its own seeded `GameRandom`.
- `InvariantChecker`: asserts the invariants in E.4 on a `GameStateView`.

### E.3 Test classes and cases

Infrastructure (not pending):

- `GameRandomTest`:
  - the same seed gives the same sequence
  - different seeds give different sequences
  - `rangeInclusive` stays in bounds, including min == max
  - `chancePercent(0)` is always false and `chancePercent(100)` is always true
- `IntSmoothingTest`:
  - slices sum exactly to the total for totals 0–10,000 at 35 ticks per day
  - every slice is ≥ 0
  - slices differ by at most 1
- `GameClockTest`:
  - day, week, and season boundaries at 35, 245, and 980 ticks
  - a year is 3,920 ticks
  - season order and wraparound
  - `isDayStart` and `isSeasonStart`
- `OrderQueueTest`: orders are drained only at their tick, in submission order.
- `GameLogTest`:
  - JSON contains the version, seed, map, difficulty, orders, UI events, and summary
  - no personal fields
  - the run ID is not the game seed
- `ReplayDeterminismTest`:
  - the same setup and orders give an identical `RunSummary` twice
  - running ticks one at a time versus in batches gives identical results (the skip guarantee)
- `TickDriverTest` (core):
  - speed cycles 1x → 2x → 3x → 1x
  - paused runs zero ticks
  - catch-up is capped per frame
  - skip runs batches and stops at spring, game over, or a pending popup
  - alpha is in [0, 1)

Game rules (pending until implemented):

- `AgeHistogramTest`:
  - `beginDay` shifts ages and moves the over-age cohort to dying
  - dying slices sum to the cohort size over a day
  - `takeOldest` takes the oldest first and respects `minAge`
  - `merge` conserves totals
  - `killPercent` stays within rounding and never goes negative
- `BroodNurseryTest`:
  - a batch emerges at the threshold
  - more nurses means fewer ticks to emerge
  - zero nurses still develops at base speed
  - `collectEmerged` removes only ready batches
- `NectarStoreTest`: deposits are capped and overflow is lost; withdrawals never go negative.
- `QueenTest`:
  - game over after exactly 2 consecutive unfed days
  - a fed day resets the counter
  - no eggs in winter
  - a higher feeding level means more eggs
- `ColonySystemTest`:
  - the queen is fed before workers when nectar is short
  - unfed workers die
  - old-age deaths are spread across the day
  - emerged bees join age 0
  - population never exceeds comb capacity
- `CombTest`: expansion costs nectar up front; capacities grow when it completes.
- `FlowerPatchTest`:
  - blooms only in its seasons
  - toxic for 14 days after a spray
  - a respray resets the timer
  - `take` never exceeds today's remaining capacity
- `ForagingSystemTest`:
  - travel time grows with distance
  - a round trip to a mid-distance patch takes about 2 days (with test config)
  - capacity is shared among groups at one patch
  - yield stays within ±`yieldRandomPercent`
  - deposits respect capacity
  - standing orders re-dispatch survivors and never refill
  - recall turns the group around
  - bees age during trips
- `PesticideTest`:
  - foraging at a toxic patch kills 70–80% of exposed bees
  - the notification is posted only after the deaths
  - a non-toxic patch kills none
- `DiseaseTest`:
  - kills 40–70% of workers in the hive and away
  - the queen is unaffected
  - never during the grace period or winter
- `DifficultyTest`:
  - escalation rises over time for every level
  - Hard yields less than Easy
  - the score multiplier is higher on harder levels and rises over time
- `WinterSystemTest`:
  - the conversion window opens only from mid-fall to winter start
  - the conversion value is 10–30% of raise cost
  - groups outside more than 2 winter days freeze
  - all workers die on winter day 4
  - brood emerging in winter dies
  - no egg laying in winter
  - skip is available only after the die-off
  - the queen keeps eating through winter
- `FogOfWarTest`: reveal radius around a point; patches inside revealed areas count as discovered.
- `DiscoveryLogTest`: the first collection per species returns true; later ones return false.
- `ScoreKeeperTest`: daily points = population × multiplier; slices sum to the daily points.
- `OrderValidationTest`:
  - rejects counts greater than available foragers
  - rejects conversion outside the window
  - rejects unknown group IDs
  - accepted orders change state only at their tick

Smoke (tagged `smoke` and `pending` until Milestone 1 is complete):

- `SmokeTest`:
  - 200 seeds × `RandomPlayer` on `TestFixtures` maps (and on `test_map` through a pure-Java fixture copy)
  - runs until game over or a cap of 5 in-game years
  - asserts no exceptions and checks `InvariantChecker` every day

### E.4 Invariants

- No negative counts anywhere (bees, brood, nectar, capacity).
- Nectar ≤ storage capacity.
- Total population ≤ population capacity.
- Conservation: births − deaths (all causes, including conversion) = change in total workers.
- At most one group per ID, and every group has bees > 0 or is removed.
- After game over, the state doesn't change.

---

## Part F — CI/CD and deployment

- **Repository:** public on GitHub, with an MIT license. Only commit art, fonts, and sounds whose licenses allow redistribution.
- **`ci.yml`** runs on every pull request to `main`:
  1. checkout
  2. set up JDK 21 (Temurin)
  3. set up Gradle with caching (`gradle/actions/setup-gradle`)
  4. `./gradlew build` (compile + `test`)
  5. `./gradlew smokeTest`
  6. `./gradlew teavm:build`
- **Branch protection** (the humans set this in GitHub settings; document the steps in the README):
  - require a pull request before merging into `main`
  - require the CI job to pass
  - no direct pushes to `main`
- **`deploy.yml`** runs on every push to `main`:
  1. build the TeaVM web app
  2. upload the output folder (`teavm/build/dist/webapp`; verify the actual path) with `actions/upload-pages-artifact`
  3. publish with `actions/deploy-pages`
  - Repo settings: Pages source = GitHub Actions.
- The game is hosted on **GitHub Pages**, not Firebase Hosting, because the Firebase free tier's hosting bandwidth is too small.

---

## Part G — Analytics backend (Milestone 5; not part of the initial setup)

The humans will set this up with Claude's help. Recorded here so the skeleton fits it:

- **Firebase on the free Spark plan only.** No Cloud Functions, since they aren't available on Spark. The game writes directly to Cloud Firestore.
- **Anonymous Authentication** (free) gives each browser a random ID with no personal information.
- **Collections:**
  - `runStarts`: a small document when a run begins (version, seed, map, difficulty)
  - `runLogs`: the full `GameLog` when a run ends
  - Abandoned runs appear as starts without logs, which shows where players quit.
- **Security Rules:**
  - both collections are create-only for authenticated users
  - no client reads, updates, or deletes
  - validate field names, types, and sizes
- **Quotas:** Spark allows 20,000 document writes per day, and the design uses about two per run.
- **Secrets:**
  - The Firebase web config (API key, project ID) is not secret and can be committed.
  - **Never commit a service-account key file.**
- The start screen shows a one-line notice that anonymous gameplay data is collected.
- The web `LogUploader` implementation (Firestore REST API or TeaVM JavaScript interop) will be decided in Milestone 5.

---

## Part H — Milestones

Each milestone ends with a playable build deployed to GitHub Pages.

0. **Walking skeleton** (Claude Code setup + humans review):
   - modules, the skeleton from this spec, and placeholder screens
   - CI and deploy working
   - a live link showing the test map
   - proves the TeaVM build works before any game logic is written
1. **Core loop:**
   - one Tiled map
   - forage, standing, and recall orders
   - nectar, age cohorts and roles, the queen and feeding level, brood batches
   - game over and the speed toggle
   - remove the `pending` tags as features land, and turn on `smokeTest`
2. **Seasons:** bloom calendar, winter rules, conversion, skip to spring.
3. **Threats:** `Difficulty`, grace period, pesticide, disease, popups.
4. **Exploration and growth:** fog of war, discovery popups, comb expansion.
5. **Analytics:** Firebase logs and the end-of-run summary screen.
6. **Facts in the UI:** move facts from the Google Doc into game data with citations; bundle abstraction notes into the related popups.

---

## Part I — Open decisions and assumptions

Assumptions this spec makes (confirm or change them):

1. The run starts at the beginning of spring.
2. Comb expansion uses house bees for a build period, with the nectar cost paid up front. Bee count and duration are in D.7.
3. Fog is revealed within a radius of each group's position as it travels, plus a starting radius around the hive.
4. Nectar delivered beyond storage capacity is lost.
5. The conversion value is rolled within 10–30% for each conversion order.
6. A forage order to an empty, unexplored tile is valid ("scouting"); the group returns with no nectar.
7. The skip is a UI fast-forward, not an order; it is logged as a UI event.
8. The age threshold for foraging is 21 days (a placeholder; bumblebee roles are simplified to age).

Still to decide (tune with headless simulations):

- All TBD values in D.7, especially travel speed, rations, egg rates, comb costs, and the difficulty curves.
- How the feeding level maps to egg laying and extra consumption.
- Exact map designs for the 1–3 maps and the playstyle each one encourages.
- Final flower species list and bloom calendar (verify against Michigan sources).
- Fact content, kept in the team Google Doc until Milestone 6.
