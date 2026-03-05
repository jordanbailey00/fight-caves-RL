# Void Fight Caves - 1:1 Headless Simulator Extraction Spec (RL-Ready)

## 0) Purpose

Build a headless, high-throughput, deterministic Fight Caves simulator from this **Void RSPS** codebase while preserving **1:1 gameplay parity** with the headed RSPS server behavior.

This spec is strict:
- Anything not explicitly included is excluded from headless runtime.
- If there is a conflict between assumptions and code, **current Void code wins** and this spec must be updated.

Primary acceptance target:
- A fully functioning headless Fight Caves simulator that trains with behavior matching the headed RSPS 1:1 under parity harness verification.

## 1) Repository Reality (Void, not Elvarg)

Authoritative implementation sources:
- `game/src/main/kotlin/content/area/karamja/tzhaar_city/TzhaarFightCave.kt`
- `game/src/main/kotlin/content/area/karamja/tzhaar_city/TzhaarFightCaveWaves.kt`
- `game/src/main/kotlin/content/area/karamja/tzhaar_city/TzTokJad.kt`
- `game/src/main/kotlin/content/area/karamja/tzhaar_city/TzHaarHealers.kt`
- `data/minigame/tzhaar_fight_cave/tzhaar_fight_cave_waves.toml`
- `data/minigame/tzhaar_fight_cave/tzhaar_fight_cave.npcs.toml`
- `data/minigame/tzhaar_fight_cave/tzhaar_fight_cave.combat.toml`
- `data/minigame/tzhaar_fight_cave/tzhaar_fight_cave.areas.toml`

Current Fight Caves scope in this repo:
- **63 waves** (not a single-wave/Jad-only implementation).
- 15 rotations for spawn direction assignment.
- Jad/healer/split mechanics implemented via scripts and config.

## 2) Non-Negotiable Parity Contracts

### 2.1 Deterministic replay contract (test/debug mode)

Headless simulator must be deterministic for:
- identical initial state
- identical `episode_seed`
- identical per-tick `action_trace`

Deterministic replay output must match oracle RSPS tick-by-tick for all tracked state fields.

Training mode is still stochastic:
- varying seeds across episodes
- actions selected online by policy

### 2.2 Tick order contract (must mirror Void)

The headless tick pipeline must preserve Void stage order from `GameTick.kt`:
1. `PlayerResetTask`
2. `NPCResetTask`
3. `BotManager`
4. `Hunting`
5. `GrandExchange`
6. `ConnectionQueue`
7. `NPCs`
8. `FloorItems`
9. `InstructionTask`
10. `World`
11. `NPCTask`
12. `PlayerTask`
13. `FloorItemTracking`
14. `GameObjects.timers`
15. `DynamicZones`
16. `ZoneBatchUpdates`
17. `CharacterUpdateTask`
18. `SaveQueue`
19. `SaveLogs`

For headless runtime, excluded stages may be removed only if they do not alter Fight Caves behavior. Any removal must be parity-tested.

Within `NPCTask` order:
- delay checks
- optional wander assignment
- regen
- soft timers
- queue tick
- mode tick
- facing updates

Within `PlayerTask` order:
- delay checks
- queue tick
- timers (if allowed)
- soft timers
- mode tick
- facing updates

### 2.3 Combat and hit timing contract

Must preserve:
- attack cooldown logic (`action_delay`, attack speed)
- range/melee distance checks and LOS behavior
- pending hit scheduling and application timing
- prayer protection effects at hit application timing
- NPC attack selection behavior and weighted sampling

### 2.4 Collision and movement contract

Must preserve Void movement/pathing stack:
- `PathFinder`, `StepValidator`, `LineValidator`
- tile clipping, diagonal blocking, entity size
- follow/approach movement semantics in combat
- dynamic zone/instance collision behavior

### 2.5 Dynamic instance contract

Fight Caves must continue to operate in copied instance regions using current Void instance logic.

### 2.6 No hidden behavior changes

Headless must not introduce:
- simplified formulas
- altered queue/timer order
- synthetic timing shortcuts

If a simplification is proposed for performance, it must pass deterministic parity harness first.

## 3) Fight Caves Domain Contract (Void-specific)

### 3.1 Authoritative controller behavior

From `TzhaarFightCave.kt` headless must preserve:
- entry behavior (instance creation, teleport, movement)
- wave state variables (`fight_cave_wave`, `fight_cave_rotation`, `fight_cave_remaining`)
- wave transitions based on despawn and remaining counts
- leave/reward flow
- death/logout/restart semantics

