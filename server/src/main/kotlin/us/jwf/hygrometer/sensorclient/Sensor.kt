package us.jwf.hygrometer.sensorclient

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import us.jwf.hygrometer.CONFIG_PATH
import us.jwf.hygrometer.SENSOR_PATH
import us.jwf.hygrometer.plugins.readConfigFile
import us.jwf.hygromter.common.Reading
import java.time.Clock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun takeSensorReading(): Reading = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { cont ->
        val process = Runtime.getRuntime().exec(
            arrayOf(
                SENSOR_PATH,
                CONFIG_PATH
            )
        )
        cont.invokeOnCancellation { process.destroyForcibly() }

        try {
            val input = process.inputReader().readLines().joinToString("\n")
            cont.resume(Json.decodeFromString(input))
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }
}
