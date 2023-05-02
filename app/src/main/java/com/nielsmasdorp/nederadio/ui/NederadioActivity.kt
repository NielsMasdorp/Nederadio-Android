package com.nielsmasdorp.nederadio.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.nielsmasdorp.nederadio.ui.theme.AppTheme

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
                NederadioApp(
                    modifier = Modifier.fillMaxSize(),
                    smallPlayerControls = controlViews[0],
                    largePlayerControls = controlViews[1],
                    castButton = castButton
                )
            }
        }
    }
}