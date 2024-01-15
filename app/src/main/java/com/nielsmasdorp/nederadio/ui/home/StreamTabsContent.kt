package com.nielsmasdorp.nederadio.ui.home

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamTabsContent(
    tabs: List<TabItem>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState
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
    ),
    pagerState = rememberPagerState { 2 }
)
