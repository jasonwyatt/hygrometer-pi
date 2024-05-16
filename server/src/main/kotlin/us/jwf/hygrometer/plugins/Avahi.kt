package us.jwf.hygrometer.plugins

import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import us.jwf.hygrometer.VERSION
import us.jwf.hygrometer.plugins.Avahi.config
import us.jwf.hygrometer.plugins.Avahi.scope
import us.jwf.hygrometer.common.ConfigFile
import us.jwf.hygrometer.common.Server
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Requires `avahi-utils` to be installed on the machine and in the path..
 */
object Avahi : ApplicationPlugin<Avahi.Config> by createApplicationPlugin(
    name = "avahi",
    createConfiguration = { Config("Service") },
    body = {
        config = pluginConfig
        application.attributes.put(Avahi.configAttributeKey, pluginConfig)
        application.updateConfigFile()
    },
) {
    lateinit var config: Config

    private val configAttributeKey = AttributeKey<Config>("avahi-config")
    internal val scope = CoroutineScope(Dispatchers.IO)
    private val cmdJob = atomic<Job?>(null)

    fun announce(port: Int, txtData: Map<String, String>) {
        cmdJob.update { oldJob ->
            scope.launch {
                oldJob?.cancelAndJoin()
                announceInternal(port, txtData)
            }
        }
    }

    suspend fun <T> list(
        txtRecordHandler: (Map<String, String>) -> T?
    ): List<T> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            val process = Runtime.getRuntime().exec(
                arrayOf(
                    "avahi-browse",
                    "-rpt",
                    "_http._tcp."
                )
            )
            cont.invokeOnCancellation { process.destroyForcibly() }

            // =;wlan0;IPv4;MSS620-acfe;_hap._tcp;local;Meross_Smart_Plug.local;192.168.0.7;52432;"sh=UnUH6A==" "ci=7"
            val records = process.inputReader().lineSequence()
                .filter { it.startsWith("=") && "IPv4" in it }
                .mapNotNull { result ->
                    println(result)
                    result.split(";")
                        .takeIf { it.size == 10 }
                        ?.last()?.takeIf { it.isNotBlank() }
                        ?.split(" ")
                        ?.mapNotNull {
                            val record = it.drop(1).dropLast(1)
                            val equals = record.indexOf('=')
                            if (equals < 0) {
                                null
                            } else {
                                val key = record.substring(0, equals)
                                val value = record.substring(equals + 1)
                                key to value
                            }
                        }
                        ?.toMap()
                }
                .mapNotNull(txtRecordHandler)

            // Use set first to get rid of any duplicates
            cont.resume(records.toSet().toList())
        }
    }

    private suspend fun announceInternal(port: Int, txtData: Map<String, String>) {
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                while (cont.isActive && internal(port, txtData, cont)) {
                    // Keep re-establishing the connection unless there is an actual error.
                    println("Retrying avahi-publish")
                }
            }
        }
    }

    private fun internal(port: Int, txtData: Map<String, String>, cont: CancellableContinuation<Unit>): Boolean {
        val localHost = NetworkInterface.getNetworkInterfaces().findLocalHost()
        // avahi-publish [options] -s <name> <type> <port> [<txt ...>]
        val process = Runtime.getRuntime().exec(
            arrayOf(
                "avahi-publish",
                "-s",
                "${config.name}-${localHost.hostAddress.encodeBase64()}",
                "_http._tcp",
                port.toString(),
                txtData.toTxtRecord()
            )
        )
        cont.invokeOnCancellation { process.destroyForcibly() }

        val reader = process.inputReader()
        val isRunning = "Established under name" in (reader.readLine() ?: "")
        return try {
            reader.forEachLine { println(it) }
            if (!isRunning) {
                val errors = process.errorReader().readLines()
                cont.resumeWithException(RuntimeException("Could not start: ${errors.joinToString("\n")}"))
            }
            false
        } catch (e: IOException) {
            true
        }
    }

    private fun Map<String, String>.toTxtRecord(): String = entries.joinToString("\n") { "${it.key}=${it.value}" }

    data class Config(
        var name: String
    )
}

@Suppress("HttpUrlsUsage")
fun Application.updateConfigFile(configFile: ConfigFile? = null) {
    scope.launch {
        val config = configFile ?: readConfigFile()
        val localHost = NetworkInterface.getNetworkInterfaces().findLocalHost()
        val baseUrl = "http://${localHost.hostAddress}:${engine.environment.config.port}"
        val server = Server(
            plantName = config.plantName,
            baseUrl = baseUrl,
            configPath = "/config",
            listServersPath = "/servers",
            readingPath = "/read",
            version = VERSION
        )
        val jsonServer = Json.encodeToString(server)
        val base64Server = jsonServer.encodeBase64()
        Avahi.announce(this@updateConfigFile.engine.environment.config.port, mapOf("info" to base64Server))
    }
}

fun Enumeration<NetworkInterface>.findLocalHost(): InetAddress {
    return this
        .toList()
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<Inet4Address>()
        .first { !it.isLoopbackAddress }
}
