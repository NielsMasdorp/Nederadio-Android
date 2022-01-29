package com.nielsmasdorp.sleeply.domain.settings

import com.nielsmasdorp.sleeply.domain.DefaultDispatcherProvider
import com.nielsmasdorp.sleeply.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for saving last played stream index
 * @param repository settings repository
 */
class SetLastPlayedIndex(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(index: Int) = withContext(dispatcher.io()) {
        repository.setLastPlayedIndex(index)
    }
}