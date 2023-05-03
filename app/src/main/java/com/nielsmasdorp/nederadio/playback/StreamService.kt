package com.nielsmasdorp.nederadio.playback

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.media.utils.MediaConstants.*
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.Player.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.session.MediaConstants.*
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.SessionCommand.*
import androidx.media3.session.SessionResult.RESULT_SUCCESS
import com.google.android.gms.cast.framework.CastContext
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.SetActiveStream
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary.Companion.STATIONS_ITEM_ID
import com.nielsmasdorp.nederadio.ui.NederadioActivity
import com.nielsmasdorp.nederadio.util.connectedDeviceName
import com.nielsmasdorp.nederadio.util.moveToFront
import com.nielsmasdorp.nederadio.util.sendCommandToController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.future
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Service responsible for hosting the [MediaLibrarySession]
 * Handles media notification and switching from foreground to background service whenever appropriate
 * Also switches from local playback to cast playback and exposes the library to other components
 * such as Android Auto
 */
@UnstableApi
class StreamService : MediaLibraryService(),
    Listener, MediaLibrarySession.Callback, SessionAvailabilityListener {

    private val streamLibrary: StreamLibrary by inject()
    private val setActiveStream: SetActiveStream by inject()
    private val getLastPlayedId: GetLastPlayedId by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var localPlayer: Player
    private lateinit var castPlayer: CastPlayer
    private lateinit var player: ReplaceableForwardingPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private lateinit var castContext: CastContext

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        releaseMediaSession()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaSession()
        stopSleepTimer()
        serviceScope.cancel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return serviceScope.future {
            val isRecentRequest = params?.isRecent ?: false
            val rootItem = if (!isRecentRequest) {
                streamLibrary.browsableContent.first().rootNode.mediaItem
            } else {
                // Playback resumption
                streamLibrary.browsableContent.first().recentRootNode.mediaItem
            }
            val extras = Bundle().apply {
                putInt(
                    EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                    EXTRAS_VALUE_CONTENT_STYLE_CATEGORY_GRID_ITEM
                )
            }
            val newParams = LibraryParams.Builder()
                .setExtras(extras)
                .setRecent(isRecentRequest)
                .build()
            LibraryResult.ofItem(rootItem, newParams)
        }
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return serviceScope.future {
            val isRecentRequest = params?.isRecent ?: false
            val children = if (!isRecentRequest) {
                streamLibrary
                    .browsableContent
                    .first()
                    .getChildren(nodeId = parentId)
            } else {
                // Playback resumption
                streamLibrary
                    .browsableContent
                    .first()
                    .getRecentChildren(lastPlayedId = getLastPlayedId())
            }
            LibraryResult.ofItemList(children, params)
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return serviceScope.future {
            val item = streamLibrary.browsableContent.first().getItem(itemId = mediaId)
            LibraryResult.ofItem(item, null)
        }
    }

    /**
     * Casting has started, switch to [CastPlayer]
     */
    override fun onCastSessionAvailable() = player.setPlayer(newPlayer = castPlayer)

    /**
     * Casting has been stopped, switch to [ExoPlayer]
     */
    override fun onCastSessionUnavailable() = player.setPlayer(newPlayer = localPlayer)

    /**
     * [MediaItem] has been updated
     */
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItem ?: return
        serviceScope.launch { setActiveStream(id = mediaItem.mediaId) }
    }

    /**
     * Track has been updates
     */
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        mediaSession.sendCommandToController(
            key = TRACK_UPDATED_COMMAND,
            value = Bundle().apply {
                putString(TRACK_UPDATED_COMMAND_VALUE_KEY, getTrackInfo(mediaMetadata))
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
     * Used to add sleep timer command to the current [MediaSession]
     */
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val result = super.onConnect(session, controller)

        val sessionCommands = result.availableSessionCommands
            .buildUpon()
            .add(SessionCommand(START_TIMER_COMMAND, Bundle()))
            .build()
        val playerCommands = result.availablePlayerCommands
        session.setAvailableCommands(controller, sessionCommands, playerCommands)
        return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
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
            else -> super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        // There are a couple of issues with media3:
        // Firstly when the [MediaController] in [AndroidStreamManager] adds media items this
        // hook gets called. As a security measure content URI's get removed before the [MediaItem]s
        // reach the [MediaSession]. We have to fill these URI's again.
        //
        // Also when the [MediaController] used by Android Auto selects a new track
        // this method is called with only one item (the selected one), the rest of the queue
        // is dropped for some reason. his is a known problem,
        // See: https://github.com/androidx/media/issues/156 and https://github.com/androidx/media/issues/236
        // To solve both these problems we check the [MediaItems]s here and replace them with the
        // Known items in our repository. In the case of Android Auto we use the whole list instead
        // of having a queue with a single item.
        return serviceScope.future {
            val streams = streamLibrary
                .browsableContent
                .first()
                .getChildren(nodeId = STATIONS_ITEM_ID)
            if (mediaItems.size == 1) { // User has selected an item in Android Auto
                val item = mediaItems[0]
                // Replace single items by all items with selected item as first item
                streams
                    .toMutableList()
                    .apply { moveToFront { it.mediaId == item.mediaId } }
            } else {
                // Just use the [MediaItem] from the content library since the URI exists there
                mediaItems.map { mediaItem ->
                    streams.find { it.mediaId == mediaItem.mediaId }!!
                }.toMutableList()
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun initialize() {
        localPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
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

        castContext = CastContext.getSharedInstance(this)
        castPlayer = CastPlayer(
            castContext,
            StreamMediaItemConverter { getTrackInfo() }
        ).apply {
            setSessionAvailabilityListener(this@StreamService)
        }

        player = ReplaceableForwardingPlayer(
            player = if (castPlayer.isCastSessionAvailable) {
                castPlayer
            } else {
                localPlayer
            }
        ).apply {
            addListener(this@StreamService)
        }

        mediaSession = MediaLibrarySession.Builder(this, player, this)
            .setSessionActivity(pendingIntent)
            .build()
    }

    private fun getTrackInfo(mediaMetadata: MediaMetadata? = null): String {
        val trackInfo = mediaMetadata?.title?.toString()
        return if (castPlayer.isCastSessionAvailable) {
            castContext.connectedDeviceName()?.let { name ->
                getString(R.string.stream_subtitle_casting_device, name)
            } ?: getString(R.string.stream_subtitle_casting)
        } else if (trackInfo.isNullOrBlank()) {
            getString(R.string.stream_subtitle_unknown_song)
        } else {
            trackInfo
        }
    }

    private fun releaseMediaSession() {
        mediaSession.run {
            release()
            if (player.playbackState != STATE_IDLE) {
                player.removeListener(this@StreamService)
                player.release()
            }
        }
    }

    private fun startSleepTimer(ms: Long) {
        stopSleepTimer()
        countDownTimer = object : CountDownTimer(ms, SLEEP_TIMER_INTERVAL) {
            override fun onTick(msLeft: Long) {
                sendSleepTimerCommand(msLeft)
                if (msLeft < TimeUnit.SECONDS.toMillis(LOWER_VOLUME_CUTOFF.toLong())) {
                    // gently lower volume by the second
                    mediaSession.player.volume =
                        (msLeft / TimeUnit.SECONDS.toMillis(1)) / LOWER_VOLUME_CUTOFF
                }
            }

            override fun onFinish() {
                sendSleepTimerCommand(msLeft = 0L)
                mediaSession.player.pause()
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
        mediaSession.player.volume = MAX_VOLUME
    }

    companion object {

        const val START_TIMER_COMMAND = "start_timer"
        const val START_TIMER_COMMAND_VALUE_KEY = "start_timer_value"
        const val TIMER_UPDATED_COMMAND = "timer_updated"
        const val TIMER_UPDATED_COMMAND_VALUE_KEY = "timer_updated_value"
        const val TRACK_UPDATED_COMMAND = "track_updated"
        const val TRACK_UPDATED_COMMAND_VALUE_KEY = "track_updated_value"
        const val PLAYER_STREAM_ERROR_COMMAND = "player_error"

        private const val SLEEP_TIMER_INTERVAL = 1000L
        private const val LOWER_VOLUME_CUTOFF = 30f
        private const val MAX_VOLUME = 1.0f
    }
}
