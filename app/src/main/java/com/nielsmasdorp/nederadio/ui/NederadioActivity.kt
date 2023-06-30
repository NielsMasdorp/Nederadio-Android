package com.nielsmasdorp.nederadio.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.nielsmasdorp.nederadio.ui.theme.AppTheme

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class NederadioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeStatusBarTransparent()

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

    @Suppress("DEPRECATION")
    fun Activity.makeStatusBarTransparent() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }
}
