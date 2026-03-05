import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.AuditLog
import java.util.*

data class HeadlessRuntime(
    val settings: Properties,
    val configFiles: ConfigFiles,
    val allowlist: HeadlessDataAllowlist,
    val scriptAllowlist: HeadlessScriptAllowlist,
    val loadedScriptClasses: Set<String>,
    val loadedScripts: List<Script>,
    val stages: List<Runnable>,
    val gameLoop: GameLoop,
) {
    fun tick(times: Int = 1) {
        repeat(times) {
            GameLoop.tick++
            gameLoop.tick()
        }
    }

    fun shutdown() {
        World.clear()
        stopKoin()
        Settings.clear()
    }
}

object HeadlessMain {
    private val logger = InlineLogger()

    fun bootstrap(
        loadContentScripts: Boolean = true,
        startWorld: Boolean = true,
        installShutdownHook: Boolean = true,
        settingsOverrides: Map<String, String> = emptyMap(),
    ): HeadlessRuntime {
        AuditLog.info("startup")
        val startTime = System.currentTimeMillis()
        val settings = loadRuntimeSettings(RuntimeMode.Headless, settingsOverrides)
        val allowlist = loadHeadlessDataAllowlist()
        val configFiles = loadHeadlessConfigFiles(allowlist)
        val scriptAllowlist = loadHeadlessScriptAllowlist()

        val cache = world.gregs.voidps.engine.timed("cache") { Cache.load(settings) }
        val scripts =
            preloadRuntime(
                cache = cache,
                configFiles = configFiles,
                runtimeMode = RuntimeMode.Headless,
                extraModules = listOf(HeadlessModules.module()),
                loadContentScripts = loadContentScripts,
                installShutdownHook = installShutdownHook,
                scriptAllowlist = if (loadContentScripts) scriptAllowlist.classNames.toSet() else null,
            )

        if (loadContentScripts) {
            validateHeadlessScriptRuntime(scriptAllowlist, scripts)
        }

        if (startWorld) {
            World.start(configFiles)
        }

        val stages = getHeadlessTickStages()
        val loadedScriptClasses = scripts.map { it::class.qualifiedName ?: it::class.java.name }.toSet()
        val runtime = HeadlessRuntime(settings, configFiles, allowlist, scriptAllowlist, loadedScriptClasses, scripts, stages, GameLoop(stages))

        logger.info {
            "${Settings["server.name"]} loaded in ${System.currentTimeMillis() - startTime}ms (mode=${currentRuntimeMode()}, headless_stages=${stages.size}, data_allowlist='${allowlist.sourcePath}', scripts=${loadedScriptClasses.size}, script_allowlist='${scriptAllowlist.sourcePath}')"
        }
        return runtime
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val runtime = bootstrap()
        val scope = CoroutineScope(Contexts.Game)
        val engine = runtime.gameLoop.start(scope)
        AuditLog.info("game online")
        runBlocking {
            try {
                engine.join()
            } finally {
                engine.cancel()
                runtime.shutdown()
                AuditLog.info("game offline")
            }
        }
    }
}
