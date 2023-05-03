package com.nielsmasdorp.nederadio.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.GetSuccessfulStreams
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.util.moveToFront
import com.nielsmasdorp.nederadio.util.toMediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@UnstableApi
class StreamLibrary(
    private val context: Context, getSuccessfulStreams: GetSuccessfulStreams,
    private val getLastPlayedId: GetLastPlayedId
) {

    val streams: Flow<List<Stream>> = getSuccessfulStreams.streams

    /**
     * a tree of content that is usable by a [MediaBrowser]
     * contains a root, 2 tabs and list of children for each tab
     * used by Android Auto for example
     */
    val browsableContent = streams
        .map { list -> Pair(list.filter { it.isFavorite }, list) }
        .map { Tree(rootNode = it.asMediaItemTree()) }

    private fun Pair<List<Stream>, List<Stream>>.asMediaItemTree(): MediaItemNode {
        val (favorites, stations) = this
        return MediaItemNode(
            mediaItem = createBrowsableMediaItem(id = ROOT_ITEM_ID),
            children = listOf(
                MediaItemNode(
                    mediaItem = createBrowsableMediaItem(
                        id = FAVORITES_ITEM_ID,
                        title = context.getString(R.string.favorites_tab)
                    ),
                    children = favorites.map {
                        MediaItemNode(mediaItem = it.toMediaItem())
                    }
                ),
                MediaItemNode(
                    mediaItem = createBrowsableMediaItem(
                        id = STATIONS_ITEM_ID,
                        title = context.getString(R.string.stations_tab)
                    ),
                    children = stations.map {
                        MediaItemNode(mediaItem = it.toMediaItem())
                    }
                )
            )
        )
    }

    private fun createBrowsableMediaItem(id: String, title: String? = null): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
    }

    class Tree(val rootNode: MediaItemNode) {

        /**
         * Return the root of the list of stations with the last played item at the start
         */
        val recentRootNode: MediaItemNode
            get() = rootNode.children.find { it.mediaItem.mediaId == STATIONS_ITEM_ID }!!

        /**
         * Recreate the stations children list but put the last played item at the front
         */
        fun getRecentChildren(lastPlayedId: String? = null): List<MediaItem> {
            return recentRootNode
                .children
                .map { it.mediaItem }
                .toMutableList()
                .apply {
                    if (lastPlayedId != null) { moveToFront { it.mediaId == lastPlayedId } }
                }
        }

        /**
         * Return the children for a given id
         */
        fun getChildren(nodeId: String): List<MediaItem> {
            return if (rootNode.mediaItem.mediaId == nodeId) {
                rootNode.children.map { it.mediaItem }
            } else {
                for (child in rootNode.children) {
                    if (child.mediaItem.mediaId == nodeId) {
                        return child.children.map { it.mediaItem }
                    }
                }
                throw IllegalStateException("Unknown id: $nodeId!")
            }
        }

        /**
         * Return a single playable item for a given id
         */
        fun getItem(itemId: String): MediaItem {
            for (child in rootNode.children) {
                for (subChild in child.children) {
                    if (subChild.mediaItem.mediaId == itemId) return subChild.mediaItem
                }
            }
            throw IllegalStateException("Unknown id: $itemId!")
        }
    }

    class MediaItemNode(val mediaItem: MediaItem, val children: List<MediaItemNode> = emptyList())

    companion object {
        const val ROOT_ITEM_ID = "[rootId]"
        const val STATIONS_ITEM_ID = "[stationsId]"
        const val FAVORITES_ITEM_ID = "[favoritesId]"
    }
}