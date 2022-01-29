package com.nielsmasdorp.sleeply.domain

import com.nielsmasdorp.sleeply.domain.settings.SetLastPlayedIndex
import com.nielsmasdorp.sleeply.domain.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class SetLastPlayedIndexTest {

    @Test
    fun `index should be set in repository`() = runBlocking {
        // given
        val index = 80085
        val settingsRepository: SettingsRepository = mock()

        // when
        val subject = SetLastPlayedIndex(settingsRepository)
        subject.invoke(index)

        // then
        verify(settingsRepository).setLastPlayedIndex(index)
    }
}