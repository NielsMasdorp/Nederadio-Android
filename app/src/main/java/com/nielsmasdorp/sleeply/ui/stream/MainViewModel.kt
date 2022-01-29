package com.nielsmasdorp.sleeply.ui.stream

import androidx.lifecycle.*
import com.nielsmasdorp.sleeply.domain.DefaultDispatcherProvider
import com.nielsmasdorp.sleeply.domain.DispatcherProvider
import com.nielsmasdorp.sleeply.domain.settings.GetLastPlayedIndex
import com.nielsmasdorp.sleeply.domain.stream.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class MainViewModel(
    private val getAllStreams: GetAllStreams,
    private val streamManager: StreamManager,
    private val getLastPlayedIndex: GetLastPlayedIndex,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    val viewData: LiveData<Stream> = streamManager.stateFlow
        .filterNotNull()
        .asLiveData()

    val sleepTimer: LiveData<String> = streamManager.sleepTimerFlow
        .map { formatTimer(it) }
        .filterNotNull()
        .asLiveData()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private val errorChannel = Channel<StreamingError>(Channel.BUFFERED)
    val errorFlow = errorChannel.receiveAsFlow()

    fun onStarted(controls: PlayerControls<*>) {
        viewModelScope.launch(dispatchers.main()) {
            val streams = getAllStreams()
            streamManager.initialize(
                streams = streams,
                startIndex = getLastPlayedIndex(),
                controls = controls
            )
        }
        viewModelScope.launch(dispatchers.main()) {
            streamManager.errorFlow
                .filterNotNull()
                .collect { errorChannel.send(it) }
        }
    }

    fun onStopped() = streamManager.release()

    fun onStreamPicked(index: Int) = streamManager.streamPicked(index)

    fun onPickStreams() {
        viewModelScope.launch(dispatchers.main()) {
            val streams = getAllStreams()
            eventChannel.send(Event.ShowStreams(streams))
        }
    }

    fun onTimerPicked() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.ShowTimer) }
    }

    fun setSleepTimer(index: Int) {
        streamManager.sleepTimerSet(calculateMs(index))
    }

    fun onAboutPicked() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.ShowAbout) }
    }

    fun onAboutDismissed() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.Empty) }
    }

    fun onTimerDismissed() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.Empty) }
    }

    fun onStreamsDismissed() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.Empty) }
    }

    fun onEmailDeveloperPicked() {
        viewModelScope.launch(dispatchers.main()) { eventChannel.send(Event.EmailDeveloper) }
    }

    fun onErrorShown() {
        viewModelScope.launch(dispatchers.main()) { errorChannel.send(StreamingError.Empty) }
    }

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

    sealed class Event {

        object Empty : Event()
        data class ShowStreams(val streams: List<Stream>) : Event()
        object ShowTimer : Event()
        object ShowAbout : Event()
        object EmailDeveloper : Event()
    }

    companion object {
        const val EMPTY_ERROR = ""
    }
}