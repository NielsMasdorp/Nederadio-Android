package com.nielsmasdorp.sleeply.ui.stream.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nielsmasdorp.sleeply.domain.stream.Stream
import androidx.compose.ui.graphics.Color

@Composable
fun PickStreamDialog(
    modifier: Modifier = Modifier,
    streams: List<Stream>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier
        ) {
            LazyVerticalGrid(cells = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                itemsIndexed(streams) { index, item ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(128.dp), onClick = { onSelect(index) }) {
                        Image(
                            painterResource(item.smallImgRes),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(Modifier.wrapContentSize(Alignment.Center)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview for Dialogs currently not working in Compose
 */
@Composable
@Preview
fun PickStreamDialogPreview() {
    return SleepTimerDialog(onSelect = { /* NO-OP */ }, onDismiss = { /* NO-OP */ })
}