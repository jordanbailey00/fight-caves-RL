# Agent Changelog

Append-only log of implementation changes and decisions.

## 2026-03-04 21:47:33 -05:00 - Step 0 Baseline Start

### Decisions
1. Proceeded with Step 0 (stabilization + baseline execution) before any extraction/refactor work.
2. Kept project Java target at 21 (as defined by repo), and ran Gradle via portable local JDK 21 because system JDK was 17.
3. Added missing runtime cache by downloading OpenRS2 revision 283 disk archive to unblock tests.

### Changes Made
1. Code fix:
   - `game/src/main/kotlin/content/entity/world/RegionLoading.kt`
   - Replaced `addFirst` call with `add(0, ...)` to resolve compile error while preserving ordering intent.
2. Environment/bootstrap artifacts:
   - Added local tooling directory `.tools/` with portable JDK 21 and downloaded cache archive extraction helper artifacts.
   - Populated `data/cache/` from OpenRS2 disk zip.
3. Documentation:
   - Added `docs/baseline.md` with command-level baseline results and blockers.

### Test Outcomes
1. `TzhaarFightCaveTest`: failed (missing `cave_entrance_fight_cave` object at expected tile).
2. `content.entity.combat.*`: failed (`CombatFlinchTest` behavior assertion mismatch).
3. `content.skill.prayer.*`: passed.
4. `content.skill.constitution.*`: passed.

### Current Status
- Step 0 remains open due 2 failing baseline targets.
- Primary blocker appears to be cache parity mismatch for Fight Caves object placement plus existing combat flinch regression.

## 2026-03-04 22:01:53 -05:00 - Step 0 Baseline Completed

### Decisions
1. Kept project JVM target at Java 21 (repo-default) and used a local portable JDK 21 instead of retargeting build files.
2. Used official Void cache from Mega (2025-06-12-void-634-cache.7z) after confirming OpenRS2 cache mismatch caused invalid baseline behavior.

### Changes Made
1. Compile fix in RegionLoading.kt:
   - addFirst replaced with add(0, ...).
2. Added /.tools/ to .gitignore to avoid tracking local JDK/cache tooling artifacts.
3. Updated docs/baseline.md to final Step 0 PASS state.

### Verification
1. TzhaarFightCaveTest: passed.
2. content.entity.combat.*: passed.
3. content.skill.prayer.*: passed.
4. content.skill.constitution.*: passed.

### Outcome
- Step 0 exit criteria satisfied.


## 2026-03-04 22:16:00 -05:00 - Iteration 01 Repo/Naming Housekeeping

### Decisions
1. Separated active development repository from the upstream Void fork to avoid fork-coupled workflow.
2. Preserved an unchanged fork copy for future replay/demo reference.
3. Committed only non-build/code artifacts in this iteration; gameplay/runtime code changes remain out-of-scope for this commit.

### Changes Made
1. GitHub repository changes:
   - Renamed fork "fight-caves-rl" -> "void-rsps-v2-5-0".
   - Updated fork description to "Void RSPS v2.5.0 (unchanged fork for replays/demos)".
   - Created new standalone public repository "fight-caves-rsps" (non-fork).
2. Local git remote changes:
   - origin now points to jordanbailey00/fight-caves-rsps.
   - Added/retained void-fork remote pointing to jordanbailey00/void-rsps-v2-5-0.
   - Kept upstream pointing to GregHib/void.
3. Planning/process updates:
   - Added iteration checklist + step snapshot tracking in plan.md.

### Status
- Repository split completed.
- Local directory rename pending and scheduled immediately after commit/push operations to avoid interrupting active path state.

## 2026-03-04 22:41:00 -05:00 - Step 1 Manifest Artifacts Created

### Decisions
1. Built Step 1 manifest as both human-readable (`docs/extraction_manifest.md`) and machine-readable (`config/headless_manifest.toml`) artifacts to reduce model/agent drift in later extraction work.
2. Kept Step 1 status as partial because required Step 1 tests are not implemented yet.
3. Used a conservative keep-set for first manifest pass to avoid accidental parity regressions before headless bootstrap wiring is introduced.

