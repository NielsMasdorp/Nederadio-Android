package com.nielsmasdorp.nederadio.domain

import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.settings.SettingsRepository
import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class GetLastPlayedIdTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `index should be returned from repository`() = runBlocking {
        // given
        val index = 80085
        val settingsRepository = mock<SettingsRepository> {
            onBlocking { getLastPlayedIndex() } doReturn index
        }

        // when
        val subject = GetLastPlayedId(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(index, output)
    }
}