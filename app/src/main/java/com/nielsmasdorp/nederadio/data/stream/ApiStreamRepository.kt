package com.nielsmasdorp.nederadio.data.stream

import android.content.Context
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.data.network.StreamApi
import com.nielsmasdorp.nederadio.domain.connectivity.NetworkManager
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.stream.CurrentStreams
import com.nielsmasdorp.nederadio.domain.stream.Failure
import com.nielsmasdorp.nederadio.domain.stream.StreamRepository
import com.nielsmasdorp.nederadio.util.toFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author Niels Masdorp (NielsMasdorp)
 * loads streams from API
 */
class ApiStreamRepository(
    private val context: Context,
    private val networkManager: NetworkManager,
    private val streamApi: StreamApi,
    private val settingsRepository: SettingsRepository
) : StreamRepository {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override val streamsFlow: MutableStateFlow<CurrentStreams> =
        MutableStateFlow(CurrentStreams.Loading)

    init {
        loadStreams()
    }

    override fun forceUpdate() {
        streamsFlow.value = CurrentStreams.Loading
        loadStreams()
    }

    private fun loadStreams() {
        scope.launch {
            if (!networkManager.isConnected()) {
                streamsFlow.value = CurrentStreams.Error(
                    Failure.NoNetworkConnection(
                        context.getString(R.string.streams_fetch_error_no_internet)
                    )
                )
            } else {
                try {
                    val streams = streamApi.getStreams().map { it.toDomain() }
                    settingsRepository.favoritesFlow.collectLatest { favorites ->
                        streamsFlow.value = CurrentStreams.Success(streams = streams.map { stream ->
                            stream.copy(isFavorite = favorites.contains(stream.id))
                        })
                    }
                } catch (ex: Exception) {
                    streamsFlow.value =
                        CurrentStreams.Error(failure = ex.toFailure(context.resources))
                }
            }
        }
    }
}