package us.jwf.hygromter.common

import kotlinx.serialization.Serializable

@Serializable
data class Servers(
    val servers: List<Server>,
)

@Serializable
data class Server(
    val plantName: String,
    val baseUrl: String,
    val configPath: String,
    val listServersPath: String,
    val readingPath: String,
    val version: Int,
)
