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
    val streamsFlow: Flow<Streams>

    /**
     * Update streams
     */
    suspend fun forceUpdate()

    /**
     * Update track for current stream
     * @param track the new track
     */
    suspend fun updateTrack(track: String)

    /**
     * Update the active stream
     * @param id the id of the new active stream
     */
    suspend fun updateActive(id: String)
}
