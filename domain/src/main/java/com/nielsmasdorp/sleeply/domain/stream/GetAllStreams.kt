package com.nielsmasdorp.sleeply.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving all streams of this app
 * @param repository stream repository
 */
class GetAllStreams(private val repository: StreamRepository) {

    suspend operator fun invoke(): List<Stream> = repository.getStreams()
}