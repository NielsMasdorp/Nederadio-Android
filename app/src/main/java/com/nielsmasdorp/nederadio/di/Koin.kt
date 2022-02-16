package com.nielsmasdorp.nederadio.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.media3.common.util.UnstableApi
import com.nielsmasdorp.nederadio.data.network.AndroidNetworkManager
import com.nielsmasdorp.nederadio.data.network.StreamApi
import com.nielsmasdorp.nederadio.data.network.ktorHttpClient
import com.nielsmasdorp.nederadio.data.stream.AndroidStreamManager
import com.nielsmasdorp.nederadio.data.stream.ApiStreamRepository
import com.nielsmasdorp.nederadio.data.settings.SharedPreferencesSettingsRepository
import com.nielsmasdorp.nederadio.domain.connectivity.NetworkManager
import com.nielsmasdorp.nederadio.domain.settings.*
import com.nielsmasdorp.nederadio.domain.stream.GetAllStreams
import com.nielsmasdorp.nederadio.domain.stream.StreamManager
import com.nielsmasdorp.nederadio.domain.stream.StreamRepository
import com.nielsmasdorp.nederadio.domain.stream.UpdateStreams
import com.nielsmasdorp.nederadio.ui.AppViewModel
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
    single { GetAllStreams(get()) }
    single { AddToFavorites(get()) }
    single { UpdateStreams(get()) }
    single { RemoveFromFavorites(get()) }
    single<StreamManager> { AndroidStreamManager(get(), get(), get(), get()) }
}

val settingsModule = module {
    single { GetLastPlayedId(get()) }
    single { SetLastPlayedId(get()) }
    single<SettingsRepository> { SharedPreferencesSettingsRepository(androidApplication()) }
}

val networkModule = module {
    single { androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single<NetworkManager> { AndroidNetworkManager(get()) }
    single { StreamApi(client = ktorHttpClient) }
}

val uiModule = module {
    viewModel { AppViewModel(get(), get(), get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
}