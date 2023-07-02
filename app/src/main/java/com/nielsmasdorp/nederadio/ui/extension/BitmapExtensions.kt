package com.nielsmasdorp.nederadio.ui.extension

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("MagicNumber")
suspend fun ImageBitmap.edgeColor(): Int = withContext(Dispatchers.IO) {
    val pixels = IntArray(width * height)
    readPixels(pixels)
    pixels.last()
}
