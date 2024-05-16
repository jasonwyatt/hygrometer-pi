package us.jwf.hygrometer.common

import kotlinx.serialization.Serializable
import us.jwf.hygrometer.common.util.Parcelable
import us.jwf.hygrometer.common.util.Parcelize

@Serializable
@Parcelize
data class Servers(
    val servers: List<Server>,
) : Parcelable

@Serializable
@Parcelize
data class Server(
    val plantName: String,
    val baseUrl: String,
    val configPath: String,
    val listServersPath: String,
    val readingPath: String,
    val version: Int,
) : Parcelable
