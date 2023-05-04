package com.nielsmasdorp.nederadio.domain.stream

import com.nielsmasdorp.nederadio.domain.settings.SetLastPlayedId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SetActiveStreamTest {

    @Test(expected = UnsupportedOperationException::class)
    fun `when updating with blank id, should throw exception`() = runBlocking {
        val setLastPlayedId: SetLastPlayedId = mockk()
        val settingsRepository = FakeStreamRepository(listOf())
        val id = " "
        coEvery { setLastPlayedId(id) } returns Unit

        // when
        val subject = SetActiveStream(settingsRepository, setLastPlayedId)

        subject(id)
    }

    @Test
    fun `when updating to new id, id should be updated in repository`() =
        runBlocking {
            // given
            val old = Streams.Success(
                listOf(
                    Stream(
                        isActive = true,
                        id = "id",
                        title = "title",
                        imageUrl = "desc",
                        url = "url"
                    ),
                    Stream(
                        isActive = false,
                        id = "id2",
                        title = "title",
                        imageUrl = "desc",
                        url = "url"
                    ),
                )
            )
            val new = Streams.Success(
                listOf(
                    Stream(
                        isActive = false,
                        id = "id",
                        title = "title",
                        imageUrl = "desc",
                        url = "url"
                    ),
                    Stream(
                        isActive = true,
                        id = "id2",
                        title = "title",
                        imageUrl = "desc",
                        url = "url"
                    ),
                )
            )
            val id = "id2"
            val setLastPlayedId: SetLastPlayedId = mockk()
            val settingsRepository = FakeStreamRepository(listOf(old))
            coEvery { setLastPlayedId(id) } returns Unit

            // when
            val subject = SetActiveStream(settingsRepository, setLastPlayedId)
            subject(id)

            // then
            Assert.assertEquals(settingsRepository.streamsFlow.first(), new)
        }
}
