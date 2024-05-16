package us.jwf.hygrometer

import android.app.Application
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import us.jwf.hygrometer.api.ServerLookup
import us.jwf.hygrometer.api.ServerRepository

class App : Application() {
    lateinit var okHttpClient: OkHttpClient
    lateinit var json: Json
    lateinit var serverLookup: ServerLookup
    lateinit var serverRepository: ServerRepository

    override fun onCreate() {
        super.onCreate()
        okHttpClient = OkHttpClient.Builder().build()
        json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
        serverLookup = ServerLookup(this, json)
        serverRepository = ServerRepository(okHttpClient, json)
    }
}
