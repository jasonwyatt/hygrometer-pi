package us.jwf.hygrometer

import android.app.Application
import us.jwf.hygrometer.lookup.ServerLookup

class App : Application() {
    lateinit var serverLookup: ServerLookup
    override fun onCreate() {
        super.onCreate()
        serverLookup = ServerLookup(this)
    }
}
