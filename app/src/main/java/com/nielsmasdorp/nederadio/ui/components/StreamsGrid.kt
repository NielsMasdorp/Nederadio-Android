package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.Stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamsGrid(
    streams: List<Stream>,
    modifier: Modifier = Modifier,
    onSelectStream: (String) -> Unit = {}
) {

    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (streams.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyView(
                    title = stringResource(id = R.string.streams_overview_no_streams_found),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(streams.size) { index ->
                    val stream = streams[index]
                    StreamImage(
                        modifier = Modifier.fillMaxSize(),
                        stream = stream,
                        shape = RoundedCornerShape(12.dp),
                        scope = scope,
                        onSelectStream = onSelectStream
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun StreamsGridPreview() {
    StreamsGrid(
        streams = listOf(
            Stream(
                isActive = true,
                id = "1",
                title = "title",
                url = "www.google.com",
                imageUrl = "www.google.com",
                isFavorite = false
            )
        )
    )
}
