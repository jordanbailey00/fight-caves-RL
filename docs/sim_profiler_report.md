# Sim Profiler Report

Date: 2026-03-09

Repo and SHA:
- fight-caves-RL: `2365506bd3ea5cce515c571f39c24e72a38acc67`

Runtime used in this audit:
- JVM: Temurin `21.0.10+7`
- host topology: WSL2 on AMD Ryzen 5 5600G, 6 cores / 12 threads
- primary profiler attempted: Java Flight Recorder (`jfr`)
- direct attach tool attempted: `jcmd`

This report records what was and was not measurable about the JVM/headless runtime in this audit pass.

## Goal

Determine whether the JVM/headless sim itself is already the dominant bottleneck, or whether the outer RL stack is slower.

## What Was Measured

### 1. Existing Pure-JVM Benchmark Artifact

Source:
- `/home/jordan/code/fight-caves-RL/docs/performance_report.md`

Existing current-repo artifact values:
- throughput benchmark: `8891.93` ticks/s
- soak benchmark: `9186.05` ticks/s

Important limitation:
- this artifact is real repo evidence, but it was generated in a Windows-native environment, not on the WSL host used for the rest of this audit packet

### 2. Current-Host JUnit Sanity Run

Command:

```bash
source /home/jordan/code/.workspace-env.sh
cd /home/jordan/code/fight-caves-RL
./gradlew --no-daemon :game:test --tests '*HeadlessStepRateBenchmarkTest'
```

Outcome:
- passed on the current WSL host
- the test does not emit numeric throughput itself; it only enforces a minimum gate

### 3. Current-Host JFR Attempts

Attempt A:
- embedded-JVM bridge benchmark with:

```bash
JAVA_TOOL_OPTIONS='-XX:StartFlightRecording=filename=/tmp/fc_perf_audit/sim_bridge64.jfr,settings=profile,dumponexit=true'
```

Outcome:
- recording startup message appeared
- dump failed on exit and produced a zero-byte file

Attempt B:
- Gradle JUnit run with:

```bash
JAVA_TOOL_OPTIONS='-XX:StartFlightRecording=filename=/tmp/fc_perf_audit/sim_step_test.jfr,settings=profile,dumponexit=true'
./gradlew --no-daemon :game:test --tests '*HeadlessStepRateBenchmarkTest'
```

Outcome:
- produced a non-zero JFR file: `/tmp/fc_perf_audit/sim_step_test.jfr`
- `jfr summary` succeeded
- `jfr print` pretty-printer hit internal errors on some frames in this JDK build
- JSON export succeeded after the worker exited

Benchmark scenario and sample duration:
- scenario: `HeadlessStepRateBenchmarkTest`
- resulting JFR duration: `319 s`
- extra JVM flags used: `-XX:StartFlightRecording=filename=/tmp/fc_perf_audit/sim_step_test.jfr,settings=profile,dumponexit=true`

Result quality:
- CPU and allocation samples were dominated by Gradle worker / JUnit / JaCoCo harness activity
- extracted CPU sample set contained zero headless-symbol hits for:
  - `HeadlessObservationBuilder`
  - `HeadlessActionAdapter`
  - `FightCave`
  - `GameTick`
  - `GameLoop`
- extracted allocation sample set also contained zero headless-symbol hits

JFR summary highlights:
- duration: `319 s`
- `jdk.ExecutionSample`: `521`
- `jdk.ObjectAllocationSample`: `4723`
- `jdk.GarbageCollection`: `27`

Allocation and GC summary from the contaminated capture:
- top allocation objects:
  - `[B`: `2514`
  - `java/lang/String`: `180`
  - `[Ljava/lang/Object;`: `126`
  - `java/lang/ThreadLocal$ThreadLocalMap$Entry`: `125`
  - `java/util/ArrayList`: `77`
- GC counts:
  - `G1New`: `19`
  - `G1Old`: `8`

Interpretation:
- this JFR file is not clean enough to attribute CPU or allocation cost to the headless fight-caves runtime itself
- the profiler signal is contaminated by the Gradle/JUnit/JaCoCo harness

### 4. Attach Tooling Result

Command:

```bash
source /home/jordan/code/.workspace-env.sh
jcmd -l
```

Outcome:
- `jcmd` sees normal Java processes such as Gradle workers
- it does not expose the embedded JPype JVM inside the RL Python benchmark as an attachable JVM target

Interpretation:
- direct attach-based JFR on the embedded bridge runtime is not available with the current setup

## Evidence-Based Conclusions

Facts:
- There is a real direct-JVM artifact in the repo at about `8.9k` ticks/s.
- The current WSL host can pass the step-rate JUnit benchmark.
- Clean JVM-only hot-function sampling was not obtained in this pass because the available JFR routes were either dump-failing or harness-contaminated.

What this does and does not prove:
- It does prove the sim is not already in the `100000+` SPS regime.
- It does not prove which exact JVM functions dominate on the WSL host in isolation.

## What Remains Uncertain

- exact current-host CPU flamegraph for the headless runtime only
- exact current-host allocation hot spots for the headless runtime only
- exact current-host GC fraction attributable only to the benchmarked fight-caves path

## Best Supported Next Sim-Side Targets

These are still hypotheses, not profiler-proven facts:
- `HeadlessObservationBuilder`
- `HeadlessActionAdapter.visibleNpcTargets(...)`
- per-tick visible-NPC sorting and map/list construction
- any repeated `LinkedHashMap` / ordered-object construction in the observation path

Reason:
- the outer Python profile shows that observation retrieval is cheap relative to Python object conversion, which means the next high-value sim-side work is likely to be observation emission and transport shape, not generic combat math tuning first

## Audit Position

The direct sim profiler work in this pass is inconclusive on function-level attribution, but it is still useful:
- it confirms the tooling limitations
- it preserves the direct-JVM artifact boundary
- it prevents overclaiming about sim hotspots without clean samples
