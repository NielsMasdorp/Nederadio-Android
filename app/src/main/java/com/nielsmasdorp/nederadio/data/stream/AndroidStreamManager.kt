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
import com.nielsmasdorp.nederadio.service.StreamService
import com.nielsmasdorp.nederadio.domain.settings.SetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.service.StreamService.Companion.START_STREAM_COMMAND
import com.nielsmasdorp.nederadio.service.StreamService.Companion.START_TIMER_COMMAND
import com.nielsmasdorp.nederadio.service.StreamService.Companion.START_TIMER_COMMAND_VALUE_KEY
import com.nielsmasdorp.nederadio.service.StreamService.Companion.TIMER_UPDATED_COMMAND
import com.nielsmasdorp.nederadio.service.StreamService.Companion.TIMER_UPDATED_COMMAND_VALUE_KEY
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
) : StreamManager, MediaController.Listener, Player.Listener {

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
     * Used to notify this [MediaController] that the sleep timer time left is changing
     */
    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return if (command.customAction == TIMER_UPDATED_COMMAND) {
            sleepTimerFlow.value = command.customExtras.getLong(TIMER_UPDATED_COMMAND_VALUE_KEY)
            Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        } else {
            super.onCustomCommand(controller, command, args)
        }
    }

    /**
     * Called when the current [Stream] changes
     * @param mediaItem the new media item
     * @param reason the reason for the change
     */
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        // TODO this doesn't work when [CastPlayer] is active
        // https://github.com/androidx/media/issues/25
        mediaItem ?: return
        val id = if (mediaItem == MediaItem.EMPTY) "0" else mediaItem.mediaId
        updateCurrentStream(
            id = id,
            currentTrack = null
        )
    }

    /**
     * Called when new track happens on the stream
     */
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        // TODO this doesn't work when [CastPlayer] is active
        // https://github.com/androidx/media/issues/26
        if (cache.currentStream !is CurrentStream.Filled) return
        val current = cache.currentStream as CurrentStream.Filled

        val new = current.copy(
            stream = current.stream.copy(track = mediaMetadata.title?.toString())
        )
        sendCurrentStream(new)
    }

    /**
     * Called when [Player] encounters an error
     */
    override fun onPlayerError(error: PlaybackException) {
        sendError(error = context.getString(R.string.stream_error_toast))
    }

    override fun release() {
        controller?.removeListener(this)
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
            requireController().sendCustomCommand(
                SessionCommand(
                    START_TIMER_COMMAND,
                    Bundle().apply { putLong(START_TIMER_COMMAND_VALUE_KEY, ms) }),
                Bundle()
            )
        }
    }

    private fun initController(controls: List<PlayerControls<*>>) {
        controls.forEach { it.view().player = requireController() }
        requireController().addListener(this)
    }

    private fun initStream(stream: Stream, force: Boolean) {
        if (requireController().mediaItemCount == 0 || force) {
            sendStreamToSession(stream = stream, start = force)
        } else if (requireController().mediaItemCount > 0) {
            updateCurrentStream(
                id = requireController().currentMediaItem!!.mediaId,
                // track might have changed while app was in the background
                currentTrack = requireController().mediaMetadata.title?.toString()
            )
        }
    }

    private fun sendStreamToSession(stream: Stream, start: Boolean) {
        // Delegate starting the stream to the [MediaSession] and not the controller
        // see https://github.com/androidx/media/issues/8
        // and https://stackoverflow.com/questions/70096715/adding-mediaitem-when-using-the-media3-library-caused-an-error
        MediaItem.Builder()
            .setMediaId(stream.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(stream.title)
                    .setArtworkData(
                        stream.imageBytes,
                        MediaMetadata.PICTURE_TYPE_OTHER
                    )
                    .setMediaUri(Uri.parse(stream.url))
                    .build()
            )
            .build().also {
                requireController().sendCustomCommand(
                    SessionCommand(
                        START_STREAM_COMMAND,
                        it.toBundle()
                    ), Bundle()
                ).addListener({
                    if (start) requireController().play()
                }, MoreExecutors.directExecutor())
            }
    }

    private fun sendError(error: String) {
        errorFlow.value = StreamingError.Filled(error = error)
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