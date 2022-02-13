package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.Flow

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Repository for [Stream] data
 */
interface StreamRepository {

    /**
     * Flow of all streams playable by the app
     */
    val streamsFlow: Flow<CurrentStreams>

    /**
     * Update streams
     */
    fun forceUpdate()
}