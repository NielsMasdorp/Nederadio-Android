package com.nielsmasdorp.nederadio.domain

import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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
        val settingsRepository = mock<SettingsRepository> {
            onBlocking { getLastPlayedId() } doReturn index
        }

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
        val settingsRepository = mock<SettingsRepository> {
            onBlocking { getLastPlayedId() } doReturn index
        }

        // when
        val subject = GetLastPlayedId(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(null, output)
    }
}