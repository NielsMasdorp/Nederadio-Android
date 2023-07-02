package com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerControlView
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.ActiveStream
import com.nielsmasdorp.nederadio.domain.stream.PlayerControls
import com.nielsmasdorp.nederadio.ui.components.StreamImage
import com.nielsmasdorp.nederadio.ui.extension.setColors

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamScreenSmall(
    activeStream: ActiveStream,
    playerControls: PlayerControls<View>,
    modifier: Modifier = Modifier,
    onStreamFavoriteStatusChanged: (String, Boolean) -> Unit,
) {

    val scope = rememberCoroutineScope()

    val controlColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    when (activeStream) {
        is ActiveStream.Unknown -> return
        is ActiveStream.Empty -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Radio,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.small_player_no_station_chosen),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> {
            val stream = (activeStream as ActiveStream.Filled).stream
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreamImage(
                    modifier = Modifier.size(48.dp),
                    stream = stream,
                    shape = RoundedCornerShape(8.dp),
                    scope = scope
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = stream.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (!stream.track.isNullOrEmpty()) {
                        Text(
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            text = stream.track.orEmpty(),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
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
                        contentDescription = if (stream.isFavorite) {
                            stringResource(id = R.string.player_remove_from_favorites)
                        } else {
                            stringResource(id = R.string.player_add_to_favorites)
                        },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                AndroidView(
                    factory = { playerControls.getView() as PlayerControlView },
                    update = { view ->
                        view.setColors(controlColor)
                    }
                )
            }
        }
    }
}
