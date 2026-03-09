import content.area.karamja.tzhaar_city.JAD_HIT_RESOLVE_OFFSET_TICKS
import content.area.karamja.tzhaar_city.beginJadTelegraphForAttack
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPCs
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class HeadlessReplayJadTelegraphTraceTest {

    @Test
    fun `replay snapshots capture jad telegraph timing from shared state`() {
        val runtime = bootstrapHeadlessWithScripts(startWorld = true)
        try {
            val player = createHeadlessPlayer("replay-jad-telegraph")
            val runner = HeadlessReplayRunner(runtime)
            val result =
                runner.run(
                    player = player,
                    seed = 630_031L,
                    actionTrace = emptyList(),
                    startWave = 1,
                    stepHook = { stepIndex, _, currentPlayer ->
                        if (stepIndex == -1) {
                            val jad = NPCs.add("tztok_jad", currentPlayer.tile.add(1, 0))
                            NPCs.run()
                            assertTrue(jad.beginJadTelegraphForAttack("magic"))
                        }
                    },
                )

            val snapshot = result.snapshots.single()
            val telegraph = assertNotNull(snapshot.jadTelegraph)
            val jadObservation = snapshot.observation.npcs.first { it.id == "tztok_jad" }

            assertEquals("magic_windup", telegraph.telegraphState)
            assertEquals("magic", telegraph.committedAttackStyle)
            assertEquals(snapshot.tick, telegraph.telegraphStartTick)
            assertEquals(snapshot.tick + JAD_HIT_RESOLVE_OFFSET_TICKS, telegraph.hitResolveTick)
            assertEquals(1, jadObservation.jadTelegraphState)
        } finally {
            runtime.shutdown()
            resetHeadlessTestRuntime()
        }
    }
}
