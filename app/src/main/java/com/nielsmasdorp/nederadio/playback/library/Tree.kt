package com.nielsmasdorp.nederadio.playback.library

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary.Companion.STATIONS_ITEM_ID
import com.nielsmasdorp.nederadio.util.moveToFront

@UnstableApi
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
                if (lastPlayedId != null) {
                    moveToFront { it.mediaId == lastPlayedId }
                }
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