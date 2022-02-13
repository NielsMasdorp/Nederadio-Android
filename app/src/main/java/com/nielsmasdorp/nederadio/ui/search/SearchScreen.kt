package com.nielsmasdorp.nederadio.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.nielsmasdorp.nederadio.di.networkModule
import com.nielsmasdorp.nederadio.di.settingsModule
import com.nielsmasdorp.nederadio.di.streamModule
import com.nielsmasdorp.nederadio.di.uiModule
import com.nielsmasdorp.nederadio.ui.components.StreamsGrid
import dev.burnoo.cokoin.Koin
import dev.burnoo.cokoin.viewmodel.getViewModel
import org.koin.android.ext.koin.androidContext

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

    val streams by viewModel.searchedStreams.observeAsState(initial = emptyList())
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

@Preview
@Composable
fun SearchScreenPreview() {
    // TODO fix preview
    val context = LocalContext.current
    Koin(appDeclaration = {
        androidContext(context)
        modules(
            streamModule,
            settingsModule,
            networkModule,
            uiModule
        )
    }) {
        SearchScreen(viewModel = getViewModel())
    }
}