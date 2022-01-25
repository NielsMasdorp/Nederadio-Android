package com.nielsmasdorp.sleeply.data.stream

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.PICTURE_TYPE_OTHER
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerControlView
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.service.StreamService
import com.nielsmasdorp.sleeply.domain.settings.SetLastPlayedIndex
import com.nielsmasdorp.sleeply.domain.stream.*
import com.nielsmasdorp.sleeply.service.StreamService.Companion.START_TIMER_COMMAND
import com.nielsmasdorp.sleeply.service.StreamService.Companion.START_TIMER_COMMAND_VALUE_KEY
import com.nielsmasdorp.sleeply.service.StreamService.Companion.TIMER_UPDATED_COMMAND
import com.nielsmasdorp.sleeply.service.StreamService.Companion.TIMER_UPDATED_COMMAND_VALUE_KEY
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnsafeOptInUsageError")
class AndroidStreamManager(
    private val application: Application,
    private val setLastPlayedIndex: SetLastPlayedIndex
) : StreamManager, MediaController.Listener, Player.Listener {

    private val serviceJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var streams: List<Stream>

    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override val stateFlow: MutableStateFlow<Stream?> = MutableStateFlow(null)

    override val sleepTimerFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    override val errorFlow: MutableStateFlow<StreamingError> =
        MutableStateFlow(StreamingError.Empty)

    override fun initialize(streams: List<Stream>, startIndex: Int, controls: PlayerControls) {
        this.streams = streams
        controllerFuture = MediaController.Builder(
            application,
            SessionToken(application, ComponentName(application, StreamService::class.java))
        ).setListener(this).buildAsync()

        controllerFuture.addListener(
            { initController(streams, startIndex, controls) },
            MoreExecutors.directExecutor()
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
        mediaItem ?: return
        managerScope.launch {
            val stream = streams.first { it.id == mediaItem.mediaId }
            managerScope.launch { setLastPlayedIndex(streams.indexOf(stream)) }
            stateFlow.value = streams.first { it.id == mediaItem.mediaId }
        }
    }

    /**
     * Called when [Player] encounters an error
     */
    override fun onPlayerError(error: PlaybackException) {
        sendError(error = application.getString(R.string.stream_error_toast))
    }

    override fun release() {
        controllerFuture.get().removeListener(this)
        requireController().removeListener(this)
        MediaController.releaseFuture(controllerFuture)
    }

    override fun streamPicked(index: Int) = requireController().seekTo(index, 0L)

    override fun sleepTimerSet(ms: Long) {
        if (ms > 0 && !requireController().isPlaying) {
            sendError(error = application.getString(R.string.sleep_timer_not_allowed))
        } else {
            requireController().sendCustomCommand(
                SessionCommand(
                    START_TIMER_COMMAND,
                    Bundle().apply { putLong(START_TIMER_COMMAND_VALUE_KEY, ms) }),
                Bundle()
            )
        }
    }

    private fun initController(streams: List<Stream>, startIndex: Int, controls: PlayerControls) {
        controls.view().player = requireController()
        requireController().addListener(this)
        if (requireController().mediaItemCount == 0) {
            streams.forEach { stream ->
                requireController().addMediaItem(
                    MediaItem.Builder()
                        .setMediaId(stream.id)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setArtworkData(toByteArray(stream.smallImgRes), PICTURE_TYPE_OTHER)
                                .setTitle(stream.title)
                                .setArtist(stream.desc)
                                .build()
                        ).build()
                )
            }
            if (!requireController().isPlaying) {
                requireController().seekTo(startIndex, 0L)
            }
            // Needed because media notification is showed immediately
            // see https://github.com/androidx/media/issues/31
            requireController().prepare()
        }
    }

    private fun requireController(): MediaController = controller!!

    private fun toByteArray(@DrawableRes imageRes: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(application, imageRes)
        val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        return byteStream.toByteArray()
    }

    private fun sendError(error: String) {
        errorFlow.value = StreamingError.Filled(error = error)
    }

    private fun PlayerControls.view(): PlayerControlView = this as PlayerControlView
}