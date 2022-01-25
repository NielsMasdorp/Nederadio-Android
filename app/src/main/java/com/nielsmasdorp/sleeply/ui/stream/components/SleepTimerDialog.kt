package com.nielsmasdorp.sleeply.ui.stream.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nielsmasdorp.sleeply.R

@Composable
fun SleepTimerDialog(
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = stringArrayResource(id = R.array.sleep_timer)

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            color = Color.DarkGray,
            modifier = modifier,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.sleep_timer_title),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
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
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                text = item,
                                color = Color.White,
                                fontSize = 16.sp
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