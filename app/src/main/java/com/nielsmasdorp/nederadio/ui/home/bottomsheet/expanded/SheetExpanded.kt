package com.nielsmasdorp.nederadio.ui.home.bottomsheet.expanded

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun SheetExpanded(
    currentFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 1f - currentFraction)
    ) {
        content()
    }
}
