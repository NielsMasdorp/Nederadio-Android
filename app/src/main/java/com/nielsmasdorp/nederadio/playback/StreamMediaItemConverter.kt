package com.nielsmasdorp.nederadio.playback

import androidx.core.net.toUri
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.cast.MediaMetadata as CastMetadata

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@UnstableApi
class StreamMediaItemConverter : MediaItemConverter {

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val mediaInfo = mediaQueueItem.media!!
        val metadata = mediaInfo.metadata!!
        val contentUrl = mediaInfo.contentUrl!!
        val imageUrl = metadata.images.first().url
        val artist = metadata.getString(CastMetadata.KEY_ARTIST)!!
        val title = metadata.getString(CastMetadata.KEY_TITLE)!!
        val mediaId = metadata.getString(KEY_MEDIA_ID)!!

        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(contentUrl.toUri())
                    .build()
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(artist)
                    .setTitle(title)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setArtworkUri(imageUrl)
                    .build()
            )
            .build()
    }

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val metadata = CastMetadata(MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(KEY_MEDIA_ID, mediaItem.mediaId)
            putString(CastMetadata.KEY_ARTIST, mediaItem.mediaMetadata.artist.toString())
            putString(CastMetadata.KEY_TITLE, mediaItem.mediaMetadata.title.toString())
            addImage(WebImage(mediaItem.mediaMetadata.artworkUri!!))
        }
        val mediaInfo = MediaInfo.Builder(mediaItem.mediaId)
            .setContentUrl(mediaItem.localConfiguration?.uri.toString())
            .setMetadata(metadata)
            .build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }

    companion object {
        private const val KEY_MEDIA_ID = "mediaId"
    }
}
