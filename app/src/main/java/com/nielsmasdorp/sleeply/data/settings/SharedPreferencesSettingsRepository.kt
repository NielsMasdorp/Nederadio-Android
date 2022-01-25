package com.nielsmasdorp.sleeply.data.settings

import android.content.Context
import com.nielsmasdorp.sleeply.domain.settings.SettingsRepository

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    override suspend fun getLastPlayedIndex(): Int {
        return prefs.getInt(LAST_PLAYED_INDEX, 0)
    }

    override suspend fun setLastPlayedIndex(index: Int) {
        prefs.edit().putInt(LAST_PLAYED_INDEX, index).apply()
    }

    companion object {
        const val PREF_KEY = "settings"
        const val LAST_PLAYED_INDEX = "last_played_index"
    }
}