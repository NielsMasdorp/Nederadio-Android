package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.*

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving all successful streams of this app
 * @param getAllStreams stream use case
 */
class GetSuccessfulStreams(getAllStreams: GetAllStreams) {

    val streams: Flow<List<Stream>> = getAllStreams.streams
        .filter { it is Streams.Success }
        .map { it as Streams.Success }
        .map { it.streams }
}
