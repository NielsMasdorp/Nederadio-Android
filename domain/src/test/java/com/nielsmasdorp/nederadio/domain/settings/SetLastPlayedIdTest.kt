package com.nielsmasdorp.nederadio.domain.settings

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SetLastPlayedIdTest {

    @Test
    fun `id should be set in repository`() = runBlocking {
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