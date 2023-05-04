package com.nielsmasdorp.nederadio.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import com.nielsmasdorp.nederadio.domain.settings.AddToFavorites
import com.nielsmasdorp.nederadio.domain.settings.RemoveFromFavorites
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.ui.components.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class AppViewModel(
    getAllStreams: GetAllStreams,
    getActiveStream: GetActiveStream,
    private val addToFavorites: AddToFavorites,
    private val removeFromFavorites: RemoveFromFavorites,
    private val updateStreams: UpdateStreams,
    private val streamManager: StreamManager,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    val streams: Flow<Streams> = getAllStreams.streams // TODO drop livedata

    val activeStream: Flow<ActiveStream> = getActiveStream.stream

    val favorites: Flow<List<Stream>> = streams
        .map { streams ->
            if (streams is Streams.Success) {
                streams.streams.filter { stream -> stream.isFavorite }
            } else {
                emptyList()
            }
        }

    val sleepTimer: Flow<String> = streamManager.sleepTimerFlow
        .map { formatTimer(it) }
        .filterNotNull()

    var errorState by mutableStateOf(ErrorState())
        private set

    private val _showAboutApp = MutableStateFlow(false)
    val showAboutApp: StateFlow<Boolean> = _showAboutApp

    private val _showSleepTimer = MutableStateFlow(false)
    val showSleepTimer: StateFlow<Boolean> = _showSleepTimer

    init {
        viewModelScope.launch {
            streamManager.errorFlow
                .filter { it is StreamingError.Filled }
                .collect { error ->
                    val errorText = (error as StreamingError.Filled).error
                    errorState = errorState.copy(
                        error = Event(errorText) {
                            errorState = errorState.copy(error = null)
                        }
                    )
                }
        }
    }

    fun onStarted(controls: List<PlayerControls<*>>) = streamManager.initialize(controls = controls)

    fun onStopped() = streamManager.release()

    fun onStreamPicked(id: String) = streamManager.streamPicked(id)

    fun onTimerPicked() {
        _showSleepTimer.value = true
    }

    fun setSleepTimer(index: Int) {
        streamManager.sleepTimerSet(calculateMs(index))
        _showSleepTimer.value = false
    }

    fun onAboutPicked() {
        _showAboutApp.value = true
    }

    fun onAlertDismissed() {
        _showSleepTimer.value = false
        _showAboutApp.value = false
    }

    fun onStreamFavoriteChanged(id: String, isFavorite: Boolean) {
        viewModelScope.launch(dispatchers.main()) {
            if (isFavorite) {
                addToFavorites(id)
            } else {
                removeFromFavorites(id)
            }
        }
    }

    fun onRetryStreams() {
        viewModelScope.launch {
            updateStreams()
        }
    }

    @Suppress("MagicNumber")
    private fun calculateMs(option: Int): Long {
        return when (option) {
            0 -> 0
            1 -> TimeUnit.MINUTES.toMillis(10)
            2 -> TimeUnit.MINUTES.toMillis(15)
            3 -> TimeUnit.MINUTES.toMillis(20)
            4 -> TimeUnit.MINUTES.toMillis(30)
            5 -> TimeUnit.MINUTES.toMillis(40)
            6 -> TimeUnit.MINUTES.toMillis(50)
            7 -> TimeUnit.HOURS.toMillis(1)
            8 -> TimeUnit.HOURS.toMillis(2)
            9 -> TimeUnit.HOURS.toMillis(3)
            else -> 0
        }
    }

    private fun formatTimer(timeLeft: Long?): String {
        if (timeLeft == null || timeLeft == 0L) {
            return ""
        }
        return if (timeLeft > TimeUnit.HOURS.toMillis(1)) {
            String.format(
                Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeLeft),
                TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft))
            )
        } else {
            String.format(
                Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft))
            )
        }
    }

    data class ErrorState(val error: Event<String>? = null)
}
