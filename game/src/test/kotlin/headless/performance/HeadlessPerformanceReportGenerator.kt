import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object HeadlessPerformanceReportGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        val output = generateAndWriteLog()
        println("Performance benchmark log written to $output")
    }

    fun generateAndWriteLog(output: Path = locateRepositoryRoot().resolve("docs/performance_benchmark.log")): Path {
        val report = runBenchmarkSuite()
        writeLog(output, report)
        return output
    }

    fun runBenchmarkSuite(): PerformanceBenchmarkSuiteResult {
        val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val runtime = bootstrapHeadlessWithScripts(startWorld = false)
        return try {
            val player = createHeadlessPlayer("perf-suite")
            val throughputResult = runThroughputBenchmark(runtime, player)
            val soakResult = runSoakBenchmark(runtime, player)

            PerformanceBenchmarkSuiteResult(
                generatedAtUtc = timestamp,
                throughput = throughputResult,
                soak = soakResult,
            )
        } finally {
            runtime.shutdown()
            resetHeadlessTestRuntime()
        }
    }

    private fun runThroughputBenchmark(runtime: FightCaveSimulationRuntime, player: world.gregs.voidps.engine.entity.character.player.Player): ThroughputBenchmarkResult {
        runtime.resetFightCaveEpisode(player, seed = 11_110L, startWave = 1)
        val steps = List(10_000) { HeadlessReplayStep(HeadlessAction.Wait, ticksAfter = 1) }
        val result = runtime.runFightCaveBatch(player, steps, observeEvery = 0)
        return ThroughputBenchmarkResult(
            steps = result.steps,
            ticksAdvanced = result.ticksAdvanced,
            elapsedNanos = result.elapsedNanos,
            stepsPerSecond = result.stepsPerSecond,
            ticksPerSecond = result.ticksPerSecond,
        )
    }

    private fun runSoakBenchmark(runtime: FightCaveSimulationRuntime, player: world.gregs.voidps.engine.entity.character.player.Player): SoakBenchmarkResult {
        runtime.resetFightCaveEpisode(player, seed = 22_220L, startWave = 1)
        val steps = List(20_000) { HeadlessReplayStep(HeadlessAction.Wait, ticksAfter = 1) }
        val result = runtime.runFightCaveBatch(player, steps, observeEvery = 500)
        val final = result.finalObservation
        return SoakBenchmarkResult(
            steps = result.steps,
            ticksAdvanced = result.ticksAdvanced,
            elapsedNanos = result.elapsedNanos,
            ticksPerSecond = result.ticksPerSecond,
            finalTick = final.tick,
            finalWave = final.wave.wave,
            finalRemaining = final.wave.remaining,
            finalHitpoints = final.player.hitpointsCurrent,
            finalPrayer = final.player.prayerCurrent,
        )
    }

    private fun writeLog(path: Path, report: PerformanceBenchmarkSuiteResult) {
        Files.createDirectories(path.parent)
        val log =
            buildString {
                appendLine("generated_at_utc=${report.generatedAtUtc}")
                appendLine("throughput.steps=${report.throughput.steps}")
                appendLine("throughput.ticks_advanced=${report.throughput.ticksAdvanced}")
                appendLine("throughput.elapsed_nanos=${report.throughput.elapsedNanos}")
                appendLine("throughput.steps_per_second=${"%.2f".format(report.throughput.stepsPerSecond)}")
                appendLine("throughput.ticks_per_second=${"%.2f".format(report.throughput.ticksPerSecond)}")
                appendLine("soak.steps=${report.soak.steps}")
                appendLine("soak.ticks_advanced=${report.soak.ticksAdvanced}")
                appendLine("soak.elapsed_nanos=${report.soak.elapsedNanos}")
                appendLine("soak.ticks_per_second=${"%.2f".format(report.soak.ticksPerSecond)}")
                appendLine("soak.final_tick=${report.soak.finalTick}")
                appendLine("soak.final_wave=${report.soak.finalWave}")
                appendLine("soak.final_remaining=${report.soak.finalRemaining}")
                appendLine("soak.final_hitpoints=${report.soak.finalHitpoints}")
                appendLine("soak.final_prayer=${report.soak.finalPrayer}")
            }
        Files.writeString(path, log)
    }

    private fun locateRepositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (true) {
            if (Files.isRegularFile(current.resolve("spec.md")) && Files.isDirectory(current.resolve("docs"))) {
                return current
            }
            current = current.parent ?: break
        }
        error("Unable to locate repository root from ${Path.of("").toAbsolutePath().normalize()}.")
    }
}

data class PerformanceBenchmarkSuiteResult(
    val generatedAtUtc: String,
    val throughput: ThroughputBenchmarkResult,
    val soak: SoakBenchmarkResult,
)

data class ThroughputBenchmarkResult(
    val steps: Int,
    val ticksAdvanced: Int,
    val elapsedNanos: Long,
    val stepsPerSecond: Double,
    val ticksPerSecond: Double,
)

data class SoakBenchmarkResult(
    val steps: Int,
    val ticksAdvanced: Int,
    val elapsedNanos: Long,
    val ticksPerSecond: Double,
    val finalTick: Int,
    val finalWave: Int,
    val finalRemaining: Int,
    val finalHitpoints: Int,
    val finalPrayer: Int,
)