package com.nielsmasdorp.nederadio.data.stream

import android.content.Context
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.data.network.StreamApi
import com.nielsmasdorp.nederadio.domain.connectivity.NetworkManager
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.stream.Streams
import com.nielsmasdorp.nederadio.domain.stream.Failure
import com.nielsmasdorp.nederadio.domain.stream.StreamRepository
import com.nielsmasdorp.nederadio.util.toFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * @author Niels Masdorp (NielsMasdorp)
 * loads streams from API
 */
class ApiStreamRepository(
    private val context: Context,
    private val networkManager: NetworkManager,
    private val streamApi: StreamApi,
    private val settingsRepository: SettingsRepository,
) : StreamRepository {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override val streamsFlow: MutableStateFlow<Streams> =
        MutableStateFlow(Streams.Loading)

    init {
        loadStreams()
        scope.launch {
            settingsRepository.favoritesFlow.collect { favorites ->
                streamsFlow.value = when (val current = streamsFlow.value) {
                    is Streams.Success -> current.copy(streams = current.streams.map { stream ->
                        stream.copy(isFavorite = favorites.contains(stream.id))
                    })
                    else -> streamsFlow.value
                }
            }
        }
    }

    override suspend fun forceUpdate() {
        streamsFlow.value = Streams.Loading
        loadStreams()
    }

    override suspend fun updateTrack(track: String) {
        streamsFlow.value = when (val current = streamsFlow.value) {
            is Streams.Success -> current.copy(streams = current.streams.map { stream ->
                if (stream.isActive) {
                    if (stream.track == track) return
                    stream.copy(track = track)
                } else {
                    stream
                }
            })
            else -> streamsFlow.value
        }
    }

    override suspend fun updateActive(id: String) {
        streamsFlow.value = when (val current = streamsFlow.value) {
            is Streams.Success -> {
                if (current.streams.find { it.isActive }?.id == id) return
                current.copy(streams = current.streams.map { stream ->
                    stream.copy(isActive = stream.id == id)
                })
            }
            else -> streamsFlow.value
        }
    }

    private fun loadStreams() {
        scope.launch {
            if (!networkManager.isConnected()) {
                streamsFlow.value = Streams.Error(
                    Failure.NoNetworkConnection(
                        context.getString(R.string.streams_fetch_error_no_internet)
                    )
                )
            } else {
                try {
                    val streams = streamApi.getStreams()
                        .map {
                            it.toDomain(
                                isCurrent = settingsRepository.getLastPlayedId() == it.id,
                                isFavorite = settingsRepository.isFavorite(id = it.id)
                            )
                        }
                    streamsFlow.value = Streams.Success(streams = streams)
                } catch (ex: Exception) {
                    streamsFlow.value =
                        Streams.Error(failure = ex.toFailure(context.resources))
                }
            }
        }
    }
}