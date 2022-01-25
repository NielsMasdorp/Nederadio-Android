package com.nielsmasdorp.sleeply.ui.stream

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.accompanist.insets.ProvideWindowInsets
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls
import com.nielsmasdorp.sleeply.ui.stream.components.StreamsScreen
import com.nielsmasdorp.sleeply.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnsafeOptInUsageError")
class StreamActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerControls: PlayerControls = SleeplyPlayerControlsView(this)
        setContent {
            AppTheme {
                ProvideWindowInsets {
                    StreamsScreen(playerControls = playerControls)
                }
            }
        }
        viewModel.onStarted(playerControls)
    }

    override fun onDestroy() {
        viewModel.onStopped()
        super.onDestroy()
    }
}