package com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nielsmasdorp.nederadio.domain.stream.ActiveStream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun SheetCollapsed(
    modifier: Modifier = Modifier,
    activeStream: ActiveStream,
    isEnabled: Boolean,
    currentFraction: Float,
    onSheetClick: () -> Unit,
    content: @Composable RowScope.(ActiveStream) -> Unit
) {
    val background = if (activeStream is ActiveStream.Unknown) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = 1f - currentFraction)
            .height(72.dp)
            .background(background)
            .clickable(
                onClick = onSheetClick,
                enabled = isEnabled
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content(activeStream)
    }
}