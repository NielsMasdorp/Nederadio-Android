package com.nielsmasdorp.nederadio.playback

import android.content.Context
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nielsmasdorp.nederadio.R
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * As it stands [CastPlayer] from Media3 has a couple of limitations making it impossible to
 * use for our use case.
 * Limitation 1: currentMediaItem always returns MediaItem.EMPTY and therefore
 * Player.Listener#onMediaItemTransition gets called with an empty item.
 * see: https://github.com/androidx/media/issues/25
 *
 * Limitation 2: mediaMetadata always returns null and
 * Player.Listener#onMediaMetadataChanged never gets called
 * see: https://github.com/androidx/media/issues/26
 *
 * This class aims to solve Limitation 1. It keeps a reference to the current [MediaItem]
 * and returns that inside Player.Listener#onMediaItemTransition and currentMediaItem
 * For Limitation 2 there is no solution at the moment, therefore we have no live track updates
 * when casting, only when local playback is active
 */
@UnstableApi
class CompositePlayer(
    private val staticTitle: String,
    private val localPlayer: ExoPlayer,
    private val castPlayer: CastPlayer
) : Player, Player.Listener {

    private var currentListeners: CopyOnWriteArraySet<Player.Listener> = CopyOnWriteArraySet()
    private var currentMediaItem: MediaItem? = null
    private var currentPlayer: Player = if (castPlayer.isCastSessionAvailable) {
        castPlayer
    } else {
        localPlayer
    }

    init {
        localPlayer.addListener(this)
        castPlayer.addListener(this)
    }

    fun setSessionAvailabilityListener(listener: SessionAvailabilityListener) {
        castPlayer.setSessionAvailabilityListener(listener)
    }

    /**
     * Stop casting current media playback
     */
    fun switchToLocal() = switchPlayer(localPlayer)

    /**
     * Start casting current media playback
     */
    fun switchToCast() = switchPlayer(castPlayer)

    /**
     * Notify latest [MediaItem] to listener
     */
    fun notifyCurrentMediaItem(listener: Player.Listener) {
        currentMediaItem ?: return
        listener.onMediaItemTransition(
            currentMediaItem,
            MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
        )
    }

    private fun switchPlayer(newPlayer: Player) {
        castPlayer.deviceInfo
        if (currentPlayer == newPlayer) return
        currentMediaItem?.let { current ->
            currentPlayer.stop()
            updateCurrentMediaItem(new = current, forPlayer = newPlayer)

            // needed to update current notification
            currentPlayer.setMediaItem(currentMediaItem!!)

            currentPlayer = newPlayer
            newPlayer.setMediaItem(currentMediaItem!!)
            newPlayer.prepare()
            newPlayer.play()
        } ?: run {
            currentPlayer = newPlayer
        }
    }

    /**
     * Since [CastPlayer], unlike `[ExoPlayer] does not support dynamic track data
     * we set the title of the [MediaMetadata] to a static title
     * when the title is set to null if the new player is the local player
     * [ExoPlayer] will dynamically change the track title
     */
    private fun updateCurrentMediaItem(new: MediaItem, forPlayer: Player) {
        currentMediaItem = new
            .buildUpon()
            .setMediaMetadata(
                new.mediaMetadata
                    .buildUpon()
                    .setTitle(if (forPlayer == castPlayer) staticTitle else null)
                    .build()
            )
            .build()
    }

    override fun getApplicationLooper(): Looper = currentPlayer.applicationLooper

    override fun addListener(listener: Player.Listener) {
        currentListeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        currentListeners.remove(listener)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) =
        currentPlayer.setMediaItems(mediaItems)

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) =
        currentPlayer.setMediaItems(mediaItems, resetPosition)

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
        startWindowIndex: Int,
        startPositionMs: Long
    ) = currentPlayer.setMediaItems(mediaItems, startWindowIndex, startPositionMs)

    override fun setMediaItem(mediaItem: MediaItem) {
        updateCurrentMediaItem(new = mediaItem, forPlayer = currentPlayer)
        currentPlayer.stop()
        currentPlayer.setMediaItem(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) =
        currentPlayer.setMediaItem(mediaItem, startPositionMs)

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) =
        currentPlayer.setMediaItem(mediaItem, resetPosition)

    override fun addMediaItem(mediaItem: MediaItem) = currentPlayer.addMediaItem(mediaItem)

    override fun addMediaItem(index: Int, mediaItem: MediaItem) =
        currentPlayer.addMediaItem(index, mediaItem)

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) =
        currentPlayer.addMediaItems(mediaItems)

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) =
        currentPlayer.addMediaItems(index, mediaItems)

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) =
        currentPlayer.moveMediaItem(currentIndex, newIndex)

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) =
        currentPlayer.moveMediaItems(fromIndex, toIndex, newIndex)

    override fun removeMediaItem(index: Int) = currentPlayer.removeMediaItem(index)

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) =
        currentPlayer.removeMediaItems(fromIndex, toIndex)

    override fun clearMediaItems() = currentPlayer.clearMediaItems()

    override fun isCommandAvailable(command: Int): Boolean =
        currentPlayer.isCommandAvailable(command)

    override fun canAdvertiseSession(): Boolean = currentPlayer.canAdvertiseSession()

    override fun getAvailableCommands(): Player.Commands = currentPlayer.availableCommands

    override fun prepare() = currentPlayer.prepare()

    override fun getPlaybackState(): Int = currentPlayer.playbackState

    override fun getPlaybackSuppressionReason(): Int = currentPlayer.playbackSuppressionReason

    override fun isPlaying(): Boolean = currentPlayer.isPlaying

    override fun getPlayerError(): PlaybackException? = currentPlayer.playerError

    override fun play() = currentPlayer.play()

    override fun pause() = currentPlayer.pause()

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        currentPlayer.playWhenReady = playWhenReady
    }

    override fun getPlayWhenReady(): Boolean = currentPlayer.playWhenReady

    override fun setRepeatMode(repeatMode: Int) {
        currentPlayer.repeatMode = repeatMode
    }

    override fun getRepeatMode(): Int = currentPlayer.repeatMode

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        currentPlayer.shuffleModeEnabled = shuffleModeEnabled
    }

    override fun getShuffleModeEnabled(): Boolean = currentPlayer.shuffleModeEnabled

    override fun isLoading(): Boolean = currentPlayer.isLoading

    override fun seekToDefaultPosition() = currentPlayer.seekToDefaultPosition()

    override fun seekToDefaultPosition(windowIndex: Int) =
        currentPlayer.seekToDefaultPosition(windowIndex)

    override fun seekTo(positionMs: Long) = currentPlayer.seekTo(positionMs)

    override fun seekTo(windowIndex: Int, positionMs: Long) =
        currentPlayer.seekTo(windowIndex, positionMs)

    override fun getSeekBackIncrement(): Long = currentPlayer.seekBackIncrement

    override fun seekBack() = currentPlayer.seekBack()

    override fun getSeekForwardIncrement(): Long = currentPlayer.seekForwardIncrement

    override fun seekForward() = currentPlayer.seekForward()

    override fun hasPrevious(): Boolean = currentPlayer.hasPrevious()

    override fun hasPreviousWindow(): Boolean = currentPlayer.hasPreviousWindow()

    override fun hasPreviousMediaItem(): Boolean = currentPlayer.hasPreviousMediaItem()

    override fun previous() = currentPlayer.previous()

    override fun seekToPreviousWindow() = currentPlayer.seekToPreviousWindow()

    override fun seekToPreviousMediaItem() = currentPlayer.seekToPreviousMediaItem()

    override fun getMaxSeekToPreviousPosition(): Long = currentPlayer.maxSeekToPreviousPosition

    override fun seekToPrevious() = currentPlayer.seekToPrevious()

    override fun hasNext(): Boolean = currentPlayer.hasNext()

    override fun hasNextWindow(): Boolean = currentPlayer.hasNextWindow()

    override fun hasNextMediaItem(): Boolean = currentPlayer.hasNextMediaItem()

    override fun next() = currentPlayer.next()

    override fun seekToNextWindow() = currentPlayer.seekToNextWindow()

    override fun seekToNextMediaItem() = currentPlayer.seekToNextMediaItem()

    override fun seekToNext() = currentPlayer.seekToNext()

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        currentPlayer.playbackParameters = playbackParameters
    }

    override fun setPlaybackSpeed(speed: Float) = currentPlayer.setPlaybackSpeed(speed)

    override fun getPlaybackParameters(): PlaybackParameters = currentPlayer.playbackParameters

    override fun stop() = currentPlayer.stop()

    override fun stop(reset: Boolean) = currentPlayer.stop(reset)

    override fun release() {
        currentListeners.clear()
        localPlayer.removeListener(this)
        castPlayer.removeListener(this)
        localPlayer.release()
        castPlayer.release()
        castPlayer.setSessionAvailabilityListener(null)
    }

    override fun getCurrentTrackGroups(): TrackGroupArray = currentPlayer.currentTrackGroups

    override fun getCurrentTrackSelections(): TrackSelectionArray =
        currentPlayer.currentTrackSelections

    override fun getCurrentTracksInfo(): TracksInfo = currentPlayer.currentTracksInfo

    override fun getTrackSelectionParameters(): TrackSelectionParameters =
        currentPlayer.trackSelectionParameters

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        currentPlayer.trackSelectionParameters = parameters
    }

    override fun getMediaMetadata(): MediaMetadata =
        currentMediaItem?.mediaMetadata ?: MediaMetadata.EMPTY

    override fun getPlaylistMetadata(): MediaMetadata = currentPlayer.playlistMetadata

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        currentPlayer.playlistMetadata = mediaMetadata
    }

    override fun getCurrentManifest(): Any? = currentPlayer.currentManifest

    override fun getCurrentTimeline(): Timeline = currentPlayer.currentTimeline

    override fun getCurrentPeriodIndex(): Int = currentPlayer.currentPeriodIndex

    override fun getCurrentWindowIndex(): Int = currentPlayer.currentWindowIndex

    override fun getCurrentMediaItemIndex(): Int = currentPlayer.currentMediaItemIndex

    override fun getNextWindowIndex(): Int = currentPlayer.nextWindowIndex

    override fun getNextMediaItemIndex(): Int = currentPlayer.nextMediaItemIndex

    override fun getPreviousWindowIndex(): Int = currentPlayer.previousWindowIndex

    override fun getPreviousMediaItemIndex(): Int = currentPlayer.previousMediaItemIndex

    override fun getCurrentMediaItem(): MediaItem? = currentMediaItem

    override fun getMediaItemCount(): Int = currentPlayer.mediaItemCount

    override fun getMediaItemAt(index: Int): MediaItem = currentPlayer.getMediaItemAt(index)

    override fun getDuration(): Long = currentPlayer.duration

    override fun getCurrentPosition(): Long = currentPlayer.currentPosition

    override fun getBufferedPosition(): Long = currentPlayer.bufferedPosition

    override fun getBufferedPercentage(): Int = currentPlayer.bufferedPercentage

    override fun getTotalBufferedDuration(): Long = currentPlayer.totalBufferedDuration

    override fun isCurrentWindowDynamic(): Boolean = currentPlayer.isCurrentWindowDynamic

    override fun isCurrentMediaItemDynamic(): Boolean = currentPlayer.isCurrentMediaItemDynamic

    override fun isCurrentWindowLive(): Boolean = currentPlayer.isCurrentWindowLive

    override fun isCurrentMediaItemLive(): Boolean = currentPlayer.isCurrentMediaItemLive

    override fun getCurrentLiveOffset(): Long = currentPlayer.currentLiveOffset

    override fun isCurrentWindowSeekable(): Boolean = currentPlayer.isCurrentWindowSeekable

    override fun isCurrentMediaItemSeekable(): Boolean = currentPlayer.isCurrentMediaItemSeekable

    override fun isPlayingAd(): Boolean = currentPlayer.isPlayingAd

    override fun getCurrentAdGroupIndex(): Int = currentPlayer.currentAdGroupIndex

    override fun getCurrentAdIndexInAdGroup(): Int = currentPlayer.currentAdIndexInAdGroup

    override fun getContentDuration(): Long = currentPlayer.contentDuration

    override fun getContentPosition(): Long = currentPlayer.contentPosition

    override fun getContentBufferedPosition(): Long = currentPlayer.contentBufferedPosition

    override fun getAudioAttributes(): AudioAttributes = currentPlayer.audioAttributes

    override fun setVolume(volume: Float) {
        currentPlayer.volume = volume
    }

    override fun getVolume(): Float = currentPlayer.volume

    override fun clearVideoSurface() = currentPlayer.clearVideoSurface()

    override fun clearVideoSurface(surface: Surface?) = currentPlayer.clearVideoSurface(surface)

    override fun setVideoSurface(surface: Surface?) = currentPlayer.setVideoSurface(surface)

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) =
        currentPlayer.setVideoSurfaceHolder(surfaceHolder)

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) =
        currentPlayer.clearVideoSurfaceHolder(surfaceHolder)

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) =
        currentPlayer.setVideoSurfaceView(surfaceView)

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) =
        currentPlayer.clearVideoSurfaceView(surfaceView)

    override fun setVideoTextureView(textureView: TextureView?) =
        currentPlayer.setVideoTextureView(textureView)

    override fun clearVideoTextureView(textureView: TextureView?) =
        currentPlayer.clearVideoTextureView(textureView)

    override fun getVideoSize(): VideoSize = currentPlayer.videoSize

    override fun getCurrentCues(): MutableList<Cue> = currentPlayer.currentCues

    override fun getDeviceInfo(): DeviceInfo = currentPlayer.deviceInfo

    override fun getDeviceVolume(): Int = currentPlayer.deviceVolume

    override fun isDeviceMuted(): Boolean = currentPlayer.isDeviceMuted

    override fun setDeviceVolume(volume: Int) {
        currentPlayer.deviceVolume = volume
    }

    override fun increaseDeviceVolume() = currentPlayer.increaseDeviceVolume()

    override fun decreaseDeviceVolume() = currentPlayer.decreaseDeviceVolume()

    override fun setDeviceMuted(muted: Boolean) {
        currentPlayer.isDeviceMuted = muted
    }


    // Player.Listener callbacks ---------------------------------------------------

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        for (listener in currentListeners) {
            listener.onTimelineChanged(timeline, reason)
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        for (listener in currentListeners) {
            listener.onMediaItemTransition(currentMediaItem, reason)
        }
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        for (listener in currentListeners) {
            listener.onTracksChanged(trackGroups, trackSelections)
        }
    }

    override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
        for (listener in currentListeners) {
            listener.onTracksInfoChanged(tracksInfo)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        currentMediaItem ?: return
        currentMediaItem = currentMediaItem!!
            .buildUpon()
            .setMediaMetadata(
                currentMediaItem!!.mediaMetadata
                    .buildUpon()
                    .setTitle(mediaMetadata.title)
                    .build()
            ).build()

        for (listener in currentListeners) {
            listener.onMediaMetadataChanged(mediaMetadata)
        }
    }

    override fun onMetadata(metadata: Metadata) {
        for (listener in currentListeners) {
            listener.onMetadata(metadata)
        }
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        for (listener in currentListeners) {
            listener.onPlaylistMetadataChanged(mediaMetadata)
        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        for (listener in currentListeners) {
            listener.onIsLoadingChanged(isLoading)
        }
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        for (listener in currentListeners) {
            listener.onLoadingChanged(isLoading)
        }
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        for (listener in currentListeners) {
            listener.onAvailableCommandsChanged(availableCommands)
        }
    }

    override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
        for (listener in currentListeners) {
            listener.onTrackSelectionParametersChanged(parameters)
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        for (listener in currentListeners) {
            listener.onPlayerStateChanged(playWhenReady, playbackState)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        for (listener in currentListeners) {
            listener.onPlaybackStateChanged(playbackState)
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        for (listener in currentListeners) {
            listener.onPlayWhenReadyChanged(playWhenReady, reason)
        }
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        for (listener in currentListeners) {
            listener.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        for (listener in currentListeners) {
            listener.onIsPlayingChanged(isPlaying)
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        for (listener in currentListeners) {
            listener.onRepeatModeChanged(repeatMode)
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        for (listener in currentListeners) {
            listener.onShuffleModeEnabledChanged(shuffleModeEnabled)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        for (listener in currentListeners) {
            listener.onPlayerError(error)
        }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        for (listener in currentListeners) {
            listener.onPlayerErrorChanged(error)
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        for (listener in currentListeners) {
            listener.onPositionDiscontinuity(oldPosition, newPosition, reason)
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        for (listener in currentListeners) {
            listener.onPositionDiscontinuity(reason)
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        for (listener in currentListeners) {
            listener.onPlaybackParametersChanged(playbackParameters)
        }
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        for (listener in currentListeners) {
            listener.onSeekBackIncrementChanged(seekBackIncrementMs)
        }
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        for (listener in currentListeners) {
            listener.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        }
    }

    override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
        for (listener in currentListeners) {
            listener.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
        }
    }

    override fun onSeekProcessed() {
        for (listener in currentListeners) {
            listener.onSeekProcessed()
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        for (listener in currentListeners) {
            listener.onEvents(player, events)
        }
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        for (listener in currentListeners) {
            listener.onAudioSessionIdChanged(audioSessionId)
        }
    }

    override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
        for (listener in currentListeners) {
            listener.onAudioAttributesChanged(audioAttributes)
        }
    }

    override fun onVolumeChanged(volume: Float) {
        for (listener in currentListeners) {
            listener.onVolumeChanged(volume)
        }
    }

    override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
        for (listener in currentListeners) {
            listener.onSkipSilenceEnabledChanged(skipSilenceEnabled)
        }
    }

    override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
        for (listener in currentListeners) {
            listener.onDeviceInfoChanged(deviceInfo)
        }
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        for (listener in currentListeners) {
            listener.onDeviceVolumeChanged(volume, muted)
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        for (listener in currentListeners) {
            listener.onVideoSizeChanged(videoSize)
        }
    }

    override fun onSurfaceSizeChanged(width: Int, height: Int) {
        for (listener in currentListeners) {
            listener.onSurfaceSizeChanged(width, height)
        }
    }

    override fun onRenderedFirstFrame() {
        for (listener in currentListeners) {
            listener.onRenderedFirstFrame()
        }
    }

    override fun onCues(cues: MutableList<Cue>) {
        for (listener in currentListeners) {
            listener.onCues(cues)
        }
    }
}