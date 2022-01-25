package com.nielsmasdorp.sleeply.ui.stream.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel.Companion.EMPTY_ERROR
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel.Event.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nielsmasdorp.sleeply.domain.stream.StreamingError

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun StreamsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    playerControls: PlayerControls
) {

    val viewData = viewModel.viewData.observeAsState()
    val sleepTimer = viewModel.sleepTimer.observeAsState()

    CheckError()
    CheckEvent()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier) {
        CurrentStreamView(
            modifier = modifier,
            playerControls = playerControls,
            viewData = viewData,
            sleepTimer = sleepTimer
        )
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            title = { Text(stringResource(id = R.string.app_name), color = Color.White) },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(onClick = {
                        viewModel.onEmailDeveloperPicked()
                        showMenu = false
                    }) {
                        Text("Email developer")
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.onAboutPicked()
                        showMenu = false
                    }) {
                        Text("About")
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
fun CheckError(viewModel: MainViewModel = viewModel()) {
    val error = viewModel.errorFlow.collectAsState(initial = EMPTY_ERROR)
    if (error.value is StreamingError.Filled) {
        viewModel.onErrorShown()
        Toast.makeText(
            LocalContext.current,
            (error.value as StreamingError.Filled).error,
            Toast.LENGTH_SHORT
        ).show()
    }
}