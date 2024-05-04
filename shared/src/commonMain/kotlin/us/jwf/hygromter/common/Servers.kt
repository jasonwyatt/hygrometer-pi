package us.jwf.hygromter.common

import kotlinx.serialization.Serializable

@Serializable
data class Servers(
    val servers: List<Server>,
)

@Serializable
data class Server(
    val plantName: String,
    val configUrl: String,
    val listServersUrl: String,
    val version: Int,
)
