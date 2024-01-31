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
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.session.*
import androidx.media3.session.MediaConstants.*
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionCommand.*
import androidx.media3.session.SessionResult.RESULT_SUCCESS
import com.google.android.gms.cast.framework.CastContext
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerManager
import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.SetActiveStream
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary
import com.nielsmasdorp.nederadio.playback.library.Tree
import com.nielsmasdorp.nederadio.ui.NederadioActivity
import com.nielsmasdorp.nederadio.util.sendCommandToController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.future
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Service responsible for hosting the [MediaLibrarySession]
 * Handles media notification and switching from foreground to background service whenever appropriate
 * Also switches from local playback to cast playback and exposes the library to other components
 * such as Android Auto
 */
@Suppress("TooManyFunctions")
@UnstableApi
class StreamService :
    MediaLibraryService(),
    Listener,
    MediaLibrarySession.Callback,
    SessionAvailabilityListener {

    private val streamLibrary: StreamLibrary by inject()

    private val contentTree: Flow<Tree> = streamLibrary.browsableContent
    private suspend fun Flow<Tree>.await(): Tree = first()

    private val setActiveStream: SetActiveStream by inject()
    private val getLastPlayedId: GetLastPlayedId by inject()
    private val equalizerManager: EqualizerManager by inject()
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
        teardown()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        teardown()
    }

    private fun teardown() {
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
            val contentTree = contentTree.await()
            val rootItem = if (!isRecentRequest) {
                contentTree.rootNode.mediaItem
            } else {
                // Playback resumption
                contentTree.recentRootNode.also {
                    contentTree.getLastPlayed(lastPlayedId = getLastPlayedId()!!).let { item ->
                        player.setMediaItem(item)
                        player.prepare()
                    }
                }
            }
            val extras = Bundle().apply {
                putInt(
                    EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                    EXTRAS_VALUE_CONTENT_STYLE_CATEGORY_GRID_ITEM
                )
                putBoolean(MEDIA_SEARCH_SUPPORTED, true)
            }
            val newParams = LibraryParams.Builder()
                .setRecent(isRecentRequest)
                .setExtras(extras)
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
            val contentTree = contentTree.await()
            val children = if (parentId == contentTree.recentRootNode.mediaId) {
                listOf(contentTree.getLastPlayed(lastPlayedId = getLastPlayedId()!!))
            } else {
                contentTree.getChildren(nodeId = parentId).toMutableList()
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
            LibraryResult.ofItem(contentTree.await().getItem(itemId = mediaId), null)
        }
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        return serviceScope.future {
            mediaSession.notifySearchResultChanged(
                /* browser */ browser,
                /* query */ query,
                /* itemCount */ contentTree.await().search(query = query).size,
                /* params */ params
            )
            LibraryResult.ofVoid()
        }
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return serviceScope.future {
            val results = contentTree.await().search(query = query)
            val fromIndex = max((page - 1) * pageSize, results.size - 1)
            val toIndex = max(fromIndex + pageSize, results.size)
            LibraryResult.ofItemList(
                /* items */ results.subList(fromIndex, toIndex),
                /* params */ params
            )
        }
    }

    /**
     * Casting has started, switch to [CastPlayer] and notify equalizer
     */
    override fun onCastSessionAvailable() {
        player.setPlayer(newPlayer = castPlayer)
        equalizerManager.onCastingStatusChanged(isCasting = true)
    }

    /**
     * Casting has been stopped, switch to [ExoPlayer] and notify equalizer
     */
    override fun onCastSessionUnavailable() {
        player.setPlayer(newPlayer = localPlayer)
        equalizerManager.onCastingStatusChanged(isCasting = false)
    }

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
        mediaMetadata.title ?: return
        mediaSession.sendCommandToController(
            key = TRACK_UPDATED_COMMAND,
            value = Bundle().apply {
                putString(TRACK_UPDATED_COMMAND_VALUE_KEY, mediaMetadata.title.toString())
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
     * Called when a [MediaController] has connected to the current [MediaSession]
     * If there is an item playing and we are just connected, post the metadata
     * to connected controllers
     */
    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        onMediaMetadataChanged(mediaMetadata = player.mediaMetadata)
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

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaItemsWithStartPosition> {
        return serviceScope.future {
            val contentTree = contentTree.await()
            if (mediaItems.size == 1) {
                val singleItem = mediaItems[0]
                val itemId = singleItem.mediaId
                if (itemId.isBlank() && singleItem.requestMetadata.searchQuery != null) {
                    // Voice search -> return search results
                    val streams = contentTree
                        .search(query = singleItem.requestMetadata.searchQuery)
                        .toMutableList()
                    MediaItemsWithStartPosition(streams, startIndex, startPositionMs)
                } else {
                    // Selected item in Android Auto, whole list gets dropped (bug)
                    // so -> recreate items with correct starting index of selected item
                    // see: https://github.com/androidx/media/issues/156 and 236
                    val streams = contentTree.getAllPlayableItems().toMutableList()
                    val index = streams.indexOfFirst { it.mediaId == itemId }
                    MediaItemsWithStartPosition(streams, index, startPositionMs)
                }
            } else {
                // normal cases -> map to content tree for usable URIs
                // because they get stripped for IPC safety
                val streams = contentTree.getAllPlayableItems()
                val mappedStreams = mediaItems.map { mediaItem ->
                    streams.find { it.mediaId == mediaItem.mediaId }!!
                }.toMutableList()
                MediaItemsWithStartPosition(mappedStreams, startIndex, startPositionMs)
            }
        }
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaItemsWithStartPosition> {
        return serviceScope.future {
            val content = contentTree.await()
            val streams = content.getAllPlayableItems()
            val lastPlayedStream = content.getLastPlayed(lastPlayedId = getLastPlayedId()!!)
            MediaItemsWithStartPosition(
                /* mediaItems */ streams.toMutableList(),
                /* startIndex */streams.indexOf(lastPlayedStream),
                /* startPositionMs */ 0L
            )
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun initialize() {
        // Might be the fix for randomly stopping streams:
        // see https://github.com/google/ExoPlayer/issues/7888
        val defaultRenderersFactory = DefaultRenderersFactory(this).apply {
            setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
                MediaCodecUtil.getDecoderInfos(
                    mimeType,
                    requiresSecureDecoder,
                    requiresTunnelingDecoder
                ).toMutableList().apply {
                    removeIf { it.name == "OMX.SEC.mp3.dec" }
                }
            }
        }
        localPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // Automatic requesting and dropping audio focus
            )
            .setRenderersFactory(defaultRenderersFactory)
            .setHandleAudioBecomingNoisy(true) // Handle headphones disconnect
            .setWakeMode(C.WAKE_MODE_NETWORK) // Wake+WiFi lock while playing
            .build().apply {
                equalizerManager.initialize(audioSessionId = audioSessionId)
            }

        val intent = Intent(this, NederadioActivity::class.java)
        val requestCode = 0
        val immutableFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE else requestCode
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            immutableFlag or FLAG_UPDATE_CURRENT
        )

        castContext = CastContext.getSharedInstance(this)
        castPlayer = CastPlayer(
            castContext,
            StreamMediaItemConverter()
        ).apply {
            setSessionAvailabilityListener(this@StreamService)
        }

        player = ReplaceableForwardingPlayer(
            player = if (castPlayer.isCastSessionAvailable) {
                castPlayer
            } else {
                localPlayer
            },
            castContext = castContext,
            context = applicationContext
        ).apply {
            addListener(this@StreamService)
        }

        mediaSession = MediaLibrarySession.Builder(this, player, this)
            .setSessionActivity(pendingIntent)
            .build()
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

        const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

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
