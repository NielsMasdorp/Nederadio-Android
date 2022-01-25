package com.nielsmasdorp.sleeply.ui.stream.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nielsmasdorp.sleeply.R

@Composable
fun AboutAppDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(
                color = MaterialTheme.colorScheme.onSurface,
                text = stringResource(id = R.string.about_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            HtmlText(html = stringResource(id = R.string.about_message))
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onDismiss() }
                ) {
                    Text("Close")
                }
            }
        }
    )
}

/**
 * Preview for Dialogs currently not working in Compose
 */
@Composable
@Preview
fun AboutAppDialogPreview() {
    return AboutAppDialog { /* NO-OP */ }
}