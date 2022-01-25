package com.nielsmasdorp.sleeply.ui.stream.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.placeholder.placeholder
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls
import com.nielsmasdorp.sleeply.domain.stream.Stream
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel

@Composable
fun CurrentStreamView(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    playerControls: PlayerControls,
    viewData: State<Stream?>,
    sleepTimer: State<String?>
) {
    Surface(modifier = modifier) {
        Crossfade(
            targetState = viewData.value,
            animationSpec = tween(750),
            modifier = Modifier
                .placeholder(
                    visible = viewData.value == null,
                    color = Color.LightGray
                )
                .fillMaxSize()
        ) { data ->
            Image(
                painterResource(data?.bigImgRes ?: R.drawable.empty_background),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.BottomStart)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(256.dp)
            ) {
                Crossfade(
                    targetState = viewData.value,
                    animationSpec = tween(750),
                    modifier = Modifier
                        .placeholder(
                            visible = viewData.value == null,
                            color = Color.DarkGray
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
                fontWeight = FontWeight.Light,
                color = Color.White,
                fontSize = 32.sp
            )
            Text(
                modifier = Modifier
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light,
                text = viewData.value?.desc ?: "",
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(64.dp))
            Text(
                textAlign = TextAlign.Center,
                text = sleepTimer.value ?: "",
                fontWeight = FontWeight.Light,
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(24.dp))
            AndroidView(
                factory = { playerControls.getView() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { viewModel.onTimerPicked() }) {
                    Text(
                        color = Color.White,
                        text = "sleep timer",
                        fontSize = 16.sp
                    )
                }
                TextButton(onClick = { viewModel.onPickStreams() }) {
                    Text(
                        color = Color.White,
                        text = "all streams",
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}