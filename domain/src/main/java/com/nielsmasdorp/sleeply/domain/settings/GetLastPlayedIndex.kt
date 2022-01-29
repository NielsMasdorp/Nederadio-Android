package com.nielsmasdorp.sleeply.domain.settings

import com.nielsmasdorp.sleeply.domain.DefaultDispatcherProvider
import com.nielsmasdorp.sleeply.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving last played stream index
 * @param repository settings repository
 */
class GetLastPlayedIndex(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(): Int {
        return withContext(dispatcher.io()) {
            repository.getLastPlayedIndex()
        }
    }
}