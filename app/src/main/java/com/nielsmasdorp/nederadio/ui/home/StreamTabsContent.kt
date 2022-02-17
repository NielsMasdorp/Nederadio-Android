package com.nielsmasdorp.nederadio.ui.home

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamTabsContent(
    modifier: Modifier = Modifier,
    tabs: List<TabItem>,
    pagerState: PagerState
) {
    HorizontalPager(
        modifier = modifier.background(MaterialTheme.colorScheme.primary),
        state = pagerState,
        count = tabs.size
    ) { page ->
        tabs[page].screen()
    }
}

@Preview
@Composable
fun StreamTabsContentPreview() = StreamTabsContent(
    tabs = listOf(
        TabItem.Stations(
            title = "Stations",
            onSelectStream = {},
            streams = emptyList()
        ),
        TabItem.Favorites(
            title = "Favorites",
            onSelectStream = {},
            streams = emptyList()
        )
    ), pagerState = rememberPagerState()
)