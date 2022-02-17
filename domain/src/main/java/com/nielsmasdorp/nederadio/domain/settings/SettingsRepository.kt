package com.nielsmasdorp.nederadio.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Repository used to store app settings and some state restoration required values
 */
interface SettingsRepository {

    /**
     * A flow of ids for favorite streams
     */
    val favoritesFlow: Flow<Set<String>>

    /**
     * @return last played stream id
     */
    suspend fun getLastPlayedId(): String

    /**
     * Save last played stream id
     * @param id the id
     */
    suspend fun setLastPlayedId(id: String)

    /**
     * @return favorite stream ids
     */
    suspend fun getFavorites(): Set<String>

    /**
     * Save favorite stream
     * @param id the id of the stream
     */
    suspend fun addToFavorite(id: String)

    /**
     * Remove favorite stream
     * @param id the id of the stream
     */
    suspend fun removeFromFavorite(id: String)
}