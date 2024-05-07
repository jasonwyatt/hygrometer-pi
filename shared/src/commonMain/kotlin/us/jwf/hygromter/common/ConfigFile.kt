package us.jwf.hygromter.common

import kotlinx.serialization.Serializable

@Serializable
data class ConfigFile(
    val devicePin: Int,
    val sampleDurationSeconds: Int,
    val smsPhoneNumber: String,
    val plantName: String,
    val thresholdVoltage: Float,
    val twilioAccountSid: String,
    val twilioAuthToken: String,
    val socketPath: String,
) {
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
}
