package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for setting the new track for a stream
 * @param repository stream repository
 */
class SetStreamTrack(private val repository: StreamRepository) {

    suspend operator fun invoke(track: String) = repository.updateTrack(track = track)
}