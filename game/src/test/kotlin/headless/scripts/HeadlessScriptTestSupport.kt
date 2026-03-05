import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile

internal fun bootstrapHeadlessWithScripts(startWorld: Boolean = false): HeadlessRuntime =
    HeadlessMain.bootstrap(
        loadContentScripts = true,
        startWorld = startWorld,
        installShutdownHook = false,
        settingsOverrides = headlessTestOverrides(),
    )

internal fun createHeadlessPlayer(name: String, tile: Tile = Tile(2438, 5168)): Player {
    val accounts: AccountManager = get()
    val player = Player(tile = tile, accountName = name, passwordHash = "")
    check(accounts.setup(player, null, 0, viewport = true)) { "Failed to setup headless test player '$name'." }
    player["creation"] = -1
    player["skip_level_up"] = true
    accounts.spawn(player, null)
    player.viewport?.loaded = true
    return player
}
