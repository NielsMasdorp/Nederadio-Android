package com.nielsmasdorp.nederadio.data.stream

import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerControlView
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.playback.StreamService
import com.nielsmasdorp.nederadio.domain.settings.SetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.MEDIA_ITEM_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.PLAYER_STREAM_ERROR_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.START_STREAM_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.START_TIMER_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.START_TIMER_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TIMER_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TIMER_UPDATED_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TRACK_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.playback.StreamService.Companion.TRACK_UPDATED_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.util.sendCommandToService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@UnstableApi
class AndroidStreamManager(
    private val context: Context,
    private val getLastPlayedId: GetLastPlayedId,
    private val setLastPlayedId: SetLastPlayedId,
    private val getAllStreams: GetAllStreams,
) : StreamManager, MediaController.Listener {

    private var isInitialized: Boolean = false

    private val serviceJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override val currentStreamFlow: MutableStateFlow<CurrentStream> =
        MutableStateFlow(CurrentStream.Unknown)

    override val sleepTimerFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    override val errorFlow: MutableStateFlow<StreamingError> =
        MutableStateFlow(StreamingError.Empty)

    private var cache: StreamsCache = StreamsCache()

    init {
        managerScope.launch {
            getAllStreams.streams.collect { streams ->
                if (streams is CurrentStreams.Success) {
                    cache = cache.copy(all = streams.streams)
                    if (isInitialized) {
                        handleStreamsUpdate(streams = streams.streams)
                    }
                }
            }
        }
    }

    override fun initialize(
        controls: List<PlayerControls<*>>
    ) {
        controllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, StreamService::class.java))
        ).setListener(this).buildAsync()

        controllerFuture.addListener(
            {
                isInitialized = true
                initController(controls)
                managerScope.launch {
                    handleStreamsUpdate(streams = cache.all)
                }
            }, MoreExecutors.directExecutor()
        )
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
            MEDIA_ITEM_UPDATED_COMMAND -> {
                onMediaItemUpdated(MediaItem.CREATOR.fromBundle(command.customExtras))
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            TRACK_UPDATED_COMMAND -> {
                onTrackChanged(command.customExtras.getString(TRACK_UPDATED_COMMAND_VALUE_KEY))
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
        if (!cache.hasCurrentStream() ||
            (cache.hasCurrentStream() && cache.getCurrentFilledStream().stream.id != id)
        ) {
            initStream(stream = cache.findById(id), force = true)
        }
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

    private fun initStream(stream: Stream, force: Boolean) {
        if (requireController().mediaItemCount == 0 || force) {
            sendStreamToSession(stream = stream, start = force)
        } else if (requireController().mediaItemCount > 0) {
            updateCurrentStream(
                id = stream.id,
                // track might have changed while app was in the background
                currentTrack = requireController().mediaMetadata.title?.toString()
            )
        }
    }

    private fun sendStreamToSession(stream: Stream, start: Boolean) {
        // Delegate starting the stream to the [MediaSession] and not the controller
        // see https://github.com/androidx/media/issues/8
        // and https://stackoverflow.com/questions/70096715/adding-mediaitem-when-using-the-media3-library-caused-an-error
        val item = MediaItem.Builder()
            .setMediaId(stream.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(stream.title)
                    .setArtworkData(
                        stream.imageBytes,
                        MediaMetadata.PICTURE_TYPE_OTHER
                    )
                    .setArtworkUri(Uri.parse(stream.imageUrl))
                    .setMediaUri(Uri.parse(stream.url))
                    .build()
            )
            .build()

        requireController().sendCommandToService(
            key = START_STREAM_COMMAND, value = item.toBundle()
        ).addListener({
            if (start) requireController().play()
        }, MoreExecutors.directExecutor())
    }

    private fun onMediaItemUpdated(mediaItem: MediaItem) {
        updateCurrentStream(
            id = mediaItem.mediaId,
            currentTrack = mediaItem.mediaMetadata.title?.toString()
        )
    }

    private fun onTrackChanged(track: String?) {
        if (cache.currentStream !is CurrentStream.Filled) return
        val current = cache.currentStream as CurrentStream.Filled

        val new = current.copy(
            stream = current.stream.copy(track = track)
        )
        sendCurrentStream(new)
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

    private suspend fun handleStreamsUpdate(streams: List<Stream>) {
        if (streams.isEmpty()) return
        val current = if (cache.hasCurrentStream()) cache.getCurrentFilledStream() else null
        val lastPlayed = getLastPlayedId()?.let { cache.findById(it) }
        val stream = current?.stream ?: lastPlayed
        stream?.let {
            val updated = cache.findById(it.id)
            val new = it.copy(isFavorite = updated.isFavorite)
            sendCurrentStream(stream = CurrentStream.Filled(stream = new))
            withContext(Dispatchers.Main) {
                initStream(new, force = false)
            }
        } ?: sendCurrentStream(stream = CurrentStream.Empty)
    }

    private fun updateCurrentStream(id: String, currentTrack: String?) {
        managerScope.launch {
            val new = CurrentStream.Filled(
                stream = cache.findById(id = id).copy(
                    track = currentTrack
                )
            )
            setLastPlayedId(new.stream.id)
            sendCurrentStream(stream = new)
        }
    }

    private fun sendCurrentStream(stream: CurrentStream) {
        currentStreamFlow.value = stream.also {
            cache = cache.copy(currentStream = it)
        }
    }

    private fun requireController(): MediaController = controller!!

    private fun PlayerControls<*>.view(): PlayerControlView = getView() as PlayerControlView

    private data class StreamsCache(
        val currentStream: CurrentStream = CurrentStream.Unknown,
        val all: List<Stream> = emptyList()
    ) {

        fun findById(id: String): Stream = all.first { it.id == id }

        fun hasCurrentStream(): Boolean = currentStream is CurrentStream.Filled

        fun getCurrentFilledStream(): CurrentStream.Filled = currentStream as CurrentStream.Filled
    }
}