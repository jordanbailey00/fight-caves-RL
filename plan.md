# plan.md - Void Fight Caves Headless Extraction and 1:1 Parity Execution Plan

This plan is implementation-facing and must be executed in order.

## Iteration Tracking

Use this section to track each execution iteration and reduce implementation drift.

### Iteration 01 - Repository and Naming Alignment (2026-03-04)

- [x] Renamed the existing fork repo to "void-rsps-v2-5-0" and retained it as an unchanged archive fork for future replay/demo use.
- [x] Created a new standalone public repo "fight-caves-rsps" (non-fork).
- [x] Rewired local git remotes:
  - origin -> jordanbailey00/fight-caves-rsps
  - void-fork -> jordanbailey00/void-rsps-v2-5-0
  - upstream -> GregHib/void
- [x] Local root directory rename to "fight caves RL" explicitly deferred by owner (manual rename later); non-blocking for implementation.
- [x] Step 1 execution started and completed in later iterations.

### Iteration 02 - Step 1 Manifest Authoring (2026-03-04)

- [x] Created `docs/extraction_manifest.md` with explicit Fight Caves keep/cut closure and validation rules.
- [x] Created `config/headless_manifest.toml` as machine-readable manifest for headless extraction enforcement.
- [x] Mapped direct Fight Caves closure dependencies from scripts, data files, item loadout sources, and runtime stage requirements.
- [x] Implemented Step 1 automated tests (`HeadlessManifestContainsFightCaveClosureTest`, `HeadlessManifestRejectsExcludedSystemsTest`).
- [x] Step 1 marked complete.

### Iteration 03 - Step 1 Test Execution and Validation (2026-03-05)

- [x] Added `game/src/test/kotlin/headless/manifest/HeadlessManifestContainsFightCaveClosureTest.kt`.
- [x] Added `game/src/test/kotlin/headless/manifest/HeadlessManifestRejectsExcludedSystemsTest.kt`.
- [x] Added shared parser/utility support in `game/src/test/kotlin/headless/manifest/HeadlessManifestTestSupport.kt`.
- [x] Executed Step 1 tests successfully with JDK 21 runtime.
- [x] Local root directory rename to "fight caves RL" explicitly deferred by owner (manual rename later); non-blocking for implementation.

### Iteration 04 - Stop Checkpoint Before Step 2 (2026-03-05)

- [x] Retried local folder rename from parent directory after stopping Gradle daemons.
- [x] Local folder rename explicitly deferred by owner; directory is open in VSCode and manual rename will be done later.
- [x] Updated `plan.md`, `spec.md`, and `docs/changelog.md` to reflect completed vs remaining work at this stopping point.
- [x] Confirmed Step 2 is the next execution target for tomorrow.

### Iteration 05 - Step 2 Headless Bootstrap Path (2026-03-05)

- [x] Added dedicated headless runtime entrypoint in `game/src/main/kotlin/HeadlessMain.kt`.
- [x] Added headless Koin service wiring in `game/src/main/kotlin/HeadlessModules.kt` and shared runtime bootstrap wiring in `game/src/main/kotlin/RuntimeBootstrap.kt`.
- [x] Added minimal headless tick pipeline in `getHeadlessTickStages()` with deterministic stage-order introspection helpers.
- [x] Added explicit runtime mode flag (`runtime.mode`) with startup logging for both headed and headless entrypoints.
- [x] Implemented and passed Step 2 tests:
  - `HeadlessBootWithoutNetworkTest`
  - `HeadlessTickPipelineOrderTest`
- [x] Ran headed regression smoke test and passed:
  - `content.area.karamja.tzhaar_city.TzhaarFightCaveTest`
- [x] Step 2 marked complete.

### Iteration 06 - Step 3 Fight Caves-Only Data Loading (2026-03-05)

