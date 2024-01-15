package com.nielsmasdorp.nederadio.playback

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.nielsmasdorp.nederadio.ui.NederadioActivity

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Custom implementation which provides our own cast app ID
 *
 * This is the only way to show the app name while casting and not "Exoplayer Default Receiver"
 *
 * used in Manifest
 */

@UnstableApi
@Suppress("unused")
class StreamOptionsProvider : OptionsProvider {

    @SuppressLint("VisibleForTests")
    override fun getCastOptions(context: Context): CastOptions {
        val buttonActions = listOf(
            MediaIntentReceiver.ACTION_STOP_CASTING,
            MediaIntentReceiver.ACTION_SKIP_PREV,
            MediaIntentReceiver.ACTION_SKIP_NEXT
        )
        val compatButtonActionsIndices = intArrayOf(1, 2)
        val notificationOptions = NotificationOptions.Builder()
            .setActions(buttonActions, compatButtonActionsIndices)
            .setTargetActivityClassName(NederadioActivity::class.java.name)
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(APP_ID)
            .setCastMediaOptions(
                CastMediaOptions.Builder()
                    .setNotificationOptions(notificationOptions)
                    .setMediaSessionEnabled(false)
                    .build()
            )
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider> {
        return emptyList()
    }

    companion object {
        private const val APP_ID =
            "A12D4273"
    }
}
