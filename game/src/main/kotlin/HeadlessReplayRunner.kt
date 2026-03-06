import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.RandomDiagnostics
import world.gregs.voidps.type.randomCallCount
import world.gregs.voidps.type.randomDiagnostics

data class HeadlessReplayStep(
    val action: HeadlessAction,
    val ticksAfter: Int = 1,
)

data class HeadlessReplayTickSnapshot(
    val stepIndex: Int,
    val tick: Int,
    val action: HeadlessAction?,
    val actionResult: HeadlessActionResult?,
    val observation: HeadlessObservationV1,
    val rngCallCount: Long,
)

data class HeadlessReplayResult(
    val seed: Long,
    val startWave: Int,
    val snapshots: List<HeadlessReplayTickSnapshot>,
    val rngDiagnostics: RandomDiagnostics,
)

class HeadlessReplayRunner(
    private val runtime: FightCaveSimulationRuntime,
) {

    fun run(
        player: Player,
        seed: Long,
        actionTrace: List<HeadlessReplayStep>,
        startWave: Int = 1,
        includeFutureLeakage: Boolean = false,
    ): HeadlessReplayResult {
        require(startWave in 1..63) { "Fight Caves start wave must be in range 1..63, got $startWave." }
        for ((index, step) in actionTrace.withIndex()) {
            require(step.ticksAfter >= 0) { "Replay step $index has invalid ticksAfter=${step.ticksAfter}; expected >= 0." }
        }

        runtime.resetFightCaveEpisode(player, seed = seed, startWave = startWave)

        val snapshots = mutableListOf<HeadlessReplayTickSnapshot>()
        snapshots +=
            HeadlessReplayTickSnapshot(
                stepIndex = -1,
                tick = world.gregs.voidps.engine.GameLoop.tick,
                action = null,
                actionResult = null,
                observation = runtime.observeFightCave(player, includeFutureLeakage = includeFutureLeakage),
                rngCallCount = randomCallCount(),
            )

        for ((index, step) in actionTrace.withIndex()) {
            val actionResult = runtime.applyFightCaveAction(player, step.action)
            if (step.ticksAfter > 0) {
                runtime.tick(step.ticksAfter)
            }
            val observation = runtime.observeFightCave(player, includeFutureLeakage = includeFutureLeakage)
            snapshots +=
                HeadlessReplayTickSnapshot(
                    stepIndex = index,
                    tick = observation.tick,
                    action = step.action,
                    actionResult = actionResult,
                    observation = observation,
                    rngCallCount = randomCallCount(),
                )
        }

        return HeadlessReplayResult(
            seed = seed,
            startWave = startWave,
            snapshots = snapshots,
            rngDiagnostics = randomDiagnostics(),
        )
    }
}
