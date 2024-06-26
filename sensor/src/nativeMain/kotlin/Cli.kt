import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.posix.*
import us.jwf.hygrometer.common.ConfigFile
import us.jwf.hygrometer.common.Reading
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@OptIn(ExperimentalForeignApi::class)
class Cli  {
    val json = Json.Default

    fun getConfig(args: Array<String>): ConfigFile = memScoped {
        val raw = FileSystem.SYSTEM.source(args[0].toPath())
            .use { fileSource ->
                fileSource.buffer()
                    .use { buffer ->
                        buildString {
                            do {
                                val line = buffer.readUtf8Line()?.let { line -> append(line) }
                            } while (line != null)
                        }
                    }
            }
        json.decodeFromString(raw)
    }

    fun run(args: Array<String>) {
        State.setUp()
        signal(SIGINT, State.closeHandler)
        signal(SIGTERM, State.closeHandler)

        val config = getConfig(args)
        val start = TimeSource.Monotonic.markNow()
        val average = State.spi.listenVoltage(config.devicePin)
            .takeWhile {
                TimeSource.Monotonic.markNow() - start < config.sampleDurationSeconds.seconds
            }
            .onEach {
                usleep(10000u)
            }
            .average()
        val result = Reading(
            voltage = average.toFloat(),
            needsWater = average < config.thresholdVoltage
        )
        println(Json.encodeToString(result))

        State.tearDown()
    }
}