package com.nielsmasdorp.sleeply.ui.stream.components

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

/**
 * Legacy text view since compose does not support HTML yet
 */
@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    html: String
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}

@Preview
@Composable
fun HtmlTextPreview() {
    return HtmlText(html = "This is a <b>html</b> text")
}