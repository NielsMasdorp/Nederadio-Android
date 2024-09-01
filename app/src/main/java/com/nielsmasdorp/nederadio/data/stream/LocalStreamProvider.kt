package com.nielsmasdorp.nederadio.data.stream

import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.domain.stream.StreamProvider

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Suppress("Unused")
class LocalStreamProvider : StreamProvider {

    override suspend fun getStreams(
        isFavorite: suspend (String) -> Boolean,
        isCurrent: suspend (String) -> Boolean
    ): List<Stream> {
        // one could use this to provide a local list of streams if no backend needs to be used
        return listOf()
    }
}
