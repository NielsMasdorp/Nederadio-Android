package com.nielsmasdorp.sleeply.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving mobile network streaming setting
 * @param repository settings repository
 */
class GetStreamOnNetworkEnabled(private val repository: SettingsRepository) {

    val flow: Flow<Boolean> = repository.isPlayOnNetworkEnabledFlow
    suspend fun await(): Boolean = repository.isPlayOnNetworkEnabled()
}