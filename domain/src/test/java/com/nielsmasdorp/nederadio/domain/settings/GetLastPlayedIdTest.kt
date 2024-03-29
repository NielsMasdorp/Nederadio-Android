package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class GetLastPlayedIdTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `when index from repository is set, return the index`() = runBlocking {
        // given
        val index = "id"
        val settingsRepository = mockk<SettingsRepository>()
        coEvery { settingsRepository.getLastPlayedId() } returns index

        // when
        val subject = GetLastPlayedId(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(index, output)
    }

    @Test
    fun `when index from repository is empty then return null`() = runBlocking {
        // given
        val index = ""
        val settingsRepository = mockk<SettingsRepository>()
        coEvery { settingsRepository.getLastPlayedId() } returns index

        // when
        val subject = GetLastPlayedId(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(null, output)
    }
}
