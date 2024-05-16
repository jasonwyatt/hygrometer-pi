package us.jwf.hygrometer.common

import kotlinx.serialization.Serializable
import us.jwf.hygrometer.common.util.Parcelable
import us.jwf.hygrometer.common.util.Parcelize

@Serializable
@Parcelize
data class ConfigFile(
    val devicePin: Int,
    val sampleDurationSeconds: Int,
    val smsPhoneNumber: String,
    val plantName: String,
    val thresholdVoltage: Float,
    val twilioAccountSid: String,
    val twilioAuthToken: String,
    val socketPath: String,
) : Parcelable {
    fun sanitizeForWeb() = copy(twilioAuthToken = "[REDACTED]")

    fun merge(partial: Partial) =
        copy(
            devicePin = partial.devicePin ?: devicePin,
            sampleDurationSeconds = partial.sampleDurationSeconds ?: sampleDurationSeconds,
            smsPhoneNumber = partial.smsPhoneNumber ?: smsPhoneNumber,
            plantName = partial.plantName ?: plantName,
            thresholdVoltage = partial.thresholdVoltage ?: thresholdVoltage,
            twilioAccountSid = partial.twilioAccountSid ?: twilioAccountSid,
            twilioAuthToken = partial.twilioAuthToken ?: twilioAuthToken,
            socketPath = socketPath
        )

    @Serializable
    data class Partial(
        val devicePin: Int? = null,
        val sampleDurationSeconds: Int? = null,
        val smsPhoneNumber: String? = null,
        val plantName: String? = null,
        val thresholdVoltage: Float? = null,
        val twilioAccountSid: String? = null,
        val twilioAuthToken: String? = null,
    )

    companion object {
        val DEFAULT = ConfigFile(
            devicePin = 0,
            sampleDurationSeconds = 5,
            smsPhoneNumber = "+15555555555",
            plantName = "Unnamed plant",
            thresholdVoltage = 1.5f,
            twilioAccountSid = "unknown",
            twilioAuthToken = "unknown",
            socketPath = "/tmp/hygrometer.sock"
        )
    }
}
