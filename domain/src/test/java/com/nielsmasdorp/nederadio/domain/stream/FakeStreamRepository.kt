package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake [StreamRepository] implementation
 *
 * @author Niels Masdorp (NielsMasdorp)
 */
class FakeStreamRepository(private var streams: List<Streams>) : StreamRepository {

    private var update = false

    override val streamsFlow: Flow<Streams>
        get() = flow {
            for (item in streams) {
                emit(item)
            }
            while (!update) {
                delay(100L)
            }
            for (item in streams) {
                emit(item)
            }
            update = false
        }

    override suspend fun forceUpdate() {
        update = true
    }

    override suspend fun updateTrack(track: String) {
        when (val current = streams.first()) {
            is Streams.Success -> {
                streams = listOf(current.copy(
                    streams = current.streams.map { stream ->
                        if (stream.isActive) {
                            stream.copy(track = track)
                        } else stream

                    }
                ))
            }
            else -> {}
        }
        update = true
    }

    override suspend fun updateActive(id: String) {
        when (val current = streams.first()) {
            is Streams.Success -> {
                streams = listOf(current.copy(
                    streams = current.streams.map { stream ->
                        stream.copy(isActive = stream.id == id)
                    }
                ))
            }
            else -> {}
        }
        update = true
    }
}