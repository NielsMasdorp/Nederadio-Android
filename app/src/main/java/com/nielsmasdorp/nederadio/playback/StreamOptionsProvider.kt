package com.nielsmasdorp.nederadio.playback

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
 */
@UnstableApi
class StreamOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setCastMediaOptions(
                CastMediaOptions.Builder()
                    .setMediaSessionEnabled(false)
                    .setNotificationOptions(null)
                    .build()
            )
            .setResumeSavedSession(false)
            .setEnableReconnectionService(false)
            .setReceiverApplicationId(APP_ID)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider> {
        return emptyList()
    }

    companion object {
        private const val APP_ID =
            "A12D4273" // TODO register cast app https://cast.google.com/publish/#/signup
    }
}