- [x] Added allowlist-driven headless config loading in `game/src/main/kotlin/HeadlessDataLoading.kt`.
- [x] Wired `HeadlessMain.bootstrap` to load allowlist-scoped `ConfigFiles` instead of `dirs.txt` sweep.
- [x] Added `config/headless_data_allowlist.toml` and `docs/headless_data_loading.md` as Step 3 artifacts.
- [x] Restricted map/collision decode to headless allowlisted regions in `MapDefinitions`.
- [x] Added headless runtime gating for non-Fight-Caves `DiangoCodeDefinitions` eager load.
- [x] Added runtime state-file directory preparation before `configFiles.update()` for non-default modified/wildcard paths.
- [x] Added and passed Step 3 tests:
  - `HeadlessLoadsFightCaveDataOnlyTest`
  - `HeadlessFailsOnMissingFightCaveWaveDataTest`
  - `HeadlessCollisionRegionSubsetTest`
- [x] Added regression hardening in headless test overrides to isolate cache/modified paths from headed tests.
- [x] Ran headed regression smoke and passed:
  - `content.area.karamja.tzhaar_city.TzhaarFightCaveTest`
- [x] Step 3 marked complete.
- [x] Remaining: Step 4 script allowlist + script registry validation (completed in Iteration 07).

### Iteration 07 - Step 4 Script Allowlist and Registry Validation (2026-03-05)

- [x] Added headless script allowlist artifact `config/headless_scripts.txt`.
- [x] Added headless script loading/validation docs `docs/headless_scripts.md`.
- [x] Updated `ContentLoader` + `RuntimeBootstrap` + `HeadlessMain` to support headless-only script allowlist filtering while preserving headed/oracle full script loading.
- [x] Added startup script validation in `HeadlessScriptLoading.kt`:
  - required hooks registered
  - loaded script set equals allowlist
  - allowlist equals `config/headless_manifest.toml` `[scripts].required_classes`
- [x] Added runtime hook-introspection helpers in engine registries (`CombatApi`, `TimerApi`, `Skills`, `Moved`, `Spawn`, `Despawn`, `Death`) for explicit startup validation.
- [x] Added and passed Step 4 tests:
  - `HeadlessScriptRegistryContainsFightCaveHandlersTest`
  - `HeadlessScriptRegistryExcludesUnrelatedSystemsTest`
  - `HeadlessSingleWaveScriptSmokeTest`
- [x] Re-ran and passed headed regression smoke:
  - `content.area.karamja.tzhaar_city.TzhaarFightCaveTest`
- [x] Re-ran and passed Step 1 manifest tests after script manifest updates:
  - `headless.manifest.HeadlessManifestContainsFightCaveClosureTest`
  - `headless.manifest.HeadlessManifestRejectsExcludedSystemsTest`
- [x] Step 4 marked complete.
- [ ] Remaining: Step 5 episode initialization contract implementation.
### Step Status Snapshot

- [x] Step 0 - Baseline Stabilization and Freeze
- [x] Step 1 - Define and Lock Extraction Manifest
- [x] Step 2 - Headless Bootstrap Path (No Networking)
- [x] Step 3 - Fight Caves-Only Data Loading
- [x] Step 4 - Script Allowlist and Content Loading
- [ ] Step 5 - Episode Initialization Contract Implementation
- [ ] Step 6 - Action Adapter (One Intent Per Tick)
- [ ] Step 7 - Observation Builder Contract
- [ ] Step 8 - Determinism Hardening and RNG Governance
- [ ] Step 9 - Oracle vs Headless Differential Parity Harness
- [ ] Step 10 - Runtime Pruning and Packaging
- [ ] Step 11 - Performance Validation (Post-Parity Only)
- [ ] Step 12 - Final Acceptance and Release Gate
- [ ] Step 13 - Physical Repository Prune (Hard Delete Pass)

Rules:
- Do not skip steps.
- Do not mark a step complete unless all listed tests pass.
- If a test fails, fix root cause before continuing.
- If code behavior conflicts with `spec.md`, update code to match spec or update spec with evidence from current authoritative Void behavior.

Traceability rule:
- Every PR/change set must reference the relevant `spec.md` sections by number.

---

## Step 0 - Baseline Stabilization and Freeze

Spec linkage:
- `spec.md` sections 1, 2, 10

Required action items:
1. Fix current compile blockers in the existing repo without changing gameplay semantics.
2. Run baseline Fight Caves tests and core combat/movement/prayer tests in current full-runtime mode.
3. Capture baseline test report and create a baseline state document with commit hash, environment details, and passing tests.
4. Record known technical debt and known flaky tests (if any) before extraction starts.

