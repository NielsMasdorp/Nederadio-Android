package com.nielsmasdorp.nederadio.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.nielsmasdorp.nederadio.ui.components.StreamsGrid
import org.koin.androidx.compose.getViewModel

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = getViewModel(),
    onExitSearch: () -> Unit = {},
    backPressHandler: (() -> Unit)? = null
) {

    val focusManager = LocalFocusManager.current

    val streams by viewModel.searchedStreams.collectAsState(initial = emptyList())
    val query: String by viewModel.searchQuery.collectAsState(initial = "")

    BackHandler(enabled = backPressHandler != null) {
        backPressHandler?.invoke()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SearchBar(
                modifier = Modifier.statusBarsPadding(),
                query = query,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onExitSearch = onExitSearch
            )
            StreamsGrid(streams = streams, onSelectStream = {
                focusManager.clearFocus()
                viewModel.onStreamPicked(it)
            })
        }
    }
}
