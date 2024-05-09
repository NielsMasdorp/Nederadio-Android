package com.nielsmasdorp.nederadio.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.ui.components.EventHandler
import com.nielsmasdorp.nederadio.ui.components.dialog.AboutAppDialog
import com.nielsmasdorp.nederadio.ui.components.dialog.SleepTimerDialog
import com.nielsmasdorp.nederadio.ui.equalizer.EqualizerScreen
import com.nielsmasdorp.nederadio.ui.equalizer.EqualizerViewModel
import com.nielsmasdorp.nederadio.ui.home.HomeScreen
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.SheetContent
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.SheetCollapsed
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed.StreamScreenSmall
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.SheetExpanded
import com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded.StreamViewLarge
import com.nielsmasdorp.nederadio.ui.search.SearchScreen
import com.nielsmasdorp.nederadio.ui.search.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val AnimationDurationMs = 50

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
    viewModel: AppViewModel = koinViewModel()
) {

    val systemUiController = rememberSystemUiController()

    val systemBarsColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(systemUiController) {
        systemUiController.setSystemBarsColor(color = systemBarsColor)
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStarted(controls = listOf(smallPlayerControls, largePlayerControls))
        onDispose { viewModel.onStopped() }
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val sheetToggle: () -> Unit = {
        scope.launch {
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        sheetToggle()
    }

    val streams: Streams by viewModel.streams.collectAsState(initial = Streams.Loading)
    val currentFavorites: List<Stream> by viewModel.favorites.collectAsState(initial = emptyList())
    val activeStream: ActiveStream by viewModel.activeStream.collectAsState(initial = ActiveStream.Unknown)
    val sleepTimer: String? by viewModel.sleepTimer.collectAsState(initial = null)
    val showAboutAppDialog: Boolean by viewModel.showAboutApp.collectAsState(initial = false)
    val showSleepTimerDialog: Boolean by viewModel.showSleepTimer.collectAsState(initial = false)
    val currentBottomSheetFraction: Float by animateFloatAsState(
        targetValue = if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) 0f else 1f,
        animationSpec = tween(durationMillis = AnimationDurationMs, easing = FastOutSlowInEasing),
        label = "bottomSheetFractionAnimation"
    )
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
        sheetDragHandle = null,
        sheetContent = {
            SheetContent {
                SheetExpanded(
                    currentFraction = currentBottomSheetFraction,
                    content = {
                        StreamViewLarge(
                            playerControls = largePlayerControls,
                            activeStream = activeStream,
                            sleepTimer = sleepTimer,
                            onCollapseClick = { sheetToggle() },
                            onTimerClicked = viewModel::onTimerPicked,
                            onStreamFavoriteStatusChanged = viewModel::onStreamFavoriteChanged
                        )
                    }
                )
                SheetCollapsed(
                    isEnabled = scaffoldState.bottomSheetState.currentValue ==
                        SheetValue.PartiallyExpanded &&
                        activeStream is ActiveStream.Filled,
                    currentFraction = currentBottomSheetFraction,
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
        sheetPeekHeight = 80.dp +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        sheetSwipeEnabled = false
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            route = "route"
        ) {
            composable(
                "home",
                enterTransition = {
                    slideIntoContainer(
                        towards = SlideDirection.Left,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = SlideDirection.Left,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = SlideDirection.Right,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = SlideDirection.Right,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                }
            ) {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
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
                        towards = SlideDirection.Left,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = SlideDirection.Right,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                }
            ) {
                val searchViewModel = koinViewModel<SearchViewModel>(
                    viewModelStoreOwner = navController.getBackStackEntry("route")
                )
                SearchScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = searchViewModel,
                    onExitSearch = { navController.popBackStack() },
                    backPressHandler = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                        { sheetToggle() }
                    } else null
                )
            }
            composable(
                "equalizer",
                enterTransition = {
                    slideIntoContainer(
                        towards = SlideDirection.Left,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = SlideDirection.Right,
                        animationSpec = tween(delayMillis = AnimationDurationMs)
                    )
                }
            ) {
                val equalizerViewModel = koinViewModel<EqualizerViewModel>(
                    viewModelStoreOwner = navController.getBackStackEntry("route")
                )
                EqualizerScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = equalizerViewModel,
                    onExitEqualizer = { navController.popBackStack() },
                    backPressHandler = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                        { sheetToggle() }
                    } else null
                )
            }
        }
    }
}
