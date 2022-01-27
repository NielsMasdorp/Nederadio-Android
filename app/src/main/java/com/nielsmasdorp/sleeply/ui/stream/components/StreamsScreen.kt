package com.nielsmasdorp.sleeply.ui.stream.components

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel.Companion.EMPTY_ERROR
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel.Event.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.sleeply.domain.stream.StreamingError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@UnstableApi
@Composable
fun StreamsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    playerControls: PlayerControls<View>
) {

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val viewData = viewModel.viewData.observeAsState()
    val sleepTimer = viewModel.sleepTimer.observeAsState()

    CheckError(coroutineScope = coroutineScope, scaffoldState = scaffoldState)
    CheckEvent()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier, scaffoldState = scaffoldState) {
        BoxWithConstraints {
            if (maxWidth < maxHeight) {
                CurrentStreamViewPortrait(
                    modifier = modifier,
                    playerControls = playerControls,
                    viewData = viewData,
                    sleepTimer = sleepTimer
                )
            } else {
                CurrentStreamViewLand(
                    modifier = modifier,
                    playerControls = playerControls,
                    viewData = viewData,
                    sleepTimer = sleepTimer
                )
            }
        }
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            title = {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(id = R.string.app_name),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.settings_content_description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {

                    DropdownMenuItem(onClick = {
                        viewModel.onEmailDeveloperPicked()
                        showMenu = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.action_email_dev),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.onAboutPicked()
                        showMenu = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.action_about),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun CheckEvent(viewModel: MainViewModel = viewModel()) {
    val state = viewModel.eventsFlow.collectAsState(initial = Empty)
    when (state.value) {
        ShowAbout -> AboutAppDialog(
            onDismiss = { viewModel.onAboutDismissed() }
        )
        is EmailDeveloper -> EmailIntent(
            mailTo = stringResource(R.string.dev_email_address),
            subject = stringResource(R.string.email_subject)
        )
        is ShowTimer -> SleepTimerDialog(onSelect = {
            viewModel.setSleepTimer(it)
            viewModel.onTimerDismissed()
        }, onDismiss = {
            viewModel.onTimerDismissed()
        })
        is ShowStreams -> PickStreamDialog(
            streams = (state.value as ShowStreams).streams,
            onSelect = {
                viewModel.onStreamsDismissed()
                viewModel.onStreamPicked(it)
            },
            onDismiss = { viewModel.onStreamsDismissed() })
        Empty -> {}
    }
}

@Composable
fun CheckError(
    viewModel: MainViewModel = viewModel(),
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    val error = viewModel.errorFlow.collectAsState(initial = EMPTY_ERROR)
    error.value.let { errorVal ->
        if (errorVal is StreamingError.Filled) {
            viewModel.onErrorShown()
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(message = errorVal.error)
            }
        }
    }
}