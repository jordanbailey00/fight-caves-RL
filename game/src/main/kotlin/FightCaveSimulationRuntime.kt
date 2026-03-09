import world.gregs.voidps.engine.entity.character.player.Player

interface FightCaveSimulationRuntime {
    fun tick(times: Int = 1)

    fun resetFightCaveEpisode(player: Player, config: FightCaveEpisodeConfig): FightCaveEpisodeState

    fun resetFightCaveEpisode(player: Player, seed: Long, startWave: Int = 1): FightCaveEpisodeState

    fun visibleFightCaveNpcTargets(player: Player): List<HeadlessVisibleNpcTarget>

    fun applyFightCaveAction(player: Player, action: HeadlessAction): HeadlessActionResult

    fun observeFightCave(player: Player, includeFutureLeakage: Boolean = false): HeadlessObservationV1

    fun observeFightCaveFlat(player: Player): HeadlessTrainingFlatObservationV1

    fun shutdown()
}