### 3.2 Waves and rotations

From `tzhaar_fight_cave_waves.toml`:
- 63 waves, each with explicit NPC composition
- 15 rotations per wave for spawn direction mapping

### 3.3 NPC allowlist (minimum required)

At minimum include all Fight Caves NPC ids used directly or indirectly:
- `tz_kih`
- `tz_kih_spawn_point`
- `tz_kek`
- `tz_kek_spawn_point`
- `tz_kek_spawn`
- `tok_xil`
- `tok_xil_spawn_point`
- `yt_mej_kot`
- `yt_mej_kot_spawn_point`
- `ket_zek`
- `ket_zek_spawn_point`
- `tztok_jad`
- `yt_hur_kot`

### 3.4 Special mechanics to preserve

Must preserve exactly:
- Tz-Kih prayer drain on hit
- Tz-Kek split spawn behavior
- Tok-Xil melee/range behavior
- Ket-Zek melee/magic behavior
- Jad melee/range/magic behavior and attack timing
- Jad healer spawn trigger and healer healing logic
- healer retargeting/follow behavior as currently scripted

### 3.5 Areas and boundaries

From `tzhaar_fight_cave.areas.toml`:
- multi-combat area behavior
- directional spawn sub-areas
- inside-cave bounds enforcement for episode validity

## 4) Player Loadout and Episode Initialization Contract

### 4.1 Fixed skill levels (episode start)

Set at episode reset:
- Attack: 1
- Strength: 1
- Defence: 70
- Hitpoints: 70
- Ranged: 70
- Prayer: 43
- Magic: 1

Derived start resources:
- HP: full (70)
- Prayer points: full (43)
- Run energy: 100%
- Run toggle: ON by default unless parity tests require different default

No XP gain in headless training episodes.

### 4.2 Equipment allowlist

Required starting equipment:
- Rune crossbow
- Adamant bolts
- Coif
- Black dragonhide body
- Black dragonhide chaps
- Black dragonhide vambraces
- Snakeskin boots

### 4.3 Inventory allowlist

Required starting inventory:
- Prayer potion x8 (dose-chain correct)
- Shark x20

Dose chain must be mapped correctly:
- prayer potion 4/3/2/1 and vial behavior

### 4.4 Timing lockouts

Preserve current consumption timing behavior:
- food delay
- drink delay
- combo delay

Lockout behavior must match current server semantics exactly.

## 5) Headless API Contract

### 5.1 Actions (one intent per tick)

Required actions:
- walk-to-tile (pathfinder-backed)
- attack visible NPC by deterministic index mapping
- toggle protection prayers
- eat shark
- drink prayer potion
- toggle run
- wait/no-op

Rejected actions must be recorded with explicit reason enums.

### 5.2 Observation schema

At minimum include:
- player tile, HP/prayer current+max, run energy/toggle
- active relevant prayer flags
- attack/consume lockouts
- inventory consumable counts and ammo count
- current wave index and remaining NPC count
- deterministic NPC list with id/tile/hp/flags

No future-leakage fields by default (e.g., next uncommitted Jad style).

### 5.3 Episode termination

Episode ends on:
- player death
- final wave completion
- invalid cave exit (if allowed by runtime policy)
- optional max tick cap

## 6) Systems to Keep vs Cut (Headless Runtime)

### 6.1 Keep (required)

Keep only systems needed for Fight Caves closure:
- tick loop and required stage execution
- movement/pathing/clipping stack
- combat/hit/timer/queue systems
- prayer/food/potion/inventory/equipment/ammo systems used by loadout
- Fight Caves scripts + dependencies
- required definitions/data loaders for Fight Caves region and allowlisted items/NPCs
- deterministic RNG control via shared random provider

### 6.2 Cut (excluded from runtime)

Exclude unless proven required for closure:
- networking/login packet handling
- account persistence autosave for training loops
- social/chat/trading/banking/shops
- unrelated skills/quests/areas/minigames
- global world spawn systems unrelated to Fight Caves

## 7) Determinism and RNG Contract

Headless runtime must:
- seed all randomness through shared RNG path
- avoid direct `kotlin.random.Random` use in Fight Caves closure
- optionally expose RNG call count/state in debug snapshots

Deterministic replay parity must be validated in CI.

## 8) Parity Harness Contract (Required)

