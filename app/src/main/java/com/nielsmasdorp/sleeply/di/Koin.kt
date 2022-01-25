package com.nielsmasdorp.sleeply.di

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import com.nielsmasdorp.sleeply.data.network.AndroidNetworkManager
import com.nielsmasdorp.sleeply.data.stream.AndroidStreamManager
import com.nielsmasdorp.sleeply.data.stream.MemoryStreamRepository
import com.nielsmasdorp.sleeply.data.settings.SharedPreferencesSettingsRepository
import com.nielsmasdorp.sleeply.domain.connectivity.NetworkManager
import com.nielsmasdorp.sleeply.domain.settings.*
import com.nielsmasdorp.sleeply.domain.stream.GetAllStreams
import com.nielsmasdorp.sleeply.domain.stream.GetStreamById
import com.nielsmasdorp.sleeply.domain.stream.StreamManager
import com.nielsmasdorp.sleeply.domain.stream.StreamRepository
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnsafeOptInUsageError")
val streamModule = module {
    single<StreamRepository> { MemoryStreamRepository(androidApplication()) }
    single { GetStreamById(get()) }
    single { GetAllStreams(get()) }
    single<StreamManager> { AndroidStreamManager(get(), get(), get(), get()) }
}

val settingsModule = module {
    single { GetLastPlayedIndex(get()) }
    single { SetLastPlayedIndex(get()) }
    single { GetStreamOnNetworkEnabled(get()) }
    single { SetStreamOnNetworkEnabled(get()) }
    single<SettingsRepository> { SharedPreferencesSettingsRepository(androidApplication()) }
}

val networkModule = module {
    single { androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single<NetworkManager> { AndroidNetworkManager(get()) }
}

val uiModule = module {
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
}