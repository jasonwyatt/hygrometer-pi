package us.jwf.hygrometer.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import us.jwf.hygrometer.JmDNSKey
import us.jwf.hygrometer.SERVICE_NAME
import us.jwf.hygrometer.updateServerInfo
import us.jwf.hygromter.common.ConfigFile
import us.jwf.hygromter.common.Server
import us.jwf.hygromter.common.Servers

fun Application.configureRouting() {

    routing {
        get("/config") {
            call.respond(readConfigFile().sanitizeForWeb())
        }
        post("/config") {
            val authPayload = call.receive<ConfigFile.Partial>()
            // validate
            // ...
            // save
            val newConfig = readConfigFile().merge(authPayload).save()
            application.updateServerInfo(newConfig)
            call.respond(newConfig.sanitizeForWeb())
        }
        get("/servers") {
            val jmdns = application.attributes[JmDNSKey]
            val servers = jmdns.list("_http._tcp.local.").filter { SERVICE_NAME in it.name }
                .map {
                    println(it)
                    Json.decodeFromString<Server>(it.niceTextString.split("=")[1])
                }
            call.respond(Servers(servers))
        }
    }
}

suspend fun readConfigFile(): ConfigFile {
    val configFilePath = System.getenv("HYGROMETER_CONFIG_PATH")
    val fileContents = withContext(Dispatchers.IO) {
        FileSystem.SYSTEM.read(configFilePath.toPath()) {
            buildString {
                do {
                    val line = readUtf8Line()?.let(::appendLine)
                } while (line != null)
            }
        }
    }
    return Json.decodeFromString(fileContents)
}

@Suppress("JSON_FORMAT_REDUNDANT")
suspend fun ConfigFile.save(): ConfigFile {
    val configFilePath = System.getenv("HYGROMETER_CONFIG_PATH")
    val fileContents = Json { prettyPrint = true }
        .encodeToString(this)
    withContext(Dispatchers.IO) {
        FileSystem.SYSTEM.write(configFilePath.toPath(), mustCreate = false) {
            writeUtf8(fileContents)
            flush()
        }
    }
    return this
}
