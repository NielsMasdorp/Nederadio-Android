package com.nielsmasdorp.sleeply.ui.stream.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.google.accompanist.placeholder.placeholder
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls
import com.nielsmasdorp.sleeply.domain.stream.Stream
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel
import com.nielsmasdorp.sleeply.ui.stream.SleeplyPlayerControlsView

@UnstableApi
@Composable
fun CurrentStreamView(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    playerControls: PlayerControls,
    viewData: State<Stream?>,
    sleepTimer: State<String?>
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.BottomStart)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(256.dp)
            ) {
                Crossfade(
                    targetState = viewData.value,
                    animationSpec = tween(750),
                    modifier = Modifier
                        .placeholder(
                            visible = viewData.value == null,
                            color = MaterialTheme.colorScheme.primary
                        )
                        .size(128.dp)
                ) { data ->
                    Image(
                        painterResource(
                            data?.smallImgRes ?: R.drawable.empty_background_small
                        ),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()

                    )
                }
            }
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp),
                text = viewData.value?.title ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                modifier = Modifier
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                text = viewData.value?.desc ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Thin,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val controlColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
                val playPauseColor = MaterialTheme.colorScheme.onPrimary.toArgb()
                Box(
                    modifier = Modifier
                        .padding(top = 72.dp)
                        .size(72.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                AndroidView(
                    factory = {
                        playerControls.getView() as SleeplyPlayerControlsView
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    update = { view ->
                        view.setColors(playPauseColor, controlColor)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = sleepTimer.value ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { viewModel.onTimerPicked() }) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        text = stringResource(id = R.string.sleep_timer_button),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TextButton(onClick = { viewModel.onPickStreams() }) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        text = stringResource(id = R.string.all_streams_button),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}