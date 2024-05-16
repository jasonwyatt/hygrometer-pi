package us.jwf.hygrometer.api

import android.net.http.HttpException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import us.jwf.hygrometer.common.ConfigFile
import us.jwf.hygrometer.common.ReadResponse
import us.jwf.hygrometer.common.Reading
import us.jwf.hygrometer.common.Server
import kotlin.time.Duration

@OptIn(ExperimentalSerializationApi::class, ExperimentalCoroutinesApi::class)
class ServerRepository(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    fun getConfig(server: Server, pollInterval: Duration): Flow<ConfigFile> = flow {
        val request = Request.Builder()
            .get()
            .url("${server.baseUrl}${server.configPath}")
            .build()
        while (currentCoroutineContext().isActive) {
            val response = okHttpClient.newCall(request).executeAsync()
            if (response.isSuccessful) {
                emit(json.decodeFromStream(response.body.byteStream()))
            }
            delay(pollInterval)
        }
    }

    suspend fun takeReading(server: Server): Result<Reading> {
        val request = Request.Builder()
            .get()
            .url("${server.baseUrl}${server.readingPath}")
            .build()
        val response = okHttpClient.newCall(request).executeAsync()
        return if (response.isSuccessful) {
            Result.success(json.decodeFromStream<ReadResponse>(response.body.byteStream()).reading)
        } else {
            Result.failure(HttpException("Request: $request failed: ${response.code}", null))
        }
    }
}
