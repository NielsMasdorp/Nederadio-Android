package com.nielsmasdorp.nederadio.domain

import com.nielsmasdorp.nederadio.domain.settings.SetLastPlayedId
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SetLastPlayedIdTest {

    @Test
    fun `index should be set in repository`() = runBlocking {
        // given
        val id = "id"
        val settingsRepository: SettingsRepository = mock()

        // when
        val subject = SetLastPlayedId(settingsRepository)
        subject.invoke(id)

        // then
        verify(settingsRepository).setLastPlayedId(id)
    }
}