### Changes Made
1. Added `docs/extraction_manifest.md`:
   - documents explicit keep/cut boundaries for code, scripts, data, runtime stages, cache regions, and exclusion policy.
   - records required NPC/item/object/variable identifiers for Fight Caves closure.
2. Added `config/headless_manifest.toml`:
   - codifies modules, script allowlist, data allowlist, exclusion globs, settings keys, region scope, and validation requirements.
3. Updated `plan.md` iteration tracking:
   - added Iteration 02 checklist with completed/remaining Step 1 work.

### Verification
1. Parsed `config/headless_manifest.toml` successfully via Python `tomllib`.
2. No build/runtime tests executed in this iteration (artifact authoring only).

### Status
- Step 1 manifest artifacts completed.
- Step 1 test implementation and pass gate remain open.

## 2026-03-05 00:53:00 -05:00 - Step 1 Tests Implemented and Passing

### Decisions
1. Implemented Step 1 checks as static manifest-validation tests to enforce Fight Caves closure and exclusion rules before headless bootstrap work.
2. Reused `ConfigReader` for TOML parsing to keep tests aligned with project-native config parsing behavior.
3. Used a portable JDK 21 runtime for local test execution because the host default JVM was Java 17 and cannot execute Java 21 bytecode.

### Changes Made
1. Added `game/src/test/kotlin/headless/manifest/HeadlessManifestTestSupport.kt`:
   - repository root discovery
   - headless manifest TOML loading/parsing
   - helper checks for file/class/id resolution
2. Added `game/src/test/kotlin/headless/manifest/HeadlessManifestContainsFightCaveClosureTest.kt`:
   - validates scope metadata (minigame, wave/rotation counts)
   - validates required code/data path resolution
   - validates required script class source resolution
   - validates required NPC/item/object IDs resolve in data TOML files
3. Added `game/src/test/kotlin/headless/manifest/HeadlessManifestRejectsExcludedSystemsTest.kt`:
   - validates required script classes exclude blocked namespaces and oracle-only overlap
   - validates required code/data paths are not excluded unless explicitly excepted
   - validates core excluded namespaces are declared in manifest
4. Updated `plan.md`:
   - Step 1 tests marked complete
   - Step 1 status marked complete in step snapshot
   - Iteration 03 tracking entry added

### Verification
1. Initial local run failed under Java 17 with `UnsupportedClassVersionError` (class file version 65 vs runtime max 61).
2. Re-ran with Java 21 and passed:
   - `./gradlew :game:test --tests "headless.manifest.HeadlessManifestContainsFightCaveClosureTest" --tests "headless.manifest.HeadlessManifestRejectsExcludedSystemsTest"`

### Outcome
- Step 1 exit criteria test gate is now satisfied.

## 2026-03-05 01:18:00 -05:00 - Stop Checkpoint and Docs Sync Before Step 2

### Decisions
1. Treated this turn as a stopping checkpoint and deferred Step 2 implementation until next session.
2. Retried the pending local folder rename first, then documented the blocker and current project state.
3. Kept all tracking docs aligned so next iteration can start directly at Step 2.

### Actions Taken
1. Attempted local rename from parent directory:
   - `Rename-Item -Path "elvarg_RSPS" -NewName "fight caves RL"`
2. Stopped local Gradle daemons and retried rename.
3. Updated documentation state:
   - `plan.md` iteration tracking/status refreshed (including rename blocker and Step 2 as next work item).
   - `spec.md` appended with execution status checkpoint section.

### Result
1. Local folder rename remains blocked by Windows file lock (`Rename-Item` IOException: directory in use by another process).
2. Step 0 and Step 1 remain complete; Step 2 is queued for tomorrow.

### Remaining Blocker
- Close/release active IDE/terminal/process handles on `elvarg_RSPS`, then retry rename to `fight caves RL`.

## 2026-03-05 01:22:00 -05:00 - Folder Rename Retry (Still Blocked)

### Action
- Retried: `Rename-Item -Path "elvarg_RSPS" -NewName "fight caves RL"` from parent directory.

### Outcome
- Same Windows file lock IOException (directory still in use by another process).

## 2026-03-05 01:28:00 -05:00 - Owner Decision: Directory Rename Deferred

### Decision
- Owner explicitly deferred local directory rename because the folder is open in VSCode and will be renamed manually later.

