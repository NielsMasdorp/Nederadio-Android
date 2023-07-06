package com.nielsmasdorp.nederadio.ui.home

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.nielsmasdorp.nederadio.R

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun TopBar(
    castButton: View,
    showCastButton: Boolean,
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit = {},
    onEqualizerClicked: () -> Unit = {},
    onAboutClicked: () -> Unit = {},
) {

    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.flag_nl),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    text = stringResource(id = R.string.app_name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            if (showCastButton) AndroidView(factory = { castButton })
            IconButton(onClick = onSearchClicked) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.search_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.settings_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onEqualizerClicked()
                        showMenu = false
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.action_equalizer),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        onAboutClicked()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.action_about),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun TopBarPreview() =
    TopBar(castButton = MediaRouteButton(LocalContext.current), showCastButton = true)
