package com.nielsmasdorp.nederadio.domain.equalizer

import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import kotlinx.coroutines.withContext

class SetEqualizerSettings(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(enabled: Boolean, preset: Short) = withContext(dispatcher.io()) {
        repository.setEqualizerSettings(enabled = enabled, preset = preset)
    }
}
