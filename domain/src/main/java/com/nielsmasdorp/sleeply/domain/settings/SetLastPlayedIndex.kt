package com.nielsmasdorp.sleeply.domain.settings

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for saving last played stream index
 * @param repository settings repository
 */
class SetLastPlayedIndex(private val repository: SettingsRepository) {

    suspend operator fun invoke(index: Int) = repository.setLastPlayedIndex(index)
}