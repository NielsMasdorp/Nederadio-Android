package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun EmptyView(
    title: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = tint
        )
    }
}

@Composable
@Preview
fun EmptyViewPreview() {
    EmptyView(
        title = "Empty View",
        tint = MaterialTheme.colorScheme.onPrimary
    )
}
