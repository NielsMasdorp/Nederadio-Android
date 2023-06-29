package com.nielsmasdorp.nederadio.playback

import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.*
import androidx.media3.common.Player.*
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import kotlin.math.min

/**
 * Mostly taken from https://github.com/android/uamp/blob/media3/common/src/main/java/com/example/android/uamp/media/ReplaceableForwardingPlayer.kt
 */
@Suppress("TooManyFunctions")
@UnstableApi
class ReplaceableForwardingPlayer(private var player: Player) : Player {

    private val externalListeners: MutableList<Listener> = arrayListOf()
    private val internalListener: Listener = PlayerListener()

    private val playlist: MutableList<MediaItem> = arrayListOf()
    private var currentPlaylistIndex: Int = 0

    init {
        player.addListener(internalListener)
    }

    fun setPlayer(newPlayer: Player) {
        for (listener in externalListeners) {
            player.removeListener(listener)
            newPlayer.addListener(listener)
        }

        // Add the listener that updates the current media item index
        // when station is changed via external input
        player.removeListener(internalListener)
        newPlayer.addListener(internalListener)

        newPlayer.apply {
            playWhenReady = player.playWhenReady
            setMediaItems(playlist, currentPlaylistIndex, contentPosition)
            prepare()
        }

        player.apply {
            clearMediaItems()
            stop()
        }.also {
            player = newPlayer
        }
    }

    override fun getApplicationLooper(): Looper = player.applicationLooper

