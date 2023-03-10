package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving the active stream
 * @param repository stream repository
 */
class GetActiveStream(private val repository: StreamRepository) {

    val stream: Flow<ActiveStream> = repository.streamsFlow
        .map { streams ->
            if (streams is Streams.Success) {
                streams.streams.find { it.isActive }?.let { stream ->
                    ActiveStream.Filled(stream = stream)
                } ?: ActiveStream.Empty
            } else {
                ActiveStream.Unknown
            }
        }
}