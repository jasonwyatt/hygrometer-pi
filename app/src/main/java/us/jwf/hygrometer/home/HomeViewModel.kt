package us.jwf.hygrometer.home

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import us.jwf.hygrometer.App
import us.jwf.hygrometer.R
import us.jwf.hygrometer.api.ServerLookup
import us.jwf.hygrometer.common.Server

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val app = application as App

    private val dataState = MutableStateFlow<HomeViewState>(HomeViewState.Loading(R.string.home_loading))
    val viewState: StateFlow<HomeViewState> = dataState.asStateFlow()

    init {
        viewModelScope.launch {
            app.serverLookup.search()
        }
        viewModelScope.launch {
            app.serverLookup.servers.combine(app.serverLookup.state) { servers, state ->
                if (servers.isNotEmpty()) {
                    HomeViewState.Loaded(servers)
                } else {
                    when (state) {
                        ServerLookup.State.IDLE -> HomeViewState.Loading(state.message)
                        ServerLookup.State.STARTED -> HomeViewState.Loading(state.message)
                        else -> HomeViewState.Error(state.message)
                    }
                }
            }.collectLatest {
                dataState.value = it
            }
        }
    }
}


sealed class HomeViewState {
    data class Loading(@StringRes val message: Int) : HomeViewState()

    data class Error(@StringRes val message: Int) : HomeViewState()

    data class Loaded(val services: List<Server>) : HomeViewState()
}
