package com.nielsmasdorp.sleeply.domain.settings

/**
 * Repository used to store app settings and some state restoration required values
 */
interface SettingsRepository {

    /**
     * @return last played stream index
     */
    suspend fun getLastPlayedIndex(): Int

    /**
     * Save last played stream index
     * @param index the index
     */
    suspend fun setLastPlayedIndex(index: Int)
}