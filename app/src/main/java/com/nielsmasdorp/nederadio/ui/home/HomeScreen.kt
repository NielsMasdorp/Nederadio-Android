package com.nielsmasdorp.nederadio.ui.home

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.rememberPagerState
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.CurrentStreams
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.ui.components.LoadingView
import com.nielsmasdorp.nederadio.ui.components.StreamsErrorView

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    castButton: View? = null,
    streams: CurrentStreams,
    favorites: List<Stream>,
    onStreamSelected: (String) -> Unit = {},
    onRetryStreams: () -> Unit = {},
    onSearch: () -> Unit = {},
    onAbout: () -> Unit = {}
) {

    val pagerState = rememberPagerState()

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            castButton = castButton,
            onAboutClicked = onAbout,
            onSearchClicked = onSearch,
        )
        when (streams) {
            is CurrentStreams.Loading -> LoadingView(
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            is CurrentStreams.Success -> {
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

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(streams = CurrentStreams.Success(emptyList()), favorites = emptyList())
}
