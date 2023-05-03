package com.nielsmasdorp.nederadio.playback.library

import androidx.media3.common.MediaItem

class MediaItemNode(val mediaItem: MediaItem, val children: List<MediaItemNode> = emptyList())