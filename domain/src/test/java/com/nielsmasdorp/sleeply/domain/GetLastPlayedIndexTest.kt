package com.nielsmasdorp.sleeply.domain

import com.nielsmasdorp.sleeply.domain.settings.GetLastPlayedIndex
import com.nielsmasdorp.sleeply.domain.settings.SettingsRepository
import com.nielsmasdorp.sleeply.domain.util.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class GetLastPlayedIndexTest {

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
        val subject = GetLastPlayedIndex(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(index, output)
    }
}