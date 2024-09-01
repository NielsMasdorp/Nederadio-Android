package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
interface StreamProvider {

    /**
     * @return a list of [Stream] to be used by the app
     * @param isCurrent whether a [Stream] id belongs to the current stream that is being loaded
     * @param isFavorite whether a [Stream] id belongs to a favorite stream
     */
    suspend fun getStreams(
        isFavorite: suspend (String) -> Boolean,
        isCurrent: suspend (String) -> Boolean
    ): List<Stream>
}
