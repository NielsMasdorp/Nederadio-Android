package com.nielsmasdorp.nederadio.ui.home.bottomsheet.collapsed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    activeStream: ActiveStream,
    isEnabled: Boolean,
    currentFraction: Float,
    onSheetClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.(ActiveStream) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = currentFraction)
            .height(80.dp)
            .clickable(
                onClick = onSheetClick,
                enabled = isEnabled
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content(activeStream)
    }
}
