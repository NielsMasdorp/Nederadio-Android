package com.nielsmasdorp.nederadio.ui.home.bottomsheet

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun SheetContent(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        content()
    }
}
