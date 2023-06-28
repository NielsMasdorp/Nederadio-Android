package com.nielsmasdorp.nederadio.domain.equalizer

import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class GetEqualizerSettingsTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `when settings are retrieved from repository then return settings from use case`() =
        runBlocking {
            // given
            val settings = Pair(true, 4.toShort())
            val settingsRepository = mockk<SettingsRepository>()
            coEvery { settingsRepository.getEqualizerSettings() } returns settings

            // when
            val subject =
                GetEqualizerSettings(settingsRepository, coroutineTestRule.testDispatcherProvider)
            val output = subject.invoke()

            // then
            Assert.assertEquals(settings, output)
        }
}
