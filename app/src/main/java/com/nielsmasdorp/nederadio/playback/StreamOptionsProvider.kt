package com.nielsmasdorp.nederadio.playback

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

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
        return CastOptions.Builder()
            .setReceiverApplicationId(APP_ID)
            .setCastMediaOptions(
                CastMediaOptions.Builder()
                    .setMediaSessionEnabled(false)
                    .setNotificationOptions(null)
                    .build()
            )
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider> {
        return mutableListOf()
    }

    companion object {
        private const val APP_ID = "A12D4273"
    }
}