    override fun addListener(listener: Listener) {
        player.addListener(listener)
        externalListeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        player.removeListener(listener)
        externalListeners.remove(listener)
    }

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
        startWindowIndex: Int,
        startPositionMs: Long
    ) {
        currentPlaylistIndex = startWindowIndex
        player.setMediaItems(mediaItems, startWindowIndex, startPositionMs)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
        player.setMediaItems(mediaItems)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        player.setMediaItems(mediaItems, resetPosition)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItem(mediaItem: MediaItem) {
        player.setMediaItem(mediaItem)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        player.setMediaItem(mediaItem, startPositionMs)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        player.setMediaItem(mediaItem, resetPosition)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
        playlist.add(mediaItem)
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        player.addMediaItem(index, mediaItem)
        playlist.add(index, mediaItem)
    }

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {
        player.addMediaItems(mediaItems)
        playlist.addAll(mediaItems)
    }

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {
        player.addMediaItems(index, mediaItems)
        playlist.addAll(index, mediaItems)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        player.moveMediaItem(currentIndex, newIndex)
        playlist.add(min(newIndex, playlist.size), playlist.removeAt(currentIndex))
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        val removedItems: ArrayDeque<MediaItem> = ArrayDeque()
        val removedItemsLength = toIndex - fromIndex
        for (i in removedItemsLength - 1 downTo 0) {
            removedItems.addFirst(playlist.removeAt(fromIndex + i))
        }
        playlist.addAll(min(newIndex, playlist.size), removedItems)
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
        player.replaceMediaItem(index, mediaItem)
        playlist.set(index, mediaItem)
    }

    override fun replaceMediaItems(
        fromIndex: Int,
        toIndex: Int,
        mediaItems: MutableList<MediaItem>
    ) {
        player.replaceMediaItems(fromIndex, toIndex, mediaItems)
        mediaItems.forEachIndexed { index, mediaItem ->
            playlist.set(fromIndex + index, mediaItem)
        }
    }

    override fun removeMediaItem(index: Int) {
        player.removeMediaItem(index)
        playlist.removeAt(index)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        player.removeMediaItems(fromIndex, toIndex)
        val removedItemsLength = toIndex - fromIndex
        for (i in removedItemsLength - 1 downTo 0) {
            playlist.removeAt(fromIndex + i)
        }
    }

    override fun clearMediaItems() {
        player.clearMediaItems()
        playlist.clear()
    }

    override fun isCommandAvailable(command: Int): Boolean = player.isCommandAvailable(command)

    override fun canAdvertiseSession(): Boolean = player.canAdvertiseSession()

    override fun getAvailableCommands(): Commands = player.availableCommands

    override fun prepare() = player.prepare()

    override fun getPlaybackState(): Int = player.playbackState

    override fun getPlaybackSuppressionReason(): Int = player.playbackSuppressionReason

    override fun isPlaying(): Boolean = player.isPlaying

    override fun getPlayerError(): PlaybackException? = player.playerError

    override fun play() = player.play()

    override fun pause() = player.pause()

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        player.playWhenReady = playWhenReady
    }

    override fun getPlayWhenReady(): Boolean = player.playWhenReady

    override fun setRepeatMode(repeatMode: Int) {
        player.repeatMode = repeatMode
    }

    override fun getRepeatMode(): Int = player.repeatMode

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        player.shuffleModeEnabled = shuffleModeEnabled
    }

    override fun getShuffleModeEnabled(): Boolean = player.shuffleModeEnabled

    override fun isLoading(): Boolean = player.isLoading

    override fun seekToDefaultPosition() = player.seekToDefaultPosition()

    override fun seekToDefaultPosition(windowIndex: Int) = player.seekToDefaultPosition(windowIndex)

    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun seekTo(windowIndex: Int, positionMs: Long) = player.seekTo(windowIndex, positionMs)

    override fun getSeekBackIncrement(): Long = player.seekBackIncrement

    override fun seekBack() = player.seekBack()

    override fun getSeekForwardIncrement(): Long = player.seekForwardIncrement

    override fun seekForward() = player.seekForward()

    override fun hasPrevious(): Boolean = player.hasPrevious()

    override fun hasPreviousWindow(): Boolean = player.hasPreviousWindow()

    override fun hasPreviousMediaItem(): Boolean = player.hasPreviousMediaItem()

    override fun previous() = player.previous()

    override fun seekToPreviousWindow() = player.seekToPreviousWindow()

    override fun seekToPreviousMediaItem() = player.seekToPreviousMediaItem()

    override fun getMaxSeekToPreviousPosition(): Long = player.maxSeekToPreviousPosition

    override fun seekToPrevious() = player.seekToPrevious()

    override fun hasNext(): Boolean = player.hasNext()

    override fun hasNextWindow(): Boolean = player.hasNextWindow()

    override fun hasNextMediaItem(): Boolean = player.hasNextMediaItem()

    override fun next() = player.next()

    override fun seekToNextWindow() = player.seekToNextWindow()

    override fun seekToNextMediaItem() = player.seekToNextMediaItem()

    override fun seekToNext() = player.seekToNext()

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        player.playbackParameters = playbackParameters
    }

    override fun setPlaybackSpeed(speed: Float) = player.setPlaybackSpeed(speed)

    override fun getPlaybackParameters(): PlaybackParameters = player.playbackParameters

    override fun stop() = player.stop()

    override fun release() {
        player.release()
        playlist.clear()
    }

    override fun getCurrentTracks(): Tracks = player.currentTracks

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return player.trackSelectionParameters
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        player.trackSelectionParameters = parameters
    }

    override fun getMediaMetadata(): MediaMetadata = player.mediaMetadata

    override fun getPlaylistMetadata(): MediaMetadata = player.playlistMetadata

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        player.playlistMetadata = mediaMetadata
    }

    override fun getCurrentManifest(): Any? = player.currentManifest

    override fun getCurrentTimeline(): Timeline = player.currentTimeline

    override fun getCurrentPeriodIndex(): Int = player.currentPeriodIndex

    override fun getCurrentWindowIndex(): Int = player.currentWindowIndex

    override fun getCurrentMediaItemIndex(): Int = player.currentMediaItemIndex

    override fun getNextWindowIndex(): Int = player.nextWindowIndex

    override fun getNextMediaItemIndex(): Int {
        return player.nextMediaItemIndex
    }

    override fun getPreviousWindowIndex(): Int = player.previousWindowIndex

    override fun getPreviousMediaItemIndex(): Int = player.previousMediaItemIndex

    override fun getCurrentMediaItem(): MediaItem? = player.currentMediaItem

    override fun getMediaItemCount(): Int = player.mediaItemCount

    override fun getMediaItemAt(index: Int): MediaItem = player.getMediaItemAt(index)

    override fun getDuration(): Long = player.duration

    override fun getCurrentPosition(): Long = player.currentPosition

    override fun getBufferedPosition(): Long = player.bufferedPosition

    override fun getBufferedPercentage(): Int = player.bufferedPercentage

    override fun getTotalBufferedDuration(): Long = player.totalBufferedDuration

    override fun isCurrentWindowDynamic(): Boolean = player.isCurrentWindowDynamic

    override fun isCurrentMediaItemDynamic(): Boolean = player.isCurrentMediaItemDynamic

    override fun isCurrentWindowLive(): Boolean = player.isCurrentWindowLive

    override fun isCurrentMediaItemLive(): Boolean = player.isCurrentMediaItemLive

    override fun getCurrentLiveOffset(): Long = player.currentLiveOffset

    override fun isCurrentWindowSeekable(): Boolean = player.isCurrentWindowSeekable

    override fun isCurrentMediaItemSeekable(): Boolean = player.isCurrentMediaItemSeekable

    override fun isPlayingAd(): Boolean = player.isPlayingAd

    override fun getCurrentAdGroupIndex(): Int = player.currentAdGroupIndex

    override fun getCurrentAdIndexInAdGroup(): Int = player.currentAdIndexInAdGroup

    override fun getContentDuration(): Long = player.contentDuration

    override fun getContentPosition(): Long = player.contentPosition

    override fun getContentBufferedPosition(): Long = player.contentBufferedPosition

    override fun getAudioAttributes(): AudioAttributes = player.audioAttributes

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun getVolume(): Float = player.volume

    override fun clearVideoSurface() = player.clearVideoSurface()

    override fun clearVideoSurface(surface: Surface?) = player.clearVideoSurface(surface)

    override fun setVideoSurface(surface: Surface?) = player.setVideoSurface(surface)

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        player.setVideoSurfaceHolder(surfaceHolder)
    }

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        player.clearVideoSurfaceHolder(surfaceHolder)
    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        return player.setVideoSurfaceView(surfaceView)
    }

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {
        return player.clearVideoSurfaceView(surfaceView)
    }

    override fun setVideoTextureView(textureView: TextureView?) {
        return player.setVideoTextureView(textureView)
    }

    override fun clearVideoTextureView(textureView: TextureView?) {
        return player.clearVideoTextureView(textureView)
    }

    override fun getVideoSize(): VideoSize = player.videoSize

    override fun getSurfaceSize(): Size = player.surfaceSize

    override fun getCurrentCues(): CueGroup = player.currentCues

    override fun getDeviceInfo(): DeviceInfo = player.deviceInfo

    override fun getDeviceVolume(): Int = player.deviceVolume

    override fun isDeviceMuted(): Boolean = player.isDeviceMuted

    override fun setDeviceVolume(volume: Int) {
        player.deviceVolume = volume
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
        player.setDeviceVolume(volume, flags)
    }

    override fun increaseDeviceVolume() = player.increaseDeviceVolume()

    override fun increaseDeviceVolume(flags: Int) {
        player.increaseDeviceVolume(flags)
    }

    override fun decreaseDeviceVolume() = player.decreaseDeviceVolume()

    override fun decreaseDeviceVolume(flags: Int) {
        player.decreaseDeviceVolume(flags)
    }

    override fun setDeviceMuted(muted: Boolean) {
        player.isDeviceMuted = muted
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
        player.setDeviceMuted(muted, flags)
    }

    private inner class PlayerListener : Listener {
        override fun onEvents(player: Player, events: Events) {
            if (events.contains(EVENT_POSITION_DISCONTINUITY) ||
                events.contains(EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(EVENT_TIMELINE_CHANGED)
            ) {
                if (!player.currentTimeline.isEmpty) {
                    currentPlaylistIndex = player.currentMediaItemIndex
                }
            }
        }
    }
}
