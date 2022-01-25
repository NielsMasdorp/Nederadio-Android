package com.nielsmasdorp.sleeply.ui.stream.components

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nielsmasdorp.sleeply.R

@Composable
fun EmailIntent(
    mailTo: String,
    subject: String
) {
    val requestIntent = Intent(
        Intent.ACTION_SENDTO, Uri.fromParts(
            stringResource(R.string.email_intent_type),
            mailTo,
            null
        )
    )
    requestIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    LocalContext.current.startActivity(
        Intent.createChooser(
            requestIntent,
            stringResource(R.string.email_intent_title)
        )
    )
}