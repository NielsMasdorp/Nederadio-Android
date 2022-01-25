package com.nielsmasdorp.sleeply

import android.app.Application
import com.nielsmasdorp.sleeply.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SleeplyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SleeplyApplication)
            modules(streamModule, settingsModule, networkModule, uiModule)
        }
    }
}