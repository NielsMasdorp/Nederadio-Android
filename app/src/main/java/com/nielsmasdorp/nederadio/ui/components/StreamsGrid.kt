package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.skydoves.landscapist.glide.GlideImage

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun StreamsGrid(
    modifier: Modifier = Modifier,
    streams: List<Stream>,
    onSelectStream: (String) -> Unit = {}
) {

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 72.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (streams.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyView(
                    title = stringResource(id = R.string.streams_overview_no_streams_found),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            LazyVerticalGrid(
                cells = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(streams.size) { index ->
                    val item = streams[index]
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .clickable(onClick = { onSelectStream(item.id) })
                    ) {
                        GlideImage(
                            imageModel = item.imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
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
                id = "1",
                title = "title",
                url = "www.google.com",
                imageUrl = "www.google.com",
                imageBytes = byteArrayOf(),
                isFavorite = false
            )
        )
    )
}