package com.nielsmasdorp.nederadio.ui.home

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.ActiveStream
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.domain.stream.Streams
import com.nielsmasdorp.nederadio.ui.components.LoadingView
import com.nielsmasdorp.nederadio.ui.components.StreamsErrorView

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun HomeScreen(
    castButton: View,
    streams: Streams,
    activeStream: ActiveStream,
    favorites: List<Stream>,
    modifier: Modifier = Modifier,
    onStreamSelected: (String) -> Unit = {},
    onRetryStreams: () -> Unit = {},
    onSearch: () -> Unit = {},
    onEqualizer: () -> Unit = {},
    onAbout: () -> Unit = {}
) {

    val pagerState = rememberPagerState { 2 }

    Column(modifier = modifier) {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            castButton = castButton,
            showCastButton = activeStream is ActiveStream.Filled,
            onAboutClicked = onAbout,
            onEqualizerClicked = onEqualizer,
            onSearchClicked = onSearch,
        )
        when (streams) {
            is Streams.Loading -> LoadingView(
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )
            is Streams.Success -> {
                val tabs = listOf(
                    TabItem.Stations(
                        title = stringResource(id = R.string.stations_tab),
                        onSelectStream = onStreamSelected,
                        streams = streams.streams
                    ),
                    TabItem.Favorites(
                        title = stringResource(id = R.string.favorites_tab),
                        onSelectStream = onStreamSelected,
                        streams = favorites
                    )
                )
                Column {
                    StreamTabs(tabs = tabs, pagerState = pagerState)
                    StreamTabsContent(tabs = tabs, pagerState = pagerState)
                }
            }
            else -> StreamsErrorView { onRetryStreams() }
        }
    }
}
