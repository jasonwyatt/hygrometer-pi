package us.jwf.hygrometer.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import us.jwf.hygrometer.CONFIG_PATH
import us.jwf.hygrometer.sensorclient.takeSensorReading
import us.jwf.hygrometer.common.ConfigFile
import us.jwf.hygrometer.common.ReadResponse
import us.jwf.hygrometer.common.Server
import us.jwf.hygrometer.common.Servers

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
            // Report to Avahi
            application.updateConfigFile(newConfig)
            call.respond(newConfig.sanitizeForWeb())
        }
        get("/read") {
            val reading = takeSensorReading()
            call.respond(
                ReadResponse(
                    deviceConfig = readConfigFile(),
                    reading = reading
                )
            )
        }
        get("/servers") {
            val servers = Avahi.list { record ->
                Json.decodeFromString<Server>(record["info"]!!.decodeBase64String())
            }
            call.respond(Servers(servers))
        }
    }
}

suspend fun readConfigFile(): ConfigFile {
    val fileContents = withContext(Dispatchers.IO) {
        val path = CONFIG_PATH.toPath()
        if (path.toFile().exists()) {
            FileSystem.SYSTEM.read(CONFIG_PATH.toPath()) {
                buildString {
                    do {
                        val line = readUtf8Line()?.let(::appendLine)
                    } while (line != null)
                }
            }
        } else {
            Json.encodeToString(ConfigFile.DEFAULT.save())
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
