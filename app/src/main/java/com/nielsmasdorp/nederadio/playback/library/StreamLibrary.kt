package com.nielsmasdorp.nederadio.playback.library

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.AnyRes
import androidx.annotation.DrawableRes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.GetSuccessfulStreams
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.util.toMediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@UnstableApi
class StreamLibrary(private val context: Context, getSuccessfulStreams: GetSuccessfulStreams) {

    val streams: Flow<List<Stream>> = getSuccessfulStreams.streams

    /**
     * a tree of content that is usable by a MediaBrowser
     * contains a root, 2 tabs and list of children for each tab
     * used by Android Auto for example
     */
    val browsableContent = streams
        .map { list -> Pair(list.filter { it.isFavorite }, list) }
        .map { Tree(rootNode = it.asMediaItemTree()) }

    private fun Pair<List<Stream>, List<Stream>>.asMediaItemTree(): MediaItemNode {
        val (favorites, stations) = this
        val favoritesNode = MediaItemNode(
            mediaItem = createBrowsableMediaItem(
                id = FAVORITES_ITEM_ID,
                title = context.getString(R.string.favorites_tab),
                iconRes = R.drawable.ic_favorites
            ),
            children = favorites.map {
                MediaItemNode(mediaItem = it.toMediaItem())
            }
        )
        val stationsNode = MediaItemNode(
            mediaItem = createBrowsableMediaItem(
                id = STATIONS_ITEM_ID,
                title = context.getString(R.string.stations_tab),
                iconRes = R.drawable.ic_stations
            ),
            children = stations.map {
                MediaItemNode(mediaItem = it.toMediaItem())
            }
        )
        return MediaItemNode(
            mediaItem = createBrowsableMediaItem(id = ROOT_ITEM_ID),
            children = if (favorites.isNotEmpty()) {
                listOf(favoritesNode, stationsNode)
            } else {
                listOf(stationsNode) // TODO maybe find a better way, like implementing an empty view if it is somehow possible
            }
        )
    }

    private fun createBrowsableMediaItem(
        id: String,
        title: String? = null,
        @AnyRes iconRes: Int? = null
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtworkUri(
                        iconRes?.let {
                            Uri.parse(
                                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                        context.packageName + "/drawable" + '/' +
                                        context.resources.getResourceEntryName(it)
                            )
                        }
                    )
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
    }

    companion object {
        const val ROOT_ITEM_ID = "[rootId]"
        const val STATIONS_ITEM_ID = "[stationsId]"
        const val FAVORITES_ITEM_ID = "[favoritesId]"
    }
}