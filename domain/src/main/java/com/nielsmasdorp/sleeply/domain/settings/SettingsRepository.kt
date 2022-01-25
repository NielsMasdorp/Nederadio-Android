package com.nielsmasdorp.sleeply.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Repository used to store app settings and some state restoration required values
 */
interface SettingsRepository {

    /**
     * [Flow] of setting whether streaming on mobile network is supported
     */
    val isPlayOnNetworkEnabledFlow: Flow<Boolean>

    /**
     * @return whether streaming on mobile network is supported
     */
    suspend fun isPlayOnNetworkEnabled(): Boolean

    /**
     * Save setting whether streaming on mobile network is supported
     * @param enabled the setting state
     */
    suspend fun setPlayOnNetworkEnabled(enabled: Boolean)

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