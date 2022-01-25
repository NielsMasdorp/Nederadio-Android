package com.nielsmasdorp.sleeply.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Repository for [Stream] data
 */
interface StreamRepository {

    /**
     * @return [Stream] by id
     * @param id of the stream
     */
    suspend fun getStreamById(id: String): Stream

    /**
     * @return all streams in the app
     */
    suspend fun getStreams(): List<Stream>
}