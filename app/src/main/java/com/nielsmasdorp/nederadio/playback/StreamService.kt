package com.nielsmasdorp.nederadio.playback

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.Player.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.session.SessionCommand.*
import androidx.media3.session.SessionResult.RESULT_SUCCESS
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.ui.NederadioActivity
import com.nielsmasdorp.nederadio.util.sendCommandToController
import java.util.concurrent.TimeUnit

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Service responsible for hosting the [MediaSession]
 * Handles media notification and switching from foreground to background service whenever appropriate
 * Also switches from local playback to cast playback whenever appropriate
 */
@UnstableApi
class StreamService : MediaSessionService(),
    Listener, MediaSession.SessionCallback, SessionAvailabilityListener {

    private lateinit var player: CompositePlayer
    private lateinit var mediaSession: MediaSession

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onDestroy() {
        player.removeListener(this)
        player.release()
        mediaSession.release()
        stopSleepTimer()
        super.onDestroy()
    }

    /**
     * Connect [MediaSession] to this [MediaSessionService]
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    /**
     * Casting has started, switch to [CastPlayer]
     */
    override fun onCastSessionAvailable() = player.switchToCast()

    /**
     * Casting has been stopped, switch to [ExoPlayer]
     */
    override fun onCastSessionUnavailable() = player.switchToLocal()

    /**
     * [MediaItem] has been updated
     */
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItem ?: return
        mediaSession.sendCommandToController(
            key = MEDIA_ITEM_UPDATED_COMMAND,
            value = mediaItem.toBundle()
        )
    }

    /**
     * Track has been updates
     */
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        mediaSession.sendCommandToController(
            key = TRACK_UPDATED_COMMAND,
            value = Bundle().apply {
                putString(
                    TRACK_UPDATED_COMMAND_VALUE_KEY, mediaMetadata.title?.toString()
                )
            }
        )
    }

    /**
     * Called when [Player] encounters an error
     */
    override fun onPlayerError(error: PlaybackException) {
        mediaSession.sendCommandToController(key = PLAYER_STREAM_ERROR_COMMAND)
    }

    /**
     * Called when [Player] events happen
     */
    override fun onEvents(player: Player, events: Events) {
        if (events.contains(EVENT_IS_PLAYING_CHANGED) &&
            !events.contains(EVENT_MEDIA_ITEM_TRANSITION) &&
            !player.isPlaying
        ) {
            // If playing is stopped -> stop the timer
            stopSleepTimer()
        }
    }

    /**
     * Called when a new [MediaController] connects to the current [MediaSession]
     * Used to add sleep timer and setting url for current
     * stream commands to the current [MediaSession]
     */
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val commands = SessionCommands.Builder()
            .add(SessionCommand(START_STREAM_COMMAND, Bundle()))
            .add(SessionCommand(START_TIMER_COMMAND, Bundle()))
            .add(COMMAND_CODE_SESSION_SET_MEDIA_URI)
            .build()
        val playerCommands = Commands.Builder().addAllCommands().build()
        session.setAvailableCommands(
            controller,
            commands,
            playerCommands
        )
        return MediaSession.ConnectionResult.accept(commands, playerCommands)
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        player.notifyCurrentMediaItem(listener = this)
    }

    /**
     * Called when [MediaController] uses [MediaController.sendCustomCommand]
     * Used to notify this session that a sleep timer should be started
     * or a new stream is about to be started with a url
     */
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return when (customCommand.customAction) {
            START_TIMER_COMMAND -> {
                val ms = customCommand.customExtras.getLong(START_TIMER_COMMAND_VALUE_KEY)
                if (ms > 0) startSleepTimer(ms) else stopSleepTimer()
                Futures.immediateFuture(SessionResult(RESULT_SUCCESS))
            }
            START_STREAM_COMMAND -> {
                startStream(mediaItem = MediaItem.CREATOR.fromBundle(customCommand.customExtras))
                Futures.immediateFuture(SessionResult(RESULT_SUCCESS))
            }
            else -> {
                super.onCustomCommand(session, controller, customCommand, args)
            }
        }
    }

    private fun startStream(mediaItem: MediaItem) {
        // The [MediaController] delegates creating [MediaItem]s to the service
        // This is needed because for some reason the uri gets stripped when a [MediaItem]
        // is created from the [MediaController]
        // see https://github.com/androidx/media/issues/8
        // and https://stackoverflow.com/questions/70096715/adding-mediaitem-when-using-the-media3-library-caused-an-error
        val item = mediaItem.buildUpon()
            .setUri(mediaItem.mediaMetadata.mediaUri)
            .setMimeType(MimeTypes.AUDIO_UNKNOWN)
            .build()
        with(player) {
            setMediaItem(item)
            prepare()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun initialize() {
        val localPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(), true // Automatic requesting and dropping audio focus
            )
            .setHandleAudioBecomingNoisy(true) // Handle headphones disconnect
            .setWakeMode(C.WAKE_MODE_NETWORK) // Wake+WiFi lock while playing
            .build()

        val intent = Intent(this, NederadioActivity::class.java)
        val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            immutableFlag or FLAG_UPDATE_CURRENT
        )

        val castPlayer = CastPlayer(
            CastContext.getSharedInstance(this),
            StreamMediaItemConverter(
                staticTitle = applicationContext.getString(R.string.stream_subtitle_streaming)
            )
        )
        player = CompositePlayer(
            staticTitle = applicationContext.getString(R.string.stream_title_casting),
            localPlayer = localPlayer,
            castPlayer = castPlayer
        ).apply {
            setSessionAvailabilityListener(this@StreamService)
            addListener(this@StreamService)
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setSessionCallback(this)
            .build()
    }

    private fun startSleepTimer(ms: Long) {
        stopSleepTimer()
        countDownTimer = object : CountDownTimer(ms, SLEEP_TIMER_INTERVAL) {
            override fun onTick(msLeft: Long) {
                sendSleepTimerCommand(msLeft)
                if (msLeft < TimeUnit.SECONDS.toMillis(LOWER_VOLUME_CUTOFF.toLong())) {
                    // gently lower volume by the second
                    player.volume =
                        (msLeft / TimeUnit.SECONDS.toMillis(1)) / LOWER_VOLUME_CUTOFF
                }
            }

            override fun onFinish() {
                sendSleepTimerCommand(msLeft = 0L)
                player.pause()
            }
        }.start()
    }

    private fun sendSleepTimerCommand(msLeft: Long) {
        mediaSession.sendCommandToController(
            key = TIMER_UPDATED_COMMAND,
            value = Bundle().apply { putLong(TIMER_UPDATED_COMMAND_VALUE_KEY, msLeft) }
        )
    }

    private fun stopSleepTimer() {
        sendSleepTimerCommand(msLeft = 0L)
        countDownTimer?.cancel()
        countDownTimer = null
        player.volume = MAX_VOLUME
    }

    companion object {

        const val START_TIMER_COMMAND = "start_timer"
        const val START_TIMER_COMMAND_VALUE_KEY = "start_timer_value"
        const val START_STREAM_COMMAND = "start_stream"
        const val TIMER_UPDATED_COMMAND = "timer_updated"
        const val TIMER_UPDATED_COMMAND_VALUE_KEY = "timer_updated_value"
        const val MEDIA_ITEM_UPDATED_COMMAND = "media_item_updated"
        const val TRACK_UPDATED_COMMAND = "track_updated"
        const val TRACK_UPDATED_COMMAND_VALUE_KEY = "track_updated_value"
        const val PLAYER_STREAM_ERROR_COMMAND = "player_error"

        private const val SLEEP_TIMER_INTERVAL = 1000L
        private const val LOWER_VOLUME_CUTOFF = 30f
        private const val MAX_VOLUME = 1.0f
    }
}