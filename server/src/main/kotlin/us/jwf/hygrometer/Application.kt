package us.jwf.hygrometer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import us.jwf.hygrometer.plugins.configureRouting
import us.jwf.hygrometer.plugins.readConfigFile
import us.jwf.hygromter.common.ConfigFile
import us.jwf.hygromter.common.Server
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

const val VERSION = 1
const val SERVICE_NAME = "HygrometerServer"

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureRouting()
    install(ContentNegotiation) {
        json(json = Json { isLenient = true })
    }
    install(JmDNSPlugin)
}

val JmDNSKey = AttributeKey<JmDNS>("JMD_NS")

val JmDNSPlugin = createApplicationPlugin("jmdns") {
    val localHost = InetAddress.getLocalHost()
    val jmdns = JmDNS.create(localHost)
    application.attributes.put(JmDNSKey, jmdns)
    application.updateServerInfo()
    on(MonitoringEvent(ApplicationStopped)) {
        jmdns.unregisterAllServices()
    }
}

@Suppress("HttpUrlsUsage")
fun Application.updateServerInfo(configFile: ConfigFile? = null) {
    val config = configFile ?: runBlocking { readConfigFile() }
    val localHost = InetAddress.getLocalHost()
    val baseUrl = "http://${localHost.hostAddress}:${engine.environment.config.port}"
    val server = Server(
        plantName = config.plantName,
        configUrl = "$baseUrl/config",
        listServersUrl = "$baseUrl/servers",
        version = VERSION
    )

    val service = ServiceInfo.create(
        /* type = */ "_http._tcp.local.",
        /* name = */ "$SERVICE_NAME-${localHost.hostAddress.encodeBase64()}",
        /* port = */ 8080,
        /* text = */ "info=${Json.encodeToString(server)}"
    )
    val jmdns = attributes[JmDNSKey]
    jmdns.unregisterAllServices()
    jmdns.registerService(service)
}