### Documentation Updates
- `plan.md` updated to mark rename as deferred/non-blocking (no longer treated as an active blocker).
- `spec.md` execution checkpoint updated to reflect deferred manual rename status.

### Effect on Execution
- Step 2 remains the next implementation step for the next session.

## 2026-03-05 14:45:00 -05:00 - Step 2 Completed (Headless Bootstrap Path)

### Decisions
1. Introduced a dedicated headless entrypoint while preserving existing headed `Main` behavior.
2. Centralized shared bootstrap/preload/cache-module wiring to reduce drift between headed and headless startup paths.
3. Kept headless tick stages minimal and explicit, with canonical stage-name helpers for deterministic order assertions.

### Changes Made
1. Added `game/src/main/kotlin/RuntimeBootstrap.kt`:
   - runtime mode flag constants/types (`runtime.mode`, `headed`, `headless`)
   - shared settings loading (`loadRuntimeSettings`)
   - shared preload/Koin wiring (`preloadRuntime`)
   - shared cache/definition module wiring (`cacheModule`)
2. Updated `game/src/main/kotlin/Main.kt`:
   - now uses shared bootstrap utilities
   - logs startup mode (`mode=headed`)
3. Added `game/src/main/kotlin/HeadlessMain.kt`:
   - dedicated headless bootstrap (`bootstrap`) and entrypoint (`main`)
   - no GameServer/LoginServer startup path
   - explicit headless mode startup logging
4. Added `game/src/main/kotlin/HeadlessModules.kt`:
   - headless-specific Koin runtime config binding
5. Updated `game/src/main/kotlin/GameTick.kt`:
   - added `getHeadlessTickStages()`
   - added `headlessTickStageOrder`
   - added `describeTickStage(s)` helpers for testable canonical stage ordering
6. Added Step 2 tests:
   - `game/src/test/kotlin/headless/bootstrap/HeadlessBootstrapTestSupport.kt`
   - `game/src/test/kotlin/headless/bootstrap/HeadlessBootWithoutNetworkTest.kt`
   - `game/src/test/kotlin/headless/bootstrap/HeadlessTickPipelineOrderTest.kt`

### Verification
1. Passed Step 2 tests:
   - `./gradlew :game:test --tests "HeadlessBootWithoutNetworkTest" --tests "HeadlessTickPipelineOrderTest"`
2. Passed headed regression smoke:
   - `./gradlew :game:test --tests "content.area.karamja.tzhaar_city.TzhaarFightCaveTest"`

### Outcome
- Step 2 exit criteria satisfied.
- Next execution target is Step 3.

## 2026-03-05 20:10:00 -05:00 - Iteration 06 Step 3 Completed (Fight Caves-Only Data Loading)

### Decisions
1. Kept headless data loading on an explicit allowlist path to prevent accidental `dirs.txt` closure drift.
2. Restricted map/collision decode by headless region allowlist and validated region subset behavior via integration test.
3. Avoided unrelated headless eager-loading failures by skipping `DiangoCodeDefinitions` initialization in headless mode.
4. Hardened runtime bootstrap to create parent directories for modified/wildcard state files before update writes.
5. Isolated headless test cache/modified paths from headed test metadata to prevent cross-test cache contamination.

### Changes Made
1. Added `game/src/main/kotlin/HeadlessDataLoading.kt`:
   - `HeadlessDataAllowlist` model
   - allowlist TOML loader and resolver
   - required setting/file validation
   - headless `ConfigFiles` assembly from allowlisted files
2. Updated `game/src/main/kotlin/HeadlessMain.kt`:
   - bootstrap now uses allowlist loader path
   - runtime now exposes loaded allowlist metadata
3. Updated `engine/src/main/kotlin/world/gregs/voidps/engine/data/definition/MapDefinitions.kt`:
   - headless-aware region restriction via `headless.map.regions`
4. Updated `engine/src/main/kotlin/world/gregs/voidps/engine/EngineModules.kt`:
   - skip `DiangoCodeDefinitions` eager load in headless mode
5. Updated `game/src/main/kotlin/RuntimeBootstrap.kt`:
   - create parent dirs for `storage.data.modified` and `storage.wildcards` before `configFiles.update()`
   - preserve headed behavior
