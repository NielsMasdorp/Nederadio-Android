package com.nielsmasdorp.nederadio.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.accompanist.insets.ProvideWindowInsets
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

    private val controlViews by lazy {
        PlayerControlsView.createViews(layoutInflater = layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ProvideWindowInsets {
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
                            smallPlayerControls = controlViews[0],
                            largePlayerControls = controlViews[1],
                        )
                    }
                }
            }
        }
    }
}