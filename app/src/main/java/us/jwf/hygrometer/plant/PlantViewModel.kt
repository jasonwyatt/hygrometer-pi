package us.jwf.hygrometer.plant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import us.jwf.hygrometer.App
import us.jwf.hygrometer.api.ServerRepository
import us.jwf.hygrometer.common.ConfigFile
import us.jwf.hygrometer.common.Reading
import us.jwf.hygrometer.common.Server
import kotlin.time.Duration.Companion.seconds

class PlantViewModel(
    private val args: Server,
    app: App,
) : ViewModel() {
    private val serverRepository: ServerRepository = app.serverRepository

    private val dataState = MutableStateFlow<PlantViewState>(PlantViewState.Loading(args))
    val viewState: StateFlow<PlantViewState> = dataState.asStateFlow()

    init {
        viewModelScope.launch {
            serverRepository.getConfig(args, 10.seconds)
                .collectLatest { config ->
                    dataState.update {
                        when (it) {
                            is PlantViewState.Loaded -> it.copy(configFile = config)
                            is PlantViewState.Loading -> PlantViewState.Loaded(it.server, config)
                        }
                    }
                }
        }
    }

    fun takeReading() {
        viewModelScope.launch {
            dataState.updateLoaded {
                copy(readingState = ReadingState.Taking)
            }
            val result = serverRepository.takeReading(args)
            result.fold(
                onSuccess = { reading ->
                    dataState.updateLoaded {
                        copy(readingState = ReadingState.Success(reading))
                    }
                },
                onFailure = { e ->
                    Log.e("PlantViewModel", "Error taking reading", e)
                    dataState.updateLoaded {
                        copy(readingState = ReadingState.Error)
                    }
                }
            )
        }
    }

    private inline fun MutableStateFlow<PlantViewState>.updateLoaded(
        block: PlantViewState.Loaded.() -> PlantViewState,
    ) {
        update {
            when (it) {
                is PlantViewState.Loaded -> it.block()
                is PlantViewState.Loading -> it
            }
        }
    }

    class Factory(private val args: Server) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T = PlantViewModel(args, checkNotNull(extras[APPLICATION_KEY]) as App) as T
    }
}

sealed interface PlantViewState {
    val server: Server

    data class Loading(override val server: Server) : PlantViewState

    data class Loaded(
        override val server: Server,
        val configFile: ConfigFile,
        val readingState: ReadingState = ReadingState.NotTaken
    ) : PlantViewState
}

sealed interface ReadingState {
    data object NotTaken : ReadingState
    data object Taking : ReadingState

    data object Error : ReadingState

    data class Success(val reading: Reading) : ReadingState
}