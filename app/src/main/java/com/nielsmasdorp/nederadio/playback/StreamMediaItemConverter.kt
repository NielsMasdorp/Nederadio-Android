package com.nielsmasdorp.nederadio.playback

import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.EMPTY
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Since the [DefaultMediaItemConverter] from media3 only supports video we have a custom
 * implementation here that creates the [MediaQueueItem] to fit our purposes
 * Note: the local playback [ExoPlayer] updates the title to be the song
 * which is playing on a online stream. However: [CastPlayer] does not support this.
 * This is why we set the static subtitle and use the radio station as title
 *
 * Can maybe be removed in the future: https://github.com/androidx/media/commit/c9151c23a27566137b957f71694f8e7b6e67146d
 */
@UnstableApi
class StreamMediaItemConverter(private val staticTitle: String) : MediaItemConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val metadata = MediaMetadata(MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_ARTIST, staticTitle)
            putString(MediaMetadata.KEY_TITLE, mediaItem.mediaMetadata.artist.toString())
            addImage(WebImage(mediaItem.mediaMetadata.artworkUri!!))
        }
        val mediaInfo = MediaInfo.Builder(mediaItem.localConfiguration!!.uri.toString())
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType(mediaItem.localConfiguration!!.mimeType!!)
            .setMetadata(metadata)
            .build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        return EMPTY // one way street
    }
}