package com.nielsmasdorp.nederadio.data.settings

import android.annotation.SuppressLint
import android.content.Context
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.util.observeKey
import kotlinx.coroutines.flow.Flow

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("ApplySharedPref")
class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    override val favoritesFlow: Flow<Set<String>>
        get() = prefs.observeKey(FAVORITES, emptySet())

    override suspend fun getLastPlayedId(): String {
        return prefs.getString(LAST_PLAYED_INDEX, null) ?: ""
    }

    override suspend fun setLastPlayedId(id: String) {
        prefs.edit().putString(LAST_PLAYED_INDEX, id).apply()
    }

    override suspend fun getFavorites(): Set<String> {
        return prefs.getStringSet(FAVORITES, null) ?: emptySet()
    }

    override suspend fun addToFavorite(id: String) {
        val new = getFavorites().toMutableSet().apply { add(id) }
        prefs.edit().putStringSet(FAVORITES, new).apply()
    }

    override suspend fun removeFromFavorite(id: String) {
        val new = getFavorites().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(FAVORITES, new).apply()
    }

    override suspend fun isFavorite(id: String): Boolean = getFavorites().contains(id)

    override suspend fun getEqualizerSettings(): Pair<Boolean, Short> {
        return Pair(
            prefs.getBoolean(EQUALIZER_ENABLED, false),
            prefs.getInt(EQUALIZER_PRESET, 0).toShort()
        )
    }

    override suspend fun setEqualizerSettings(enabled: Boolean, preset: Short) {
        prefs.edit().putBoolean(EQUALIZER_ENABLED, enabled).apply()
        prefs.edit().putInt(EQUALIZER_PRESET, preset.toInt()).apply()
    }

    companion object {
        const val PREF_KEY = "settings"
        const val LAST_PLAYED_INDEX = "last_played_id"
        const val FAVORITES = "favorites"
        const val EQUALIZER_ENABLED = "equalizer_enabled"
        const val EQUALIZER_PRESET = "equalizer_preset"
    }
}
