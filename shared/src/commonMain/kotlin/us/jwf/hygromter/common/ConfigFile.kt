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
)
