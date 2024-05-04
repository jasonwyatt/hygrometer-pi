package us.jwf.hygrometer.common

import kotlinx.serialization.json.Json
import us.jwf.hygromter.common.ConfigFile
import kotlin.test.Test

class ConfigFileTest {
    @Test
    fun buildFile() {
        val config = ConfigFile(
            devicePin = 0,
            sampleDurationSeconds = 5,
            smsPhoneNumber = "+15555555555",
            plantName = "Hosta",
            thresholdVoltage = 0.5f,
            twilioAccountSid = "asdf",
            twilioAuthToken = "wafs"
        )

        val json = Json { prettyPrint = true }

        println(json.encodeToString(ConfigFile.serializer(), config))
    }
}
