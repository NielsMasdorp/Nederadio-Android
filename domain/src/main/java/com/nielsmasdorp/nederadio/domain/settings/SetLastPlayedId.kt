package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for saving last played stream id
 * @param repository settings repository
 */
class SetLastPlayedId(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(id: String) = withContext(dispatcher.io()) {
        repository.setLastPlayedId(id)
    }
}
