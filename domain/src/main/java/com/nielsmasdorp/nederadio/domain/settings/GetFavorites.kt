package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving favorite streams
 * @param repository settings repository
 */
class GetFavorites(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(): Set<String> {
        return withContext(dispatcher.io()) {
            repository.getFavorites()
        }
    }
}