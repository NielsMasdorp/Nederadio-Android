package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.Flow

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving all streams of this app
 * @param repository stream repository
 */
class GetAllStreams(private val repository: StreamRepository) {

    val streams: Flow<CurrentStreams> = repository.streamsFlow
}