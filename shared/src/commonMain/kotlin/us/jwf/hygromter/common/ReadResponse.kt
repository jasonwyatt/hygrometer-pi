package us.jwf.hygromter.common

import kotlinx.serialization.Serializable

@Serializable
data class ReadResponse(
    val deviceConfig: ConfigFile,
    val reading: Reading,
)
