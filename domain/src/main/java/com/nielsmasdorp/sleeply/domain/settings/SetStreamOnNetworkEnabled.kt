package com.nielsmasdorp.sleeply.domain.settings

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for saving mobile network streaming setting
 * @param repository settings repository
 */
class SetStreamOnNetworkEnabled(private val repository: SettingsRepository) {

    suspend operator fun invoke(enabled: Boolean) = repository.setPlayOnNetworkEnabled(enabled)
}