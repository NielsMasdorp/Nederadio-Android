package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

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
        val settingsRepository: SettingsRepository = mockk()
        coEvery { settingsRepository.removeFromFavorite(id) } returns Unit

        // when
        val subject =
            RemoveFromFavorites(settingsRepository, coroutineTestRule.testDispatcherProvider)
        subject.invoke(id)

        // then
        coVerify { settingsRepository.removeFromFavorite(id) }
    }
}
