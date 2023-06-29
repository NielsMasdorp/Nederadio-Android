package com.nielsmasdorp.nederadio.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.data.equalizer.AndroidEqualizerManager
import com.nielsmasdorp.nederadio.data.network.AndroidNetworkManager
import com.nielsmasdorp.nederadio.data.network.StreamApi
import com.nielsmasdorp.nederadio.data.network.ktorHttpClient
import com.nielsmasdorp.nederadio.data.settings.SharedPreferencesSettingsRepository
import com.nielsmasdorp.nederadio.data.stream.AndroidStreamManager
import com.nielsmasdorp.nederadio.data.stream.ApiStreamRepository
import com.nielsmasdorp.nederadio.domain.connectivity.NetworkManager
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerManager
import com.nielsmasdorp.nederadio.domain.equalizer.GetEqualizerSettings
import com.nielsmasdorp.nederadio.domain.equalizer.SetEqualizerSettings
import com.nielsmasdorp.nederadio.domain.settings.*
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.playback.library.StreamLibrary
import com.nielsmasdorp.nederadio.ui.AppViewModel
import com.nielsmasdorp.nederadio.ui.equalizer.EqualizerViewModel
import com.nielsmasdorp.nederadio.ui.search.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@UnstableApi
val streamModule = module {
    single<StreamRepository> { ApiStreamRepository(androidContext(), get(), get(), get()) }
    single { GetAllStreams(repository = get()) }
    single { GetSuccessfulStreams(getAllStreams = get()) }
    single { GetActiveStream(repository = get()) }
    single { AddToFavorites(repository = get()) }
    single { UpdateStreams(repository = get()) }
    single { SetStreamTrack(repository = get()) }
    single { SetActiveStream(repository = get(), setLastPlayedId = get()) }
    single { RemoveFromFavorites(repository = get()) }
    single { StreamLibrary(context = androidContext(), getSuccessfulStreams = get()) }
    single<StreamManager> {
        AndroidStreamManager(
            context = get(),
            setActiveStream = get(),
            setStreamTrack = get(),
            streamLibrary = get()
        )
    }
}

val settingsModule = module {
    single { GetLastPlayedId(repository = get()) }
    single { SetLastPlayedId(repository = get()) }
    single<SettingsRepository> { SharedPreferencesSettingsRepository(context = androidApplication()) }
}

val equalizerModule = module {
    single { GetEqualizerSettings(repository = get()) }
    single { SetEqualizerSettings(repository = get()) }
    single<EqualizerManager> {
        AndroidEqualizerManager(
            getEqualizerSettings = get(),
            setEqualizerSettings = get()
        )
    }
}

val networkModule = module {
    single { androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single<NetworkManager> { AndroidNetworkManager(connectivityManager = get()) }
    single { StreamApi(client = ktorHttpClient) }
}

val uiModule = module {
    viewModel {
        AppViewModel(
            getAllStreams = get(),
            getActiveStream = get(),
            addToFavorites = get(),
            removeFromFavorites = get(),
            updateStreams = get(),
            streamManager = get()
        )
    }
    viewModel { SearchViewModel(getAllStreams = get(), streamManager = get()) }
    viewModel { EqualizerViewModel(equalizerManager = get()) }
}
