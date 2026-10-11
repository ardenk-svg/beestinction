# Beestinction

We will be creating a game showcasing the fragility and importance of bees in the ecosystem, especially in Michigan. The game will feature the impact of pesticides and invasive species on bee populations.

Our long term approach for this project is to spread awareness about the effects of pesticides and all of the benefits that come from bees in the ecosystem. We hope that by presenting it as a video game format, children will be more inclined to interact and learn more about bees, pesticides, and ultimately the environment.

Beestinction is a single-player bumblebee colony survival game by Michigan EcoData, written in Java with libGDX and playable in the browser through a TeaVM web build hosted on GitHub Pages. The full game and architecture spec is in [CLAUDE.md](CLAUDE.md); this README covers how to work in the repository.

**Status: Milestone 0 (walking skeleton).** The project, build, architecture, data loading, placeholder UI, tests, and CI are in place. The game rules are stubs for the team to implement (see [What is implemented](#what-is-implemented-and-what-is-yours)).

---

## Quick start

You need **JDK 17 or newer** (CI uses Temurin 21). Gradle is included through the wrapper (`./gradlew`), so you do not need to install it.

| Task | Command |
|---|---|
| Build everything and run the required tests | `./gradlew build` |
| Run the required tests only | `./gradlew test` |
| Run tests for features not implemented yet | `./gradlew pendingTest` |
| Run the long headless smoke tests | `./gradlew smokeTest` |
| Play on desktop | `./gradlew lwjgl3:run` |
| Build the web version | `./gradlew teavm:build` (output: `teavm/build/dist/wasm/release/webapp`) |
| Play the web version locally | `./gradlew teavm:run`, then open http://localhost:8181/ |
| Web version with readable stack traces | `./gradlew teavm:gdx_teavm_web_js_run` (unobfuscated JavaScript, auto-rebuilds) |
| Runnable desktop JAR | `./gradlew lwjgl3:jar` (output: `lwjgl3/build/libs/Beestinction-0.1.0.jar`) |

On Windows outside WSL use `gradlew.bat` instead of `./gradlew`.

**WSL setup on this machine:** JDK 21 (Temurin) is installed in `~/.local/share/jdks/temurin-21`, and `JAVA_HOME` is set in `~/.bashrc` and `~/.bash_profile` (look for the "JDK for beestinction" block). Open a new terminal (or reload the VS Code window) so it takes effect. `./gradlew lwjgl3:run` opens a window through WSLg; Mesa may print `libEGL warning` lines, which are harmless.

**IDEs:** VS Code with the "Extension Pack for Java" imports the Gradle project automatically when you open the repository folder. IntelliJ IDEA: *File > Open* the repository folder and accept the Gradle import. The `sim` module has no libGDX dependency, so you can run its tests without a window.

**The web build needs a browser with WebAssembly GC** (current Chrome, Edge, Firefox, Safari 18.2+). Browsers pause games in background tabs; that is expected (the game auto-pauses when hidden).

---

## Module layout

| Module | Contents | Depends on |
|---|---|---|
| `sim` | All game state and rules. Pure Java, **no libGDX**, integer-only, deterministic. | nothing |
| `core` | libGDX game: screens, rendering, placeholder UI, data loaders, platform interfaces, the game loop (`TickDriver`) | `sim`, libGDX |
| `lwjgl3` | Desktop launcher, `LocalFileLogUploader` (writes `logs/*.json`) | `core` |
| `teavm` | Web launcher (TeaVM / gdx-teavm), `FirestoreLogUploader` stub (Milestone 5) | `core` |

```
assets/          data/balance.json, data/flowers.json, maps/test_map.tmx (+ tileset), ui/ (skin)
sim/src/main     com.michiganecodata.beestinction.sim.{colony, config, difficulty, events, foraging, log, orders,
                 replay, score, season, stats, util, world}
sim/src/test     tests for all of the above, plus testutil/ (TestFixtures, RandomPlayer, InvariantChecker)
core/src/main    com.michiganecodata.beestinction.{bridge, data, loop, platform, render, screens, ui}
core/src/test    TickDriverTest, DataFilesTest
tools/           generate_test_map.py (makes the placeholder map and its Java copy)
.github/         ci.yml (pull requests), deploy.yml (GitHub Pages)
```

How a tick flows (CLAUDE.md C and D.5): the UI submits `Order`s to `Simulation.submit()`, which stamps them with the current tick. `TickDriver` calls `Simulation.tick()` 4 times per second times the speed. `tick()` runs the systems in a fixed order, applies the orders for that tick, records them in the `GameLog`, and then hands the notifications that systems posted to every `GameListener` (the UI listens through `UiGameListener`). The renderer draws every frame, interpolating bee groups between ticks.

A few wiring details worth knowing:
- `GameState` holds the rule systems in a `GameRules` bundle so that orders (for example `ForageOrder.apply` calls `s.rules().foraging().dispatch(...)`) and the read-only `GameStateView` can use them. `Simulation` drives them in the D.5 order.
- Each run works on its own copy of the map (`GameMap.freshCopy()`), because flower patches hold per-run state.
- The UI only reads the game through `GameStateView` and only changes it through `Order`s.

---

## What is implemented, and what is yours

Claude Code built the infrastructure and wiring with significant human guidance. **The game rules are stubs** marked `// TODO(human): implement per RME above.` Every stub returns a neutral value (0, `false`, an empty list, `null` for `dispatch`) and never throws, so the game runs end to end today: you can click through every screen, time passes, and orders are submitted, validated (rejected, for now), and logged. Numbers stay at 0 until the colony rules exist.

Find all the stubs:

```bash
grep -rn "TODO(human)" sim core teavm --include=*.java
```

Rules of thumb used in the skeleton: constructors, fields, and plain getters are implemented; anything with a rule in it is a stub. The stubs (all in `sim` unless noted):

- **colony:** `AgeHistogram`, `BroodBatch`, `BroodNursery`, `NectarStore`, `Comb`, `Queen`, `Hive` (counts), `ColonySystem` (including `initializeColony`, the starting colony)
- **world:** `FlowerPatch` (bloom, toxicity, daily capacity, `take`), `FogOfWar`, `DiscoveryLog`
- **foraging:** `ForagingSystem` (`dispatch`, `travelTicks`, movement, foraging, deposits, pesticide deaths, standing orders, fog, discoveries)
- **orders:** `validate()` and `apply()` of every order
- **events:** `PesticideSprayEvent.apply`, `DiseaseOutbreakEvent.apply`, `EventScheduler.rollDailyEvents`
- **difficulty:** `ScalingDifficulty` (the curves)
- **season:** `WinterSystem`
- **score:** `ScoreKeeper`
- **teavm:** `FirestoreLogUploader` (Milestone 5)
- The real UI layout and art (the current screens are placeholders).

### The RME comment convention

Every class has an OVERVIEW / INVARIANTS / COLLABORATORS comment, and every public or protected method has a REQUIRES / MODIFIES / EFFECTS comment (CLAUDE.md A.4). Implement each stub to match its RME, and keep the RME up to date if you change what a method does.

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

### Hard rules for code in `sim` (CLAUDE.md A.5)

No `float` or `double` (use percent, basis points, or milli-units); randomness only through `GameRandom`; no wall-clock time; no `HashMap`/`HashSet` (use `ArrayList`, `LinkedHashMap`, `TreeMap`, `EnumMap`); no threads; no reflection-based serialization; no libGDX. **`HardRulesTest` checks these automatically** and names the file and line if something slips in. Check [TeaVM's class library coverage](https://teavm.org/jcl-report/recent/jcl.html) before using newer JDK APIs anywhere in `sim` or `core`.

---

## Testing

Tests use JUnit 5 and tags:

- `@Tag("pending")`: the feature is not implemented yet. **Excluded from `./gradlew test` and CI.**
- `@Tag("smoke")`: long headless games (`SmokeTest`: 200 seeds x `RandomPlayer` x 2 maps, up to 5 in-game years, checking `InvariantChecker` every day).

**Workflow:** implement a class → delete `@Tag("pending")` from its test class → `./gradlew test` (or run that test class from your IDE) → open a pull request. Once a test class is no longer pending, CI requires it to pass.

| Test classes | Status |
|---|---|
| `GameRandomTest`, `IntSmoothingTest`, `IntMathTest`, `JsonWriterTest`, `GameClockTest`, `OrderQueueTest`, `GameLogTest`, `ReplayDeterminismTest`, `SimulationTest`, `BalanceConfigTest`, `GameMapTest`, `HardRulesTest`, `TestMapCopyTest` (sim); `TickDriverTest`, `DataFilesTest` (core) | Infrastructure. Pass today; required by CI. |
| `AgeHistogramTest`, `BroodNurseryTest`, `NectarStoreTest`, `QueenTest`, `ColonySystemTest`, `CombTest`, `FlowerPatchTest`, `FogOfWarTest`, `DiscoveryLogTest`, `ForagingSystemTest`, `PesticideTest`, `DiseaseTest`, `DifficultyTest`, `WinterSystemTest`, `ScoreKeeperTest`, `OrderValidationTest` | `pending`: they fail against the stubs until you implement the rules. |
| `SmokeTest` | `smoke` and `pending` until Milestone 1 is done; then remove `pending` so CI runs it. |

Test helpers live in `sim/src/test/.../testutil`:
- `TestFixtures`: default and special configs, the placeholder flower species, a tiny 21x21 map with patches at known distances, the pure-Java copy of `test_map`, fixed seeds, `newSimulation(seed)`, `newState(seed)` (a bare state with an empty hive), and `runRules(state, ticks)` (runs the systems in D.5 order without the UI).
- `RandomPlayer`: a bot that issues random valid orders from its own seed.
- `InvariantChecker`: asserts the CLAUDE.md E.4 invariants (no negatives, nectar and population within capacity, births minus deaths equals the change in workers, unique non-empty groups, nothing changes after game over).

`ReplayDeterminismTest` is the guard for determinism: the same setup and orders must give an identical summary, and running ticks one at a time or in batches (what skip-to-spring does) must give identical games. Keep it green.

---

## Playing the skeleton

Main menu → map select (map + difficulty) → game → summary → main menu.

- Click a tile or patch, type a bee count, then **Send** (one trip) or **Standing** (repeat trips). Groups out, feeding level, build comb, convert bees (fall only), and skip to spring (after the winter die-off) are in the side panel.
- **Keys** (when no text box has focus): `Space` pause/resume, `S` cycle speed 1x → 2x → 3x. Debug keys: `F` toggles the fog overlay, `E` ends the run (shows the game-over popup, then the summary).
- The fog is drawn semi-transparent on purpose, because `FogOfWar` is still a stub and reports every tile as hidden.
- Desktop runs write logs to `logs/<runId>-start.json` and `logs/<runId>.json` (git-ignored). The web build does not upload logs until Milestone 5.

## Data files and maps

- `assets/data/balance.json`: every tunable number (CLAUDE.md D.7). It must contain every key; `DataFilesTest` checks that it parses and matches `BalanceConfig.defaults()`. The values are untuned placeholders; if you tune one, change both (or update that test).
- `assets/data/flowers.json`: species. Bloom seasons are approximate and the nectar numbers and potentials are placeholders; verify against Michigan sources.
- `assets/maps/*.tmx`: Tiled maps. Conventions (CLAUDE.md D.6): map properties `id`, `displayName`, `description`, `milliMilesPerTile` (int); a tile layer `terrain` whose tiles each have a string property `type` (a `TileType` name); an object layer `patches` with point objects having `species` (a flower id) and `size` (int); an object layer `hive` with exactly one point object. **To add a map**, draw it in [Tiled](https://www.mapeditor.org/), save it in `assets/maps/`, and add its path to `MapCatalog.MAP_FILES`.
- `test_map.tmx` (40 x 40 tiles at 0.2 miles per tile, hive in the middle) and its tileset are generated by `python3 tools/generate_test_map.py`, which also writes the pure-Java copy used by headless tests. `TestMapCopyTest` fails if the two drift apart.

---

## CI/CD

- **`.github/workflows/ci.yml`** runs on every pull request to `main`: JDK 21 (Temurin), Gradle with caching, `./gradlew build`, `./gradlew smokeTest`, `./gradlew teavm:build`.
- **`.github/workflows/deploy.yml`** runs on every push to `main`: builds the web app and publishes `teavm/build/dist/wasm/release/webapp` to GitHub Pages. The site will be at `https://<owner>.github.io/beestinction/`.

**One-time GitHub settings** (a repository admin does these):

1. *Settings > Pages > Build and deployment > Source*: **GitHub Actions**.
2. *Settings > Branches* (or *Rules > Rulesets*) > add a rule for `main`:
   - **Require a pull request before merging.**
   - **Require status checks to pass**, and select the check **"Build, test, and web build"** (it appears after CI has run once on a pull request).
   - **Do not allow bypassing the above settings** / block direct pushes to `main`.

---

## Open decisions

Choices made during setup where the spec was ambiguous. Each one has a `DECISION:` comment in the code (`grep -rn "DECISION" --include=*.java --include=*.gradle .`). Confirm or change them.

**Assumptions from CLAUDE.md Part I that the skeleton follows:** the run starts at the beginning of spring; comb expansion uses house bees for a build period with the nectar cost paid up front (`Comb`: bee-ticks of work reaching `combExpansionBeeTicks`); fog is revealed around traveling groups plus a starting radius around the hive; nectar beyond storage is lost; the conversion value is rolled once per conversion order; forage orders to empty tiles are valid scouting trips; skip-to-spring is a UI fast-forward logged as a UI event; the forage age is 21 days.

**Setup decisions:**

1. **Java 17 language level in every module** (`build.gradle`). gdx-liftoff defaults core to Java 8 but requires at least 17 for the TeaVM module; one level everywhere keeps things simple. CI and the local setup use JDK 21.
2. **Starting colony** (`ColonySystem.initializeColony`, RME): starting workers are spread evenly across all ages, so some can forage on day 1.
3. **Population** (`Hive`) counts adult workers in the hive and away; the queen and brood do not count toward population or its capacity.
4. **Old-age "dying" pool** (`AgeHistogram`): bees that reach their lifespan die over the course of that day and are never counted as available foragers.
5. **One brood batch per day** (`BroodNursery.layEggs`).
6. **Queen priority over the whole day** (`ColonySystem.onTick` RME): workers can only eat nectar not needed for the rest of the queen's ration.
7. **Recalling a standing group also cancels it** (`RecallOrder`).
8. **Winter day numbering** (`WinterSystem`): winter day 1 is the first day of winter, so the "day 4" die-off is at the start of the fourth winter day.
9. **Days survived** counts completed days (`StatsRecorder.buildSummary`).
10. **Ending a run early** (debug key `E`) uses a `null` game-over cause rather than adding a `DeathCause` value; the summary shows "Ended early".
11. **The systems finish the tick in which the game ends**; the state is frozen from the next tick (`Simulation`).
12. **`Simulation` records group positions for interpolation** before the foraging step, so rendering never depends on how the movement rules are written.
13. **Orders stamped for a past tick are applied late rather than dropped** (`OrderQueue.drainForTick`).
14. **The log's run ID comes from a second seed** carried in `GameSetup` (`runIdSeed`), so it never reveals the game seed; the seed is written as a JSON string because JavaScript numbers can't hold every 64-bit value (`GameLog`).
15. **The map list is a constant** (`MapCatalog.MAP_FILES`) because listing asset folders is unreliable on the web.
16. **The UI skin is built in code** from libGDX's default uiskin images and font (`UiSkin`), avoiding reflection-based JSON (A.5 rule 6). UI widgets own a Scene2D `Table` instead of subclassing it (A.5 rule 8).
17. **Fog is drawn semi-transparent** while `FogOfWar` is a stub (`WorldRenderer`).
18. **Placeholder flower numbers:** `nectarPerDayPerSize` 8–14 and potentials LOW/MEDIUM/HIGH in `flowers.json` are guesses (clover = 10 is from the spec).

**Differences from a stock gdx-liftoff project:** the `sim` module was added; the construo native-packaging plugin and GraalVM native-image setup were left out (we ship on the web); `lwjgl3:run` uses the repository root as its working directory (so desktop logs land in `logs/`, not in `assets/`); the `generateAssetList` task was left out; Gradle logging is `lifecycle` instead of `quiet` so test results show; Gradle may use up to 2 GB for the TeaVM compile; the debug JavaScript web target is unobfuscated.

**Still to decide** (CLAUDE.md Part I): every TBD value in D.7, how the feeding level maps to egg laying and extra consumption, whether bees away from the hive eat, when the raise cost is paid, the exact maps and their playstyles, the final species list and bloom calendar, and the fact content.

---

## Other notes

- The `beestinction/` folder at the repository root holds the team's earlier scratch files (`Main.java`, `bees.java`, ...). It is not part of the Gradle build; delete it when you no longer need it.
- License: MIT (see [LICENSE](LICENSE)). The placeholder UI skin and `StartupHelper` are Apache-2.0. Only commit art, fonts, and sounds whose licenses allow redistribution.
- Analytics (Milestone 5) will use Firebase on the free Spark plan with anonymous auth (CLAUDE.md Part G). Never commit a service-account key; `.gitignore` blocks the usual file names.
