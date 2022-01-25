package com.nielsmasdorp.sleeply.ui.stream.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.padding
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

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun StreamsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    playerControls: PlayerControls
) {

    val viewData = viewModel.viewData.observeAsState()
    val sleepTimer = viewModel.sleepTimer.observeAsState()
    val networkEnabled = viewModel.networkEnabled.observeAsState()
    val events = viewModel.eventsFlow.collectAsState(initial = Empty)
    val errors = viewModel.errorFlow.collectAsState(initial = EMPTY_ERROR)

    CheckError(errors.value)
    CheckEvent(events.value)

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
                        viewModel.setPlayOnNetworkEnabled(
                            !(networkEnabled.value ?: true)
                        )
                    }) {
                        // TODO color
                        Text("Stream on WiFi only")
                        Checkbox(
                            checked = networkEnabled.value ?: true,
                            onCheckedChange = { viewModel.setPlayOnNetworkEnabled(it) }
                        )
                    }
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
fun CheckEvent(
    value: MainViewModel.Event,
    viewModel: MainViewModel = viewModel()
) {
    when (value) {
        ShowAbout -> AboutAppDialog(
            onDismiss = { viewModel.onAboutDismissed() }
        )
        is EmailDeveloper -> EmailIntent(
            mailTo = stringResource(R.string.dev_email_address),
            subject = stringResource(R.string.email_subject)
        )
        is ShowTimer -> SleepTimerDialog(onSelect = {
            viewModel.setSleepTimer(it)
            viewModel.onAboutDismissed()
        }, onDismiss = {
            viewModel.onAboutDismissed()
        })
        is ShowStreams -> PickStreamDialog(
            streams = value.streams,
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
    error: String?,
    viewModel: MainViewModel = viewModel(),
) {
    if (!error.isNullOrEmpty()) {
        viewModel.onErrorShown()
        Toast.makeText(
            LocalContext.current,
            error,
            Toast.LENGTH_SHORT
        ).show()
    }
}