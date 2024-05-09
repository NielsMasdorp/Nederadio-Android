package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerState
import org.koin.androidx.compose.koinViewModel

@Composable
fun EqualizerScreen(
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = koinViewModel(),
    onExitEqualizer: () -> Unit = {},
    backPressHandler: (() -> Unit)? = null
) {

    val state: EqualizerState by viewModel.equalizerState.collectAsState()

    BackHandler(enabled = backPressHandler != null) {
        backPressHandler?.invoke()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = stringResource(id = R.string.action_equalizer),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                navigationIcon = {
                    IconButton(onClick = onExitEqualizer) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = state.isEnabled,
                        enabled = state.canBeEnabled,
                        onCheckedChange = viewModel::setEnabled,
                        colors = SwitchDefaults.colors(
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            checkedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedThumbColor = MaterialTheme.colorScheme.onSurface
                        ),
                        thumbContent = if (state.isEnabled) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.surface
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(bottom = 80.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (val data = state) {
                    is EqualizerState.Filled -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Equalizer(
                                modifier = Modifier.fillMaxWidth(),
                                equalizerProducer = viewModel.equalizerProducer,
                                minBandRange = data.min.toFloat(),
                                maxBandRange = data.max.toFloat()
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                itemsIndexed(data.presets.presets) { index, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setPreset(index.toShort()) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item,
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (data.presets.currentPreset == index.toShort()) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = stringResource(
                                                    id = R.string.equalizer_selected_preset_descr
                                                ),
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is EqualizerState.NotAvailableWhileCasting -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.equalizer_not_available),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.equalizer_loading),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    )
}
