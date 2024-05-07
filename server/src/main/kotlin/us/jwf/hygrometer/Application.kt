package us.jwf.hygrometer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import us.jwf.hygrometer.plugins.Avahi
import us.jwf.hygrometer.plugins.configureRouting
import us.jwf.hygrometer.plugins.findLocalHost
import java.net.NetworkInterface

const val VERSION = 1

val CONFIG_PATH: String
    get() = System.getenv("HYGROMETER_CONFIG_PATH")

val SENSOR_PATH: String
    get() = System.getenv("HYGROMETER_SENSOR_PATH")

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(json = Json { isLenient = true })
    }
    install(Avahi) {
        val localHost = NetworkInterface.getNetworkInterfaces().findLocalHost()
        name = "Hygrometer-${localHost.hostAddress.encodeBase64()}"
    }
    configureRouting()
}
