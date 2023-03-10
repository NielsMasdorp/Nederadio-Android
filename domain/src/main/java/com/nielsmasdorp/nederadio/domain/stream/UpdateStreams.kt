package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for updating streams in repository
 * @param repository stream repository
 */
class UpdateStreams(private val repository: StreamRepository) {

    suspend operator fun invoke() = repository.forceUpdate()
}