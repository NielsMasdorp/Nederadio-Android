package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class RemoveFromFavoritesTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `id should be set removed from repository`() = runBlocking {
        // given
        val id = "id"
        val settingsRepository: SettingsRepository = mock()

        // when
        val subject = RemoveFromFavorites(settingsRepository, coroutineTestRule.testDispatcherProvider)
        subject.invoke(id)

        // then
        verify(settingsRepository).removeFromFavorite(id)
    }

}