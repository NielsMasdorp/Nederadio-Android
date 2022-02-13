package com.nielsmasdorp.nederadio.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.nielsmasdorp.nederadio.di.networkModule
import com.nielsmasdorp.nederadio.di.settingsModule
import com.nielsmasdorp.nederadio.di.streamModule
import com.nielsmasdorp.nederadio.di.uiModule
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.ui.components.EventHandler
import com.nielsmasdorp.nederadio.ui.home.HomeScreen
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.SheetContent
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.SheetCollapsed
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.StreamScreenSmall
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.SheetExpanded
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.StreamViewLarge
import com.nielsmasdorp.nederadio.ui.search.SearchScreen
import com.nielsmasdorp.nederadio.ui.components.dialog.AboutAppDialog
import com.nielsmasdorp.nederadio.ui.components.dialog.SleepTimerDialog
import com.nielsmasdorp.nederadio.ui.extension.currentFraction
import com.nielsmasdorp.nederadio.ui.search.SearchViewModel
import dev.burnoo.cokoin.Koin
import dev.burnoo.cokoin.viewmodel.getViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NederadioApp(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = getViewModel(),
    smallPlayerControls: PlayerControls<View>,
    largePlayerControls: PlayerControls<View>,
    castButton: View? = null,
) {

    DisposableEffect(key1 = viewModel) {
        viewModel.onStarted(controls = listOf(smallPlayerControls, largePlayerControls))
        onDispose { viewModel.onStopped() }
    }

    val navController = rememberAnimatedNavController()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    val sheetToggle: () -> Unit = {
        scope.launch {
            if (scaffoldState.bottomSheetState.isCollapsed) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    BackHandler(enabled = scaffoldState.bottomSheetState.isExpanded) {
        sheetToggle()
    }

    val currentStreams: CurrentStreams by viewModel.streams.observeAsState(initial = CurrentStreams.Loading)
    val currentFavorites: List<Stream> by viewModel.favorites.observeAsState(initial = emptyList())
    val currentStream: CurrentStream by viewModel.currentStream.observeAsState(initial = CurrentStream.Unknown)
    val sleepTimer: String? by viewModel.sleepTimer.observeAsState(initial = null)
    val showAboutAppDialog: Boolean by viewModel.showAboutApp.collectAsState(initial = false)
    val showSleepTimerDialog: Boolean by viewModel.showSleepTimer.collectAsState(initial = false)

    EventHandler(event = viewModel.errorState.error) {
        scaffoldState.snackbarHostState.showSnackbar(message = it)
    }

    if (showAboutAppDialog) {
        AboutAppDialog(onDismiss = viewModel::onAlertDismissed)
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onSelect = viewModel::setSleepTimer,
            onDismiss = viewModel::onAlertDismissed
        )
    }

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetElevation = if (currentStream is CurrentStream.Filled) 12.dp else 0.dp,
        backgroundColor = MaterialTheme.colorScheme.primary,
        sheetContent = {
            SheetContent {
                SheetExpanded(
                    content = {
                        StreamViewLarge(
                            playerControls = largePlayerControls,
                            currentStream = currentStream,
                            sleepTimer = sleepTimer,
                            onCollapseClick = { sheetToggle() },
                            onTimerClicked = viewModel::onTimerPicked,
                            onStreamFavoriteStatusChanged = viewModel::onStreamFavoriteChanged
                        )
                    }
                )
                SheetCollapsed(
                    isEnabled = scaffoldState.bottomSheetState.isCollapsed && currentStream is CurrentStream.Filled,
                    currentFraction = scaffoldState.currentFraction,
                    onSheetClick = { sheetToggle() },
                    currentStream = currentStream
                ) { currentStream ->
                    StreamScreenSmall(
                        playerControls = smallPlayerControls,
                        currentStream = currentStream,
                        onStreamFavoriteStatusChanged = viewModel::onStreamFavoriteChanged
                    )
                }
            }
        },
        sheetPeekHeight = 72.dp,
        sheetGesturesEnabled = false
    ) {
        AnimatedNavHost(navController = navController, startDestination = "home", route = "route") {
            composable("home") {
                HomeScreen(
                    modifier = modifier,
                    castButton = castButton,
                    streams = currentStreams,
                    favorites = currentFavorites,
                    onStreamSelected = viewModel::onStreamPicked,
                    onRetryStreams = viewModel::onRetryStreams,
                    onSearch = { navController.navigate("search") },
                    onAbout = viewModel::onAboutPicked
                )
            }
            composable("search",
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentScope.SlideDirection.Up,
                        animationSpec = tween(200)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentScope.SlideDirection.Down,
                        animationSpec = tween(200)
                    )
                }
            ) {
                val searchViewModel = getViewModel<SearchViewModel>(
                    viewModelStoreOwner = navController.getBackStackEntry("route")
                )
                SearchScreen(
                    modifier = modifier,
                    viewModel = searchViewModel,
                    onExitSearch = { navController.popBackStack() },
                    backPressHandler = if (scaffoldState.bottomSheetState.isExpanded) {
                        { sheetToggle() }
                    } else null
                )
            }
        }
    }
}

@Preview
@Composable
fun NederadioAppPreview() {
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
        val playerControls = PlayerControlsView.createViews(
            (LocalContext.current as Activity).layoutInflater
        )
        NederadioApp(
            smallPlayerControls = playerControls[0],
            largePlayerControls = playerControls[1],
        )
    }
}