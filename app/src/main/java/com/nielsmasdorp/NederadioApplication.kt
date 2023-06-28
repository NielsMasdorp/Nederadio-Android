package com.nielsmasdorp

import android.app.Application
import com.nielsmasdorp.nederadio.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class NederadioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NederadioApplication)
            modules(
                streamModule,
                settingsModule,
                networkModule,
                equalizerModule,
                uiModule
            )
        }
    }
}
