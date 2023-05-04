package com.nielsmasdorp.nederadio.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nielsmasdorp.nederadio.R

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
@Suppress("ModifierReused")
fun SleepTimerDialog(
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val options = stringArrayResource(id = R.array.sleep_timer)

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier,
        ) {
            Column(modifier) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.sleep_timer_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    itemsIndexed(options) { index, item ->
                        Row(
                            modifier = Modifier
                                .clickable(onClick = { onSelect(index) })
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                text = item,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview for Dialogs currently not working in Compose
 */
@Composable
@Preview
fun SleepTimerDialogPreview() {
    return SleepTimerDialog(onSelect = { /* NO-OP */ }, onDismiss = { /* NO-OP */ })
}
