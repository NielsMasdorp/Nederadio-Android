package com.nielsmasdorp.nederadio.playback.library

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary.Companion.RECENT_ITEM_ID
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary.Companion.STATIONS_ITEM_ID

@UnstableApi
class Tree(val rootNode: MediaItemNode) {

    val recentRootNode: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId(RECENT_ITEM_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    fun getLastPlayed(lastPlayedId: String): MediaItem {
        return rootNode.children.first { it.mediaItem.mediaId == STATIONS_ITEM_ID }
            .children
            .find { it.mediaItem.mediaId == lastPlayedId }!!
            .mediaItem
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
            error("Unknown id: $nodeId!")
        }
    }

    /**
     * Return all playable items
     */
    fun getAllPlayableItems() = getChildren(nodeId = STATIONS_ITEM_ID)

    /**
     * Return a single playable item for a given id
     */
    fun getItem(itemId: String): MediaItem {
        for (child in rootNode.children) {
            for (subChild in child.children) {
                if (subChild.mediaItem.mediaId == itemId) return subChild.mediaItem
            }
        }
        error("Unknown id: $itemId!")
    }

    /**
     * Return a list of items based on a search query
     */
    fun search(query: String?): List<MediaItem> {
        return getChildren(nodeId = STATIONS_ITEM_ID).let { results ->
            if (query.isNullOrBlank()) {
                // Return shuffled full list when no query is supplied
                results.shuffled()
            } else {
                results.filter { it.mediaMetadata.artist?.contains(query) == true }
            }
        }
    }
}
