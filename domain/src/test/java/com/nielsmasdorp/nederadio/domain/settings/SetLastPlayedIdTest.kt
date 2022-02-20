package com.nielsmasdorp.nederadio.domain.settings

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SetLastPlayedIdTest {

    @Test
    fun `id should be set in repository`() = runBlocking {
        // given
        val id = "id"
        val settingsRepository: SettingsRepository = mockk()
        coEvery { settingsRepository.setLastPlayedId(id) } returns Unit

        // when
        val subject = SetLastPlayedId(settingsRepository)
        subject.invoke(id)

        // then
        coVerify { settingsRepository.setLastPlayedId(id) }
    }
}