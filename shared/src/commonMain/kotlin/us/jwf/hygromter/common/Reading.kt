package us.jwf.hygromter.common

import kotlinx.serialization.Serializable

@Serializable
data class Reading(
    val voltage: Float,
    val needsWater: Boolean,
)
