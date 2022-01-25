package com.nielsmasdorp.sleeply.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.session.SessionCommand.*
import androidx.media3.session.SessionResult.RESULT_SUCCESS
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.sleeply.domain.stream.GetAllStreams
import com.nielsmasdorp.sleeply.ui.stream.StreamActivity
import com.nielsmasdorp.sleeply.util.callPrivateFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Service responsible for hosting the [MediaSession]
 * Handles media notification and switching from foreground to background service whenever appropriate
 */
@SuppressLint("UnsafeOptInUsageError")
class StreamService : MediaSessionService(), Player.Listener, MediaSession.SessionCallback {

    private val getAllStreams: GetAllStreams by inject()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var urlsMap: Map<String, String>

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            val streams = getAllStreams()
            urlsMap = streams.map { it.id to it.url }.toMap() // cache urls
            initialize()
        }
    }

    override fun onDestroy() {
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
     * Called when [Player] events happen
     */
    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) &&
            !events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) &&
            !player.isPlaying
        ) {
            // If playing is stopped -> stop the timer
            stopSleepTimer()
        }
    }

    /**
     * Called when a new [MediaController] connects to the current [MediaSession]
     * Used to add sleep timer commands to the current [MediaSession]
     */
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val commands = SessionCommands.Builder()
            .add(SessionCommand(START_TIMER_COMMAND, Bundle()))
            .add(COMMAND_CODE_SESSION_SET_MEDIA_URI)
            .build()
        val playerCommands = Player.Commands.Builder().addAllCommands().build()
        session.setAvailableCommands(
            controller,
            commands,
            playerCommands
        )
        return MediaSession.ConnectionResult.accept(commands, playerCommands)
    }

    /**
     * Called when [MediaController] uses [MediaController.sendCustomCommand]
     * Used to notify this session that a sleep timer should be started
     */
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return if (customCommand.customAction == START_TIMER_COMMAND) {
            val ms = customCommand.customExtras.getLong(START_TIMER_COMMAND_VALUE_KEY)
            if (ms > 0) startSleepTimer(ms) else stopSleepTimer()
            Futures.immediateFuture(SessionResult(RESULT_SUCCESS))
        } else {
            super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun initialize() {
        player = ExoPlayer.Builder(applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(), true // Automatic requesting and dropping audio focus
            )
            .setHandleAudioBecomingNoisy(true) // Handle headphones disconnect
            .setWakeMode(C.WAKE_MODE_NETWORK) // Wake+WiFi lock while playing
            .build().apply { addListener(this@StreamService) }

        val intent = Intent(this, StreamActivity::class.java)
        val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            immutableFlag or FLAG_UPDATE_CURRENT
        )

        val builder = MediaSession.Builder(applicationContext, player)
            .setSessionActivity(pendingIntent)
            .setSessionCallback(this)

        // Every day we stray further from the light
        // Using reflection since relevant fix has not been released
        // https://github.com/androidx/media/commit/2a62a5ee302d694eb9c7099024d13607f6143830
        builder.callPrivateFunc("setMediaItemFiller", CustomMediaItemFiller(streamUrls = urlsMap))
        mediaSession = builder.build()
    }

    private fun startSleepTimer(ms: Long) {
        stopSleepTimer()
        countDownTimer = object : CountDownTimer(ms, SLEEP_TIMER_INTERVAL) {
            override fun onTick(msLeft: Long) {
                sendSleepTimerCommand(msLeft)
                if (msLeft < TimeUnit.SECONDS.toMillis(LOWER_VOLUME_CUTOFF.toLong())) {
                    // gently lower volume by the second
                    player.volume = (msLeft / TimeUnit.SECONDS.toMillis(1)) / LOWER_VOLUME_CUTOFF
                }
            }

            override fun onFinish() {
                sendSleepTimerCommand(msLeft = 0L)
                player.pause()
            }
        }.start()
    }

    private fun sendSleepTimerCommand(msLeft: Long) {
        mediaSession.connectedControllers.firstOrNull()?.let { controller ->
            mediaSession.sendCustomCommand(
                controller,
                SessionCommand(
                    TIMER_UPDATED_COMMAND,
                    Bundle().apply { putLong(TIMER_UPDATED_COMMAND_VALUE_KEY, msLeft) }),
                Bundle()
            )
        }
    }

    private fun stopSleepTimer() {
        sendSleepTimerCommand(msLeft = 0L)
        countDownTimer?.cancel()
        countDownTimer = null
        player.volume = MAX_VOLUME
    }

    class CustomMediaItemFiller(private val streamUrls: Map<String, String>) :
        MediaSession.MediaItemFiller {
        override fun fillInLocalConfiguration(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItem: MediaItem
        ): MediaItem = mediaItem
            .buildUpon()
            .setUri(Uri.parse(streamUrls[mediaItem.mediaId]))
            .build()
    }

    companion object {

        const val START_TIMER_COMMAND = "start_timer"
        const val START_TIMER_COMMAND_VALUE_KEY = "start_timer_value"
        const val TIMER_UPDATED_COMMAND = "timer_updated"
        const val TIMER_UPDATED_COMMAND_VALUE_KEY = "timer_updated_value"
        private const val SLEEP_TIMER_INTERVAL = 1000L
        private const val LOWER_VOLUME_CUTOFF = 30f
        private const val MAX_VOLUME = 1.0f
    }
}