Implement side-by-side differential harness:
- Oracle: full RSPS logic path
- Candidate: headless path
- Inputs: identical seed + initial state + action trace

Tick-by-tick compare:
- player state
- wave state
- sorted NPC state
- pending hits/timers relevant to combat outcomes
- terminal condition and reward outputs

Fail-fast output must include:
- tick number
- last action
- mismatch field path
- oracle vs headless value
- optional RNG counter snapshot

## 9) Performance Contract

After parity lock:
- optimize allocations and logging
- batch/vectorize environments if needed
- keep semantics unchanged

Any optimization that changes parity is rejected.

## 10) Delivery and Acceptance Criteria

Required deliverables:
- headless Fight Caves runtime path
- deterministic replay mode
- parity harness with CI regressions
- extraction manifest documenting keep/cut closure

Final acceptance criteria:
1. Headless sim completes full Fight Caves lifecycle correctly.
2. Deterministic parity harness passes against headed/oracle behavior for required test matrix.
3. Runtime includes only required closure systems/data.
4. Simulator is stable for RL stepping and replayable for debugging.

## 11) Future RL Work (Out of Scope Until 1:1 Is Locked)

Future tasks after parity completion:
- vectorized env wrappers
- curriculum/reward experiments
- policy training/evaluation pipelines
- headed demo replay tooling for trained policy

No RL optimization work should proceed before full parity sign-off.

## 12) Execution Status Checkpoint (2026-03-05)

Completed to date:
- Step 0 baseline stabilization and verification gate passed.
- Step 1 manifest definition and lock gate passed.
- Step 2 headless bootstrap and non-network tick path gate passed.
- Step 3 Fight Caves-only data loading gate passed.
- Step 4 script allowlist and content loading gate passed.

Step 1 artifacts present:
- `docs/extraction_manifest.md`
- `config/headless_manifest.toml`

Step 3 artifacts present:
- `config/headless_data_allowlist.toml`
- `docs/headless_data_loading.md`
- `game/src/main/kotlin/HeadlessDataLoading.kt`
- `game/src/test/kotlin/headless/data/HeadlessLoadsFightCaveDataOnlyTest.kt`
- `game/src/test/kotlin/headless/data/HeadlessFailsOnMissingFightCaveWaveDataTest.kt`
- `game/src/test/kotlin/headless/data/HeadlessCollisionRegionSubsetTest.kt`

Step 4 artifacts present:
- `config/headless_scripts.txt`
- `docs/headless_scripts.md`
- `game/src/main/kotlin/HeadlessScriptLoading.kt`
- `game/src/test/kotlin/headless/scripts/HeadlessScriptRegistryContainsFightCaveHandlersTest.kt`
- `game/src/test/kotlin/headless/scripts/HeadlessScriptRegistryExcludesUnrelatedSystemsTest.kt`
- `game/src/test/kotlin/headless/scripts/HeadlessSingleWaveScriptSmokeTest.kt`

Step 4 verification status:
- Passed:
  - `HeadlessScriptRegistryContainsFightCaveHandlersTest`
  - `HeadlessScriptRegistryExcludesUnrelatedSystemsTest`
  - `HeadlessSingleWaveScriptSmokeTest`
- Passed headed regression smoke after Step 4:
  - `content.area.karamja.tzhaar_city.TzhaarFightCaveTest`
- Passed manifest regression checks after Step 4 manifest updates:
  - `headless.manifest.HeadlessManifestContainsFightCaveClosureTest`
  - `headless.manifest.HeadlessManifestRejectsExcludedSystemsTest`

Implementation notes added in Step 4:
- Headless runtime now loads scripts strictly from `config/headless_scripts.txt`; headed/oracle runtime still loads the full generated `scripts.txt`.
- Headless startup now fails fast on script drift and missing required hooks.
- Script drift guard now enforces parity between `config/headless_scripts.txt` and `config/headless_manifest.toml` `[scripts].required_classes`.
- Explicit hook-introspection helpers were added for combat/timer/movement/spawn/death/despawn registries to support deterministic startup validation.

Remaining before Step 5 execution:
- Local folder rename from `elvarg_RSPS` to `fight caves RL` remains owner-deferred (manual rename later) and is non-blocking.
- Step 5 (episode initialization contract implementation) is the next implementation target.

Environment clarification:
- Project/test bytecode target is Java 21; run verification tests under a Java 21 runtime.
