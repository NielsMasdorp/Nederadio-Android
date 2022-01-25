package com.nielsmasdorp.sleeply.data.settings

import android.content.Context
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.nielsmasdorp.sleeply.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val prefs =
        FlowSharedPreferences(context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE))

    override val isPlayOnNetworkEnabledFlow: Flow<Boolean> =
        prefs.getBoolean(STREAM_ON_NETWORK_ENABLED, false).asFlow()

    override suspend fun isPlayOnNetworkEnabled(): Boolean {
        return prefs.getBoolean(STREAM_ON_NETWORK_ENABLED, false).get()
    }

    override suspend fun setPlayOnNetworkEnabled(enabled: Boolean) {
        prefs.getBoolean(STREAM_ON_NETWORK_ENABLED).set(enabled)
    }

    override suspend fun getLastPlayedIndex(): Int {
        return prefs.getInt(LAST_PLAYED_INDEX, 0).get()
    }

    override suspend fun setLastPlayedIndex(index: Int) = prefs.getInt(LAST_PLAYED_INDEX).set(index)

    companion object {
        const val PREF_KEY = "settings"
        const val STREAM_ON_NETWORK_ENABLED = "stream_wifi_only"
        const val LAST_PLAYED_INDEX = "last_played_index"
    }
}