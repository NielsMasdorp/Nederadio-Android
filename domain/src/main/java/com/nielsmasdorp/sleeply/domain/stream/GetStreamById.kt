package com.nielsmasdorp.sleeply.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving a stream by id
 * @param repository stream repository
 */
class GetStreamById(private val repository: StreamRepository) {

    suspend operator fun invoke(id: String): Stream = repository.getStreamById(id)
}