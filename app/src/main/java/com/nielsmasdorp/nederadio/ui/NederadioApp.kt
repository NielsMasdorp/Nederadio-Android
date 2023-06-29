package com.nielsmasdorp.nederadio.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.ui.components.EventHandler
import com.nielsmasdorp.nederadio.ui.components.dialog.AboutAppDialog
import com.nielsmasdorp.nederadio.ui.components.dialog.SleepTimerDialog
import com.nielsmasdorp.nederadio.ui.equalizer.EqualizerScreen
import com.nielsmasdorp.nederadio.ui.equalizer.EqualizerViewModel
import com.nielsmasdorp.nederadio.ui.extension.currentFraction
import com.nielsmasdorp.nederadio.ui.home.HomeScreen
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.SheetContent
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.SheetCollapsed
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.StreamScreenSmall
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.SheetExpanded
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.StreamViewLarge
import com.nielsmasdorp.nederadio.ui.search.SearchScreen
import com.nielsmasdorp.nederadio.ui.search.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Suppress("MagicNumber")
@Composable
fun NederadioApp(
    smallPlayerControls: PlayerControls<View>,
    largePlayerControls: PlayerControls<View>,
    castButton: View,
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = getViewModel()
) {

    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false
        )
        onDispose {}
    }

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

    val streams: Streams by viewModel.streams.collectAsState(initial = Streams.Loading)
    val currentFavorites: List<Stream> by viewModel.favorites.collectAsState(initial = emptyList())
    val activeStream: ActiveStream by viewModel.activeStream.collectAsState(initial = ActiveStream.Unknown)
    val sleepTimer: String? by viewModel.sleepTimer.collectAsState(initial = null)
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
        modifier = modifier
            .fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetElevation = if (activeStream is ActiveStream.Filled) 12.dp else 0.dp,
        backgroundColor = MaterialTheme.colorScheme.primary,
        sheetContent = {
            SheetContent {
                SheetExpanded(
                    content = {
                        StreamViewLarge(
                            playerControls = largePlayerControls,
                            activeStream = activeStream,
                            sleepTimer = sleepTimer,
                            onCollapseClick = { sheetToggle() },
                            onTimerClicked = viewModel::onTimerPicked,
                            onStreamFavoriteStatusChanged = viewModel::onStreamFavoriteChanged,
                            currentFraction = scaffoldState.currentFraction
                        )
                    }
                )
                SheetCollapsed(
                    isEnabled = scaffoldState.bottomSheetState.isCollapsed && activeStream is ActiveStream.Filled,
                    currentFraction = scaffoldState.currentFraction,
                    onSheetClick = { sheetToggle() },
                    activeStream = activeStream
                ) { currentStream ->
                    StreamScreenSmall(
                        playerControls = smallPlayerControls,
                        activeStream = currentStream,
                        onStreamFavoriteStatusChanged = viewModel::onStreamFavoriteChanged
                    )
                }
            }
        },
        sheetPeekHeight = 72.dp +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        sheetGesturesEnabled = false
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "home",
            route = "route"
        ) {
            composable("home") {
                HomeScreen(
                    modifier = Modifier.statusBarsPadding(),
                    castButton = castButton,
                    streams = streams,
                    activeStream = activeStream,
                    favorites = currentFavorites,
                    onStreamSelected = viewModel::onStreamPicked,
                    onRetryStreams = viewModel::onRetryStreams,
                    onSearch = { navController.navigate("search") },
                    onEqualizer = { navController.navigate("equalizer") },
                    onAbout = viewModel::onAboutPicked
                )
            }
            composable(
                "search",
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
                    modifier = Modifier.statusBarsPadding(),
                    viewModel = searchViewModel,
                    onExitSearch = { navController.popBackStack() },
                    backPressHandler = if (scaffoldState.bottomSheetState.isExpanded) {
                        { sheetToggle() }
                    } else null
                )
            }
            composable(
                "equalizer",
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
                val equalizerViewModel = getViewModel<EqualizerViewModel>(
                    viewModelStoreOwner = navController.getBackStackEntry("route")
                )
                EqualizerScreen(
                    modifier = Modifier.statusBarsPadding(),
                    viewModel = equalizerViewModel,
                    onExitEqualizer = { navController.popBackStack() },
                    backPressHandler = if (scaffoldState.bottomSheetState.isExpanded) {
                        { sheetToggle() }
                    } else null
                )
            }
        }
    }
}
