package com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerControlView
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.ActiveStream
import com.nielsmasdorp.nederadio.domain.stream.PlayerControls
import com.nielsmasdorp.nederadio.ui.extension.setColors
import com.skydoves.landscapist.glide.GlideImage

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamViewLarge(
    playerControls: PlayerControls<View>,
    activeStream: ActiveStream,
    sleepTimer: String?,
    onCollapseClick: () -> Unit,
    onTimerClicked: () -> Unit,
    onStreamFavoriteStatusChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeStream is ActiveStream.Filled) { // should not be visible to user when stream is not filled
                val stream = activeStream.stream
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(onClick = { onCollapseClick() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_down),
                        contentDescription = stringResource(id = R.string.settings_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(256.dp)
                ) {
                    GlideImage(
                        imageModel = stream.imageUrl,
                        placeHolder = ImageBitmap.imageResource(R.drawable.empty_background_small),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp),
                    text = stream.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!stream.track.isNullOrEmpty()) {
                    Text(
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        text = stream.track.orEmpty(),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    textAlign = TextAlign.Center,
                    text = sleepTimer.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.weight(1.0f))
                IconButton(onClick = {
                    onStreamFavoriteStatusChanged(
                        stream.id,
                        !stream.isFavorite
                    )
                }) {
                    Icon(
                        imageVector = if (stream.isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        modifier = Modifier.size(36.dp),
                        contentDescription = if (stream.isFavorite) {
                            stringResource(id = R.string.player_remove_from_favorites)
                        } else {
                            stringResource(id = R.string.player_add_to_favorites)
                        },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val playPauseColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            factory = { playerControls.getView() as PlayerControlView },
                            modifier = Modifier.fillMaxWidth(),
                            update = { view ->
                                view.setColors(playPauseColor)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onTimerClicked() }) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        text = stringResource(id = R.string.sleep_timer_button),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
