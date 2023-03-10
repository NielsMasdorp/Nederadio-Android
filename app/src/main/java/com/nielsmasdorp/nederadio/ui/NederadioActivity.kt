package com.nielsmasdorp.nederadio.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.nielsmasdorp.nederadio.di.networkModule
import com.nielsmasdorp.nederadio.di.settingsModule
import com.nielsmasdorp.nederadio.di.streamModule
import com.nielsmasdorp.nederadio.di.uiModule
import com.nielsmasdorp.nederadio.ui.theme.AppTheme
import dev.burnoo.cokoin.Koin
import org.koin.android.ext.koin.androidContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class NederadioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Cast button setup
        val castButton = MediaRouteButton(this)
        CastButtonFactory.setUpMediaRouteButton(this, castButton)

        // Media playback controls setup
        val controlViews = PlayerControlsView.createViews(
            layoutInflater = layoutInflater
        )
        setContent {
            AppTheme {
                Koin(appDeclaration = {
                    androidContext(applicationContext)
                    modules(
                        streamModule,
                        settingsModule,
                        networkModule,
                        uiModule
                    )
                }) {
                    NederadioApp(
                        modifier = Modifier
                            .fillMaxSize(),
                        smallPlayerControls = controlViews[0],
                        largePlayerControls = controlViews[1],
                        castButton = castButton
                    )
                }
            }
        }
    }
}