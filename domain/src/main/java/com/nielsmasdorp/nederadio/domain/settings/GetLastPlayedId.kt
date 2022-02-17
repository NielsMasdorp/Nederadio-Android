package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving last played stream id
 * @param repository settings repository
 */
class GetLastPlayedId(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(): String? {
        return withContext(dispatcher.io()) {
            val id = repository.getLastPlayedId()
            id.ifEmpty { null }
        }
    }
}