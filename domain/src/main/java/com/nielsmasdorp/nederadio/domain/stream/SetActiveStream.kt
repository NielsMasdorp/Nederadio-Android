package com.nielsmasdorp.nederadio.domain.stream

import com.nielsmasdorp.nederadio.domain.settings.SetLastPlayedId

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for saving the active stream
 * @param repository stream repository
 * @param setLastPlayedId set last played id use case
 */
class SetActiveStream(
    private val repository: StreamRepository,
    private val setLastPlayedId: SetLastPlayedId
) {

    suspend operator fun invoke(id: String) {
        if (id.isBlank()) throw UnsupportedOperationException("id is empty")
        setLastPlayedId(id = id)
        repository.updateActive(id = id)
    }
}