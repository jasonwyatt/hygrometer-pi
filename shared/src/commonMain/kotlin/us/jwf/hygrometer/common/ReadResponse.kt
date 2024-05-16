package us.jwf.hygrometer.common

import kotlinx.serialization.Serializable
import us.jwf.hygrometer.common.util.Parcelable
import us.jwf.hygrometer.common.util.Parcelize

@Serializable
@Parcelize
data class ReadResponse(
    val deviceConfig: ConfigFile,
    val reading: Reading,
) : Parcelable
