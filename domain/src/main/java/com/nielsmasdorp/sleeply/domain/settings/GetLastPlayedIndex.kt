package com.nielsmasdorp.sleeply.domain.settings

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving last played stream index
 * @param repository settings repository
 */
class GetLastPlayedIndex(private val repository: SettingsRepository) {

    suspend operator fun invoke(): Int = repository.getLastPlayedIndex()
}