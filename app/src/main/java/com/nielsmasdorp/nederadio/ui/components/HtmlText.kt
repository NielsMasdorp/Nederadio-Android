package com.nielsmasdorp.nederadio.ui.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Legacy text view since compose does not support HTML yet
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            it.apply {
                text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
                movementMethod = LinkMovementMethod.getInstance()
                setLinkTextColor(color.toArgb())
                setTextColor(color.toArgb())
            }
        }
    )
}

@Preview
@Composable
fun HtmlTextPreview() {
    return HtmlText(html = "This is a <b>html</b> text")
}