Required tests:
1. `./gradlew :game:test --tests "content.area.karamja.tzhaar_city.TzhaarFightCaveTest"`
2. `./gradlew :game:test --tests "content.entity.combat.*"`
3. `./gradlew :game:test --tests "content.skill.prayer.*"`
4. `./gradlew :game:test --tests "content.skill.constitution.*"`

Step artifacts:
1. `docs/baseline.md` with commit id and pass/fail snapshot.
2. CI log artifacts or stored local logs.

Exit criteria:
- Baseline compile passes and listed tests pass on unextracted runtime.

---

## Step 1 - Define and Lock Extraction Manifest

Spec linkage:
- `spec.md` sections 3, 4, 6

Required action items:
1. Create an explicit keep/cut manifest covering code modules, scripts, and data files.
2. Enumerate all Fight Caves closure dependencies transitively from:
   - `TzhaarFightCave.kt`
   - movement/pathing
   - combat/hits
   - prayer
   - food/potions
   - inventory/equipment/ammo
3. Define explicit exclusion list for non-runtime systems.
4. Add a static check to ensure new files outside manifest are not accidentally loaded in headless mode.

Required tests:
1. New unit test: `HeadlessManifestContainsFightCaveClosureTest`.
2. New unit test: `HeadlessManifestRejectsExcludedSystemsTest`.

Step artifacts:
1. `docs/extraction_manifest.md`.
2. `config/headless_manifest.toml` (or equivalent machine-readable manifest).

Exit criteria:
- Manifest is complete, reviewed, and test-validated.

---

## Step 2 - Headless Bootstrap Path (No Networking)

Spec linkage:
- `spec.md` sections 2.2, 6, 10

Required action items:
1. Add dedicated headless entrypoint and service wiring (Koin/module setup).
2. Ensure headless path does not start login/game servers or packet decoding loops.
3. Build a minimal stage pipeline preserving required tick order semantics for retained systems.
4. Add explicit runtime mode flag (`headed` vs `headless`) and log startup mode.

Required tests:
1. New integration test: `HeadlessBootWithoutNetworkTest`.
2. New integration test: `HeadlessTickPipelineOrderTest`.
3. Regression test: existing headed mode still boots and tests pass.

Step artifacts:
1. `HeadlessMain` (or equivalent).
2. `HeadlessModules` configuration.

Exit criteria:
- Headless runtime boots and ticks without network stack.

---

## Step 3 - Fight Caves-Only Data Loading

Spec linkage:
- `spec.md` sections 3, 4, 6

Required action items:
1. Implement headless data loader using an explicit allowlist (no full `dirs.txt` sweep).
2. Include Fight Caves-specific config files and all required shared definitions.
3. Limit map/collision loading to Fight Caves source regions plus required dynamic-instance support.
4. Add assertions that missing allowlisted data causes startup failure with clear errors.

Required tests:
1. New integration test: `HeadlessLoadsFightCaveDataOnlyTest`.
2. New integration test: `HeadlessFailsOnMissingFightCaveWaveDataTest`.
3. New integration test: `HeadlessCollisionRegionSubsetTest`.

Step artifacts:
1. `config/headless_data_allowlist.toml`.
2. Loader docs in `docs/headless_data_loading.md`.

Exit criteria:
- Headless runtime loads only allowlisted data and still runs Fight Caves.

---

## Step 4 - Script Allowlist and Content Loading

Spec linkage:
- `spec.md` sections 3, 6

Required action items:
1. Add headless script manifest (Fight Caves closure scripts only).
2. Keep full script loading behavior unchanged for oracle/headed mode.
3. Add startup validation that required script hooks are registered.
4. Add guard against accidental script drift (new script dependencies not in manifest).

Required tests:
1. New integration test: `HeadlessScriptRegistryContainsFightCaveHandlersTest`.
2. New integration test: `HeadlessScriptRegistryExcludesUnrelatedSystemsTest`.
3. Differential smoke: single wave run executes with expected script hooks.

Step artifacts:
1. `config/headless_scripts.txt`.
2. `docs/headless_scripts.md`.

