# changelog.md

## 2026-03-09

- Started Phase 0 optimization implementation work for the headless sim measurement gate.
- Added clean standalone sim measurement entrypoints in `game/build.gradle.kts`:
  - `:game:headlessPerformanceReport`
  - `:game:headlessPerformanceProfile`
- Expanded the sim-side performance harness in:
  - `game/src/test/kotlin/headless/performance/HeadlessPerformanceReportGenerator.kt`
  - the harness now writes both `docs/performance_benchmark.log` and `docs/performance_benchmark.json`
  - the harness now records runtime metadata and a batched headless throughput row
  - the harness now publishes a per-worker ceiling estimate
- Updated performance harness regression coverage in:
  - `game/src/test/kotlin/headless/performance/HeadlessPerformanceReportGenerationTest.kt`
- Executed the standalone report and profile tasks successfully on the current WSL host:
  - `./gradlew --no-daemon :game:headlessPerformanceReport`
  - `./gradlew --no-daemon :game:headlessPerformanceProfile`
- Recorded the current-host standalone sim results:
  - single-slot throughput about `30.5k` ticks/sec
  - batched headless throughput (`16 envs`) about `473.6k` env steps/sec
  - clean JFR artifact emitted at `game/build/reports/headless-performance/headless_performance_profile.jfr`
- Recorded one important Phase 0 interpretation change:
  - the older Step 11 `8.9k` artifact is now historical context, not the active per-worker ceiling estimate
  - the remaining blocker before Phase 1 is the native-Linux source-of-truth rerun, not missing profiler infrastructure
- Updated sim-side docs to reflect the new standalone Phase 0 path:
  - `docs/sim_profiler_report.md`
  - `docs/performance_report.md`

- Added a sim-side performance audit report in:
  - `docs/sim_profiler_report.md`
- Recorded the current audit outcome for JVM profiling:
  - the repo still has a real direct-JVM throughput artifact in `docs/performance_report.md` at about `8.9k` ticks/sec
  - the current WSL host passes `HeadlessStepRateBenchmarkTest`
  - embedded-JVM JFR dump attempts launched from the RL Python benchmark did not dump cleanly on exit
  - Gradle/JUnit JFR capture succeeded but was dominated by Gradle / JUnit / JaCoCo harness noise and did not yield clean headless-symbol CPU samples
  - `jcmd -l` did not expose the embedded JPype JVM as an attachable target during the RL bridge benchmark
- Documented the profiler limitation explicitly instead of claiming function-level JVM hotspots without clean samples.
- Expanded the sim profiler report with the exact JFR commands, runtime/JVM details, and benchmark-scenario metadata used in this audit pass.

## 2026-03-08

- Tightened the root `FCspec.md` episode-start contract so the primary module spec now matches the already-documented headless reset contract:
  - fixed run toggle wording to unconditional `ON`
  - added explicit reset input defaults for `startWave`, `ammo`, `prayerPotions`, and `sharks`
  - added the missing `all other skills: 1` line
  - clarified that no neck item is part of the canonical loadout
- Added an explicit root-spec note that the current `create_release.yml` remains inherited from the upstream Void fork and is not the source of truth for headless artifact naming or packaging contracts.
- Updated the root README and end-to-end checklist to use the canonical headless artifact wording:
  - default task `:game:headlessDistZip`
  - fallback/validation task `:game:packageHeadless`

## 2026-03-07

- Adopted canonical Fight Caves module root filenames:
  - `FCspec.md`
  - `FCplan.md`
- Renamed the prior root files from `spec.md` and `plan.md`.
- Updated active functional references so the rename does not break:
  - repo-root detection in Kotlin sources/tests
  - headless manifest references
  - current README guidance
  - current end-to-end checklist docs
- Existing detailed historical execution notes remain in `docs/changelog.md`.
