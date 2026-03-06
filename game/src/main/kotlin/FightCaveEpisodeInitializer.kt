import content.area.karamja.tzhaar_city.TzhaarFightCave
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.quest.clearInstance
import content.quest.instance
import content.quest.instanceOffset
import content.quest.smallInstance
import content.skill.prayer.PrayerConfigs
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setSeededRandom

data class FightCaveEpisodeConfig(
    val seed: Long,
    val startWave: Int = 1,
    val ammo: Int = 1000,
    val prayerPotions: Int = 8,
    val sharks: Int = 20,
)

data class FightCaveEpisodeState(
    val seed: Long,
    val wave: Int,
    val rotation: Int,
    val remaining: Int,
    val instanceId: Int,
    val playerTile: Tile,
)

class FightCaveEpisodeInitializer(
    private val fightCave: TzhaarFightCave,
    private val configFiles: ConfigFiles,
) {

    fun reset(player: Player, config: FightCaveEpisodeConfig): FightCaveEpisodeState {
        require(config.startWave in 1..63) { "Fight Caves start wave must be in range 1..63, got ${config.startWave}." }
        require(config.ammo > 0) { "Ammo amount must be > 0, got ${config.ammo}." }
        require(config.prayerPotions >= 0) { "Prayer potion count cannot be negative, got ${config.prayerPotions}." }
        require(config.sharks >= 0) { "Shark count cannot be negative, got ${config.sharks}." }

        setSeededRandom(config.seed, trackCalls = true)
        player[EPISODE_SEED_KEY] = config.seed

        fightCave.ensureWavesLoaded(configFiles)

        resetTransientState(player)
        resetFightCaveState(player)
        resetPlayerStatsAndResources(player)
        resetPlayerLoadout(player, config)
        resetFightCaveInstance(player, config)

        return FightCaveEpisodeState(
            seed = config.seed,
            wave = player[FIGHT_CAVE_WAVE_KEY, -1],
            rotation = player[FIGHT_CAVE_ROTATION_KEY, -1],
            remaining = player[FIGHT_CAVE_REMAINING_KEY, 0],
            instanceId = checkNotNull(player.instance()).id,
            playerTile = player.tile,
        )
    }

    private fun resetTransientState(player: Player) {
        player.queue.clear()
        player.softTimers.stopAll()
        player.timers.stopAll()
        player.mode = EmptyMode
        player.steps.clear()
        player.clearFace()
        player.clearWatch()
        player.clearAnim()
        player.clearGfx()

        for (clock in RESET_CLOCKS) {
            player.stop(clock)
        }

        player["logged_out"] = false
    }

    private fun resetFightCaveState(player: Player) {
        clearPreviousInstance(player)

        for (variable in RESET_VARIABLES) {
            player.clear(variable)
        }

        player["fight_caves_logout_warning"] = false
        player["headless_episode"] = true

        player[PrayerConfigs.PRAYERS] = ""
        player[PrayerConfigs.USING_QUICK_PRAYERS] = false
        player[PrayerConfigs.SELECTING_QUICK_PRAYERS] = false
        player.clear(PrayerConfigs.ACTIVE_PRAYERS)
        player.clear(PrayerConfigs.ACTIVE_CURSES)
        player.clear(PrayerConfigs.QUICK_PRAYERS)
        player.clear(PrayerConfigs.QUICK_CURSES)
        player.clear(PrayerConfigs.TEMP_QUICK_PRAYERS)
        player.clear("prayer_drain_counter")
    }

    private fun clearPreviousInstance(player: Player) {
        val previous = player.instance() ?: return
        repeat(4) { level ->
            NPCs.clear(previous.toLevel(level))
        }
        NPCs.run()
        player.clearInstance()
    }

    private fun resetPlayerStatsAndResources(player: Player) {
        for (skill in Skill.all) {
            player.experience.removeBlock(skill)
            val level = FIXED_LEVELS[skill] ?: 1
            player.experience.set(skill, Level.experience(skill, level))
            player.levels.set(skill, level)
            player.experience.addBlock(skill)
        }

        player.runEnergy = MAX_RUN_ENERGY
        player.running = true
        player["movement_temp"] = "run"
        player["skip_level_up"] = true
    }

    private fun resetPlayerLoadout(player: Player, config: FightCaveEpisodeConfig) {
        val equipmentReset =
            player.equipment.transaction {
                clear()
                set(EquipSlot.Hat.index, Item("coif"))
                set(EquipSlot.Weapon.index, Item("rune_crossbow"))
                set(EquipSlot.Chest.index, Item("black_dragonhide_body"))
                set(EquipSlot.Legs.index, Item("black_dragonhide_chaps"))
                set(EquipSlot.Hands.index, Item("black_dragonhide_vambraces"))
                set(EquipSlot.Feet.index, Item("snakeskin_boots"))
                set(EquipSlot.Ammo.index, Item("adamant_bolts", config.ammo))
            }
        check(equipmentReset) { "Failed to reset headless episode equipment loadout." }

        val inventoryReset =
            player.inventory.transaction {
                clear()
                add("prayer_potion_4", config.prayerPotions)
                add("shark", config.sharks)
            }
        check(inventoryReset) { "Failed to reset headless episode consumable inventory." }
    }

    private fun resetFightCaveInstance(player: Player, config: FightCaveEpisodeConfig) {
        player.smallInstance(fightCave.region, levels = 3)
        val offset = player.instanceOffset()
        player.tele(fightCave.entrance.add(offset))
        player.walkTo(fightCave.centre.add(offset))
        fightCave.startWave(player, config.startWave, start = false)
    }

    companion object {
        private const val EPISODE_SEED_KEY = "episode_seed"
        private const val FIGHT_CAVE_WAVE_KEY = "fight_cave_wave"
        private const val FIGHT_CAVE_ROTATION_KEY = "fight_cave_rotation"
        private const val FIGHT_CAVE_REMAINING_KEY = "fight_cave_remaining"

        private val RESET_CLOCKS =
            listOf(
                "delay",
                "movement_delay",
                "food_delay",
                "drink_delay",
                "combo_delay",
                "fight_cave_cooldown",
            )

        private val FIXED_LEVELS =
            mapOf(
                Skill.Attack to 1,
                Skill.Strength to 1,
                Skill.Defence to 70,
                Skill.Constitution to 700,
                Skill.Ranged to 70,
                Skill.Prayer to 43,
                Skill.Magic to 1,
            )

        private val RESET_VARIABLES =
            listOf(
                "fight_cave_wave",
                "fight_cave_rotation",
                "fight_cave_remaining",
                "fight_cave_start_time",
                "healed",
            )
    }
}
