package com.nielsmasdorp.nederadio.data.stream

import android.content.*
import android.os.Bundle
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.playback.StreamService
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.PLAYER_STREAM_ERROR_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.START_TIMER_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.START_TIMER_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TIMER_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TIMER_UPDATED_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TRACK_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TRACK_UPDATED_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary
import com.nielsmasdorp.nederadio.util.sendCommandToService
import com.nielsmasdorp.nederadio.util.toMediaItem
import com.nielsmasdorp.nederadio.util.view
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@UnstableApi
class AndroidStreamManager(
    private val context: Context,
    private val setActiveStream: SetActiveStream,
    private val setStreamTrack: SetStreamTrack,
    private val streamLibrary: StreamLibrary,
) : StreamManager, MediaController.Listener {

    private var isInitialized: Boolean = false

    private val serviceJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override val sleepTimerFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    override val errorFlow: MutableStateFlow<StreamingError> =
        MutableStateFlow(StreamingError.Empty)

    override fun initialize(
        controls: List<PlayerControls<*>>
    ) {
        controllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, StreamService::class.java))
        ).setListener(this).buildAsync()

        val executor = MoreExecutors.directExecutor()
        controllerFuture.addListener({ setupController(controls = controls) }, executor)
    }

    private fun setupController(controls: List<PlayerControls<*>>) {
        isInitialized = true
        initController(controls)
        managerScope.launch {
            streamLibrary.streams
                .map(::toStreamsResult)
                .distinctUntilChanged()
                .collect { result ->
                    withContext(Dispatchers.Main) {
                        val (streams, currentStreamIndex) = result
                        val controller = requireController()
                        if (currentStreamIndex != -1) {
                            val currentStream = streams[currentStreamIndex]
                            val currentStreamInUse = controller.currentMediaItem
                            if (currentStreamInUse?.mediaId != currentStream.mediaId) {
                                controller.setMediaItems(streams, currentStreamIndex, 0L)
                            }
                        }
                    }
                }
        }
    }

    /**
     * Called when [MediaSession] uses [MediaSession.sendCustomCommand]
     */
    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return when (command.customAction) {
            TIMER_UPDATED_COMMAND -> {
                sleepTimerFlow.value = command.customExtras.getLong(TIMER_UPDATED_COMMAND_VALUE_KEY)
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            TRACK_UPDATED_COMMAND -> {
                managerScope.launch {
                    setStreamTrack(
                        track = command.customExtras.getString(TRACK_UPDATED_COMMAND_VALUE_KEY)!!
                    )
                }
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            PLAYER_STREAM_ERROR_COMMAND -> {
                onStreamError()
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            else -> super.onCustomCommand(controller, command, args)
        }
    }

    override fun release() {
        MediaController.releaseFuture(controllerFuture)
        isInitialized = false
    }

    override fun streamPicked(id: String) {
        managerScope.launch { setActiveStream(id = id) }
    }

    override fun sleepTimerSet(ms: Long) {
        if (ms > 0 && !requireController().isPlaying) {
            sendError(error = context.getString(R.string.sleep_timer_not_allowed))
        } else {
            requireController().sendCommandToService(
                key = START_TIMER_COMMAND,
                value = Bundle().apply { putLong(START_TIMER_COMMAND_VALUE_KEY, ms) }
            )
        }
    }

    private fun initController(controls: List<PlayerControls<*>>) {
        controls.forEach { it.view().player = requireController() }
        sleepTimerFlow.value = null // sleep timer might have completed in background
    }

    /**
     * Called when [Player] encounters an streaming error
     */
    private fun onStreamError() {
        sendError(error = context.getString(R.string.stream_error_toast))
    }

    /**
     * Send error to subscribers
     * @param error the error to send
     * @param persistent whether the error should not be followed by an empty error
     * i.e. it is not a one shot error
     */
    private fun sendError(error: String, persistent: Boolean = false) {
        errorFlow.value = StreamingError.Filled(error = error)
        if (!persistent) errorFlow.value = StreamingError.Empty
    }

    private fun toStreamsResult(streams: List<Stream>): Pair<List<MediaItem>, Int> {
        return Pair(
            streams.map { stream -> stream.toMediaItem() },
            streams.indexOfFirst { stream -> stream.isActive }
        )
    }

    private fun requireController(): MediaController = controller!!
}
