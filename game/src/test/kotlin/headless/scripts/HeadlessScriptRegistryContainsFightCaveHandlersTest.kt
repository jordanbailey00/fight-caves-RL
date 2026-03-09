import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skills
import world.gregs.voidps.engine.timer.TimerApi
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HeadlessScriptRegistryContainsFightCaveHandlersTest {

    @AfterEach
    fun cleanup() {
        resetHeadlessTestRuntime()
    }

    @Test
    fun `headless script registry contains required fight cave handlers`() {
        val runtime = bootstrapHeadlessWithScripts(startWorld = false)

        assertEquals(runtime.scriptAllowlist.classNames.toSet(), runtime.loadedScriptClasses)

        assertTrue(Operation.playerObject.containsKey("Enter:cave_entrance_fight_cave"))
        assertTrue(Operation.playerObject.containsKey("Enter:cave_exit_fight_cave"))

        assertTrue(Despawn.hasNpcDespawnHandler("tztok_jad"))
        assertTrue(Death.hasNpcDeathHandler("tz_kek"))

        assertTrue(CombatApi.hasNpcDamageHandler("tz_kek", "*"))
        assertTrue(CombatApi.hasNpcDamageHandler("tz_kek_spawn_point", "*"))
        assertTrue(CombatApi.hasNpcAttackHandler("tztok_jad", "magic"))
        assertTrue(CombatApi.hasNpcAttackHandler("tztok_jad", "range"))
        assertTrue(CombatApi.hasNpcConditionHandler("weakened_nearby_monsters"))

        assertTrue(Skills.hasNpcLevelChangedHandler("tztok_jad", Skill.Constitution))
        assertTrue(Moved.hasNpcMovedHandler("yt_hur_kot"))
        assertTrue(TimerApi.hasNpcTimerStartHandler("yt_hur_kot_heal"))
        assertTrue(TimerApi.hasNpcTimerTickHandler("yt_hur_kot_heal"))

        runtime.tick()
    }
}
