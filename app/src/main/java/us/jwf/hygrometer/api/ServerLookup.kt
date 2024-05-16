package us.jwf.hygrometer.api

import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Base64
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import us.jwf.hygrometer.App
import us.jwf.hygrometer.R
import us.jwf.hygrometer.common.Server

class ServerLookup(app: App, json: Json) : DiscoveryListener {
    private val wifiManager: WifiManager = app.getSystemService(WifiManager::class.java)
    private val nsdManager: NsdManager = app.getSystemService(NsdManager::class.java)
    private val lock = wifiManager.createMulticastLock("ServerLookup")

    private val _services = MutableStateFlow<Set<ServiceInfo>>(emptySet())
    val services = _services.asStateFlow()

    val servers = services.map { infoSet ->
        infoSet
            .filter { info ->
                "Hygrometer" in info.serviceName
            }
            .mapNotNull {
                it.txtRecords["info"]
            }
            .map {
                val raw = Base64.decode(it, Base64.DEFAULT).toString(Charsets.UTF_8)
                json.decodeFromString<Server>(raw)
            }
    }

    private val _state = MutableStateFlow(State.IDLE)
    val state = _state.asStateFlow()

    suspend fun search() = suspendCancellableCoroutine<Unit> { cont ->
        lock.acquire()
        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, this)
        cont.invokeOnCancellation {
            nsdManager.stopServiceDiscovery(this)
            lock.release()
        }
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        _state.value = when (errorCode) {
            NsdManager.FAILURE_INTERNAL_ERROR -> State.ERROR_INTERNAL
            NsdManager.FAILURE_ALREADY_ACTIVE -> State.ERROR_ALREADY_ACTIVE
            NsdManager.FAILURE_BAD_PARAMETERS -> State.ERROR_BAD_PARAMETERS
            NsdManager.FAILURE_OPERATION_NOT_RUNNING -> State.ERROR_OP_NOT_RUNNING
            NsdManager.FAILURE_MAX_LIMIT -> State.ERROR_MAX_LIMIT
            else -> State.ERROR_UNKNOWN
        }
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

    override fun onDiscoveryStarted(serviceType: String) {
        _state.value = State.STARTED
    }

    override fun onDiscoveryStopped(serviceType: String) {
        _state.value = State.IDLE
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        @Suppress("DEPRECATION")
        nsdManager.resolveService(
            serviceInfo,
            object : ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val info = ServiceInfo(serviceInfo)
                    // Have to remove it before we can add it... derp
                    _services.update { (it - info) + info }
                }
            }
        )
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        _services.update { it - ServiceInfo(serviceInfo) }
    }

    data class ServiceInfo(
        val serviceName: String,
        val port: Int,
        val hostAddress: String,
        val txtRecords: Map<String, String>,
    ) {
        constructor(serviceInfo: NsdServiceInfo) : this(
            serviceName = serviceInfo.serviceName,
            port = serviceInfo.port,
            hostAddress = serviceInfo.hostAddresses.firstOrNull()?.hostAddress
                //.find { it is Inet4Address }
                //?.hostAddress ?: "unknown",
                ?: "unknown",
            serviceInfo.attributes.mapValues { it.value.toString(Charsets.UTF_8) }
        )

        override fun equals(other: Any?): Boolean {
            return other is ServiceInfo && other.serviceName == serviceName
        }

        override fun hashCode(): Int = serviceName.hashCode()
    }

    enum class State(@StringRes val message: Int) {
        IDLE(R.string.server_lookup_idle),
        STARTED(R.string.server_lookup_started),
        ERROR_ALREADY_ACTIVE(R.string.server_lookup_already_active),
        ERROR_INTERNAL(R.string.server_lookup_internal_error),
        ERROR_BAD_PARAMETERS(R.string.server_lookup_bad_parameters),
        ERROR_OP_NOT_RUNNING(R.string.server_lookup_op_not_running),
        ERROR_MAX_LIMIT(R.string.server_lookup_max_limit),
        ERROR_UNKNOWN(R.string.server_lookup_error_unknown)
    }
}