6. Added/updated Step 3 artifacts:
   - `config/headless_data_allowlist.toml`
   - `docs/headless_data_loading.md`
7. Added Step 3 tests:
   - `game/src/test/kotlin/headless/data/HeadlessLoadsFightCaveDataOnlyTest.kt`
   - `game/src/test/kotlin/headless/data/HeadlessFailsOnMissingFightCaveWaveDataTest.kt`
   - `game/src/test/kotlin/headless/data/HeadlessCollisionRegionSubsetTest.kt`
8. Updated headless test support:
   - `game/src/test/kotlin/headless/bootstrap/HeadlessBootstrapTestSupport.kt`
   - isolated modified/wildcards/cache test paths under `../temp/data/headless-test-cache/`
   - disabled map cache writing in headless tests (`storage.caching.active=false`)

### Verification
1. Passed Step 3 gate tests:
   - `./gradlew :game:test --tests "HeadlessLoadsFightCaveDataOnlyTest" --tests "HeadlessFailsOnMissingFightCaveWaveDataTest" --tests "HeadlessCollisionRegionSubsetTest" --no-daemon`
2. Passed headed regression smoke after Step 3 changes:
   - `./gradlew :game:test --tests "content.area.karamja.tzhaar_city.TzhaarFightCaveTest" --no-daemon`

### Outcome
- Step 3 exit criteria satisfied.
- Step 4 (script allowlist + script registry validation) is next.

## 2026-03-05 22:35:00 -05:00 - Iteration 07 Step 4 Completed (Script Allowlist and Content Loading)

### Decisions
1. Enforced headless script loading through an explicit allowlist file (`config/headless_scripts.txt`) while leaving headed/oracle full-script loading unchanged.
2. Added startup fail-fast validation for script drift and required Fight Caves hook registration to prevent silent closure regressions.
3. Kept the Step 4 allowlist focused on Fight Caves closure scripts that can be loaded under headless data scope without wildcard-resolution failures.

### Changes Made
1. Added script loading/validation path:
   - `game/src/main/kotlin/HeadlessScriptLoading.kt`
   - `config/headless_scripts.txt`
   - `docs/headless_scripts.md`
2. Updated runtime/script loader wiring:
   - `game/src/main/kotlin/ContentLoader.kt`
   - `game/src/main/kotlin/RuntimeBootstrap.kt`
   - `game/src/main/kotlin/HeadlessMain.kt`
3. Added registry introspection helpers used by startup validation:
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/mode/combat/CombatApi.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/timer/TimerApi.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/player/skill/Skills.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/mode/move/Moved.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/Spawn.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/Despawn.kt`
   - `engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/Death.kt`
4. Added Step 4 tests:
   - `game/src/test/kotlin/headless/scripts/HeadlessScriptRegistryContainsFightCaveHandlersTest.kt`
   - `game/src/test/kotlin/headless/scripts/HeadlessScriptRegistryExcludesUnrelatedSystemsTest.kt`
   - `game/src/test/kotlin/headless/scripts/HeadlessSingleWaveScriptSmokeTest.kt`
   - support: `game/src/test/kotlin/headless/scripts/HeadlessScriptTestSupport.kt`
5. Updated manifest/docs tracking for script closure parity:
   - `config/headless_manifest.toml`
   - `docs/extraction_manifest.md`
   - `plan.md`
   - `spec.md`

### Verification
1. Passed Step 4 tests:
   - `./gradlew :game:test --tests "HeadlessScriptRegistryContainsFightCaveHandlersTest" --tests "HeadlessScriptRegistryExcludesUnrelatedSystemsTest" --tests "HeadlessSingleWaveScriptSmokeTest" --no-daemon`
2. Passed headed regression smoke:
   - `./gradlew :game:test --tests "content.area.karamja.tzhaar_city.TzhaarFightCaveTest" --no-daemon`
3. Passed manifest regression checks:
   - `./gradlew :game:test --tests "headless.manifest.HeadlessManifestContainsFightCaveClosureTest" --tests "headless.manifest.HeadlessManifestRejectsExcludedSystemsTest" --no-daemon`

### Outcome
- Step 4 exit criteria satisfied.
- Next target is Step 5 (episode initialization contract implementation).
