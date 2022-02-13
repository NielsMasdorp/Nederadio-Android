package com.nielsmasdorp.nederadio.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.ui.components.StreamsGrid

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
sealed class TabItem(
    val icon: ImageVector,
    open val title: String,
    val screen: @Composable () -> Unit,
) {
    data class Favorites(
        override val title: String,
        val onSelectStream: (String) -> Unit,
        val streams: List<Stream>
    ) : TabItem(
        icon = Icons.Default.Favorite,
        title = title,
        screen = { StreamsGrid(onSelectStream = onSelectStream, streams = streams) }
    )

    data class Stations(
        override val title: String,
        val onSelectStream: (String) -> Unit,
        val streams: List<Stream>
    ) : TabItem(
        icon = Icons.Default.Radio,
        title = title,
        screen = { StreamsGrid(onSelectStream = onSelectStream, streams = streams) }
    )
}
