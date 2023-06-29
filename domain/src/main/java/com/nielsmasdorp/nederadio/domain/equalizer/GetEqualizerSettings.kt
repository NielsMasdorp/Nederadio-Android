package com.nielsmasdorp.nederadio.domain.equalizer

import com.nielsmasdorp.nederadio.domain.DefaultDispatcherProvider
import com.nielsmasdorp.nederadio.domain.DispatcherProvider
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import kotlinx.coroutines.withContext

class GetEqualizerSettings(
    private val repository: SettingsRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {
    suspend operator fun invoke() = withContext(dispatcher.io()) {
        repository.getEqualizerSettings()
    }
}
