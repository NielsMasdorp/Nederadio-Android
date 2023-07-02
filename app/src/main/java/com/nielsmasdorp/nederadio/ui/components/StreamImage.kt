package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.ui.extension.edgeColor
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val AspectRatio = 1.31f

@Composable
fun StreamImage(
    stream: Stream,
    shape: Shape,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    onSelectStream: ((String) -> Unit)? = null
) {
    val clickableModifier = if (onSelectStream != null) {
        modifier
            .aspectRatio(ratio = AspectRatio)
            .clip(shape)
            .clickable { onSelectStream.invoke(stream.id) }
    } else {
        modifier
            .aspectRatio(ratio = AspectRatio)
            .clip(shape)
    }
    Card(modifier = clickableModifier) {
        var edgeColor by remember { mutableStateOf(Color.White.toArgb()) }
        GlideImage(
            imageModel = { stream.imageUrl },
            success = { success, painter ->
                LaunchedEffect(Unit) {
                    scope.launch {
                        success.imageBitmap?.let { edgeColor = it.edgeColor() }
                    }
                }
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(color = edgeColor)),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
        )
    }
}