Exit criteria:
- Headless script registry is minimal and sufficient.

---

## Step 5 - Episode Initialization Contract Implementation

Spec linkage:
- `spec.md` sections 3, 4, 5.3

Required action items:
1. Implement deterministic episode reset function for headless API.
2. Initialize fixed stats, resources, equipment, and inventory exactly as defined.
3. Ensure Fight Caves wave state and instance state are reset correctly.
4. Ensure run/prayer toggles and timers start from contract-defined values.

Required tests:
1. New test: `EpisodeInitSetsFixedStatsTest`.
2. New test: `EpisodeInitSetsLoadoutAndConsumablesTest`.
3. New test: `EpisodeInitResetsWaveStateTest`.
4. New test: `EpisodeInitUsesProvidedSeedTest`.

Step artifacts:
1. `FightCaveEpisodeInitializer` (or equivalent).
2. `docs/episode_init_contract.md`.

Exit criteria:
- Reset state is reproducible and contract-compliant.

---

## Step 6 - Action Adapter (One Intent Per Tick)

Spec linkage:
- `spec.md` section 5.1

Required action items:
1. Define stable action enum/id mapping.
2. Implement per-tick action ingestion and intent application to native Void systems.
3. Implement deterministic visible-NPC indexing and target mapping rules.
4. Implement standardized rejected-action reason enums and `action_applied` metadata.
5. Ensure movement actions use canonical pathfinder and walking queue behavior.

Required tests:
1. New test: `ActionWalkToUsesPathfinderTest`.
2. New test: `ActionAttackNpcDeterministicIndexMappingTest`.
3. New test: `ActionPrayerToggleParityTest`.
4. New test: `ActionEatDrinkLockoutRejectionTest`.
5. New test: `ActionInvalidTargetRejectionTest`.

Step artifacts:
1. `HeadlessActionAdapter`.
2. `HeadlessActionResult` schema.

Exit criteria:
- Every action is deterministic, validated, and correctly reported.

---

## Step 7 - Observation Builder Contract

Spec linkage:
- `spec.md` section 5.2

Required action items:
1. Build observation schema with strict field definitions and stable ordering.
2. Ensure deterministic NPC ordering for fixed indexing.
3. Expose required player/wave/NPC state and lockout fields.
4. Omit future-leakage fields by default.
5. Version the observation schema and add backward compatibility policy.

Required tests:
1. New test: `ObservationSchemaCompletenessTest`.
2. New test: `ObservationNpcOrderingDeterminismTest`.
3. New test: `ObservationNoFutureLeakageDefaultTest`.
4. New test: `ObservationVersioningContractTest`.

Step artifacts:
1. `HeadlessObservationV1`.
2. `docs/observation_schema.md`.

Exit criteria:
- Observation output is deterministic and policy-ready.

---

## Step 8 - Determinism Hardening and RNG Governance

Spec linkage:
- `spec.md` sections 2.1, 7

Required action items:
1. Ensure all Fight Caves closure randomness uses shared seeded RNG path.
2. Add deterministic replay runner that accepts `seed + action_trace`.
3. Add optional RNG call counter/state tracking for diff diagnostics.
4. Add lint/static checks preventing direct `kotlin.random.Random` use in closure.

Required tests:
1. New test: `DeterministicReplaySameSeedSameTraceTest`.
2. New test: `DeterministicReplayDifferentSeedDivergesTest`.
3. New test: `FightCaveClosureNoDirectRandomUsageTest`.
4. New test: `RngCounterMonotonicityTest`.

Step artifacts:
1. `HeadlessReplayRunner`.
2. RNG governance docs in `docs/determinism.md`.

Exit criteria:
- Replay determinism is enforced and verifiable.

---

## Step 9 - Oracle vs Headless Differential Parity Harness

Spec linkage:
- `spec.md` section 8

Required action items:
1. Build side-by-side runner for oracle (full runtime path) and headless path.
2. Apply identical initial state, seed, and action trace to both.
3. Compare per tick with fail-fast mismatch reporting.
4. Persist first-divergence diagnostic artifacts for reproducibility.

Required tests:
1. New integration test: `ParityHarnessSingleWaveTraceTest`.
2. New integration test: `ParityHarnessFullRunTraceTest`.
3. New integration test: `ParityHarnessJadHealerScenarioTest`.
4. New integration test: `ParityHarnessTzKekSplitScenarioTest`.

Step artifacts:
1. `ParityHarness` runner.
2. `ParitySnapshot` schema.
3. `docs/parity_harness.md`.

Exit criteria:
- Differential parity tests pass for required scenario matrix.

---

## Step 10 - Runtime Pruning and Packaging

Spec linkage:
- `spec.md` section 6

Required action items:
1. Remove excluded runtime systems from headless package path.
2. Keep oracle/headed mode intact for future differential validation.
3. Add build tasks for headless artifact creation.
4. Add startup assertions to fail if excluded systems are accidentally active.
5. Produce a deletion-candidate inventory from `config/headless_manifest.toml` for all files/directories not required by headless runtime.

Required tests:
1. New integration test: `HeadlessPackageStartsWithoutExcludedSystemsTest`.
2. New integration test: `HeadedModeStillPassesBaselineFightCaveTests`.
3. Build test: headless artifact generation task succeeds.
4. New test: `HeadlessDeletionCandidateInventoryTest`.

Step artifacts:
1. headless distribution/build target.
2. `docs/runtime_pruning.md`.
3. `docs/deletion_candidates.md`.

Exit criteria:
- Headless runtime is minimal and functionally complete.

---

## Step 11 - Performance Validation (Post-Parity Only)

Spec linkage:
- `spec.md` section 9

Required action items:
1. Profile hot paths (tick loop, combat, pathing, observation serialization).
2. Optimize allocations and reduce logging in hot path.
3. Add batch stepping support only if parity remains unchanged.
4. Re-run complete parity suite after each optimization batch.

Required tests:
1. New performance test: `HeadlessStepRateBenchmarkTest`.
2. New soak test: `HeadlessLongRunStabilityTest`.
3. Mandatory full parity regression rerun.

Step artifacts:
1. `docs/performance_report.md`.
2. Benchmark output logs.

Exit criteria:
- Throughput target met with no parity regressions.

---

## Step 12 - Final Acceptance and Release Gate

Spec linkage:
- `spec.md` section 10

Required action items:
1. Run full end-to-end suite defined in `e2e test.md`.
2. Verify all mandatory pass criteria and artifact generation.
3. Freeze release candidate commit.
4. Publish final implementation notes and known limitations (if any).

Required tests:
1. Entire `e2e test.md` suite.

Step artifacts:
1. `docs/release_candidate.md`.
2. test logs and parity artifacts.

Exit criteria:
- All acceptance criteria satisfied and signed off.

---

## Step 13 - Physical Repository Prune (Hard Delete Pass)

Spec linkage:
- `spec.md` sections 6, 10

Required action items:
1. Freeze a recovery point before deletion (`git tag` + branch) so removed files can be recovered if needed.
2. Convert `docs/deletion_candidates.md` into an approved deletion list.
3. Physically delete all non-approved files/directories/code/data not required for:
   - headless Fight Caves runtime
   - deterministic replay
   - parity harness and required tests
   - project build and CI metadata
4. Remove unused Gradle modules/dependencies and update `settings.gradle.kts` if modules are deleted.
5. Update docs to reflect post-prune project structure.
6. Re-run full E2E suite after deletion.

Required tests:
1. New test: `ProjectTreeMatchesApprovedManifestTest`.
2. New test: `ForbiddenPathsAbsentTest`.
3. Full `e2e test.md` suite (must pass post-prune).

Step artifacts:
1. `docs/repo_prune_report.md` (deleted paths + reason + owner approval).
2. Updated `config/headless_manifest.toml`.
3. Updated `docs/extraction_manifest.md`.

Exit criteria:
- Unnecessary files/directories are physically removed from the repository.
- Build, parity, and E2E validation still pass after pruning.

---
## Post-Spec Future Work (Do Not Start Before Step 12 Passes)

1. RL environment wrappers (Gymnasium-style).
2. Vectorized rollouts and distributed training.
3. Reward experiments and curriculum.
4. Policy replay into headed RSPS demonstrations.

These tasks are blocked until full 1:1 parity is confirmed.

