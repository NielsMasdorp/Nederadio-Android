package com.nielsmasdorp.nederadio.domain.equalizer

import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class SetEqualizerSettingsTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `settings should be set in repository`() = runBlocking {
        // given
        val settings = Pair(true, 3.toShort())
        val settingsRepository: SettingsRepository = mockk()
        coEvery {
            settingsRepository.setEqualizerSettings(
                settings.first,
                settings.second
            )
        } returns Unit

        // when
        val subject = SetEqualizerSettings(settingsRepository)
        subject.invoke(settings.first, settings.second)

        // then
        coVerify { settingsRepository.setEqualizerSettings(settings.first, settings.second) }
    }
}
