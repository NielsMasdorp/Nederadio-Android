package com.nielsmasdorp.nederadio.domain

import com.nielsmasdorp.nederadio.domain.stream.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class GetAllStreamsTest {

    @Test
    fun `when successful streams is returned by repository then return from subject`() =
        runBlocking {
            // given
            val streams = CurrentStreams.Success(
                listOf(
                    Stream(
                        id = "id",
                        title = "title",
                        imageUrl = "desc",
                        imageBytes = byteArrayOf(),
                        url = "url"
                    ),
                    Stream(
                        id = "id2",
                        title = "title",
                        imageUrl = "desc",
                        imageBytes = byteArrayOf(),
                        url = "url"
                    ),
                )
            )
            val settingsRepository = FakeStreamRepository(listOf(streams))

            // when
            val subject = GetAllStreams(settingsRepository)

            // then
            Assert.assertEquals(subject.streams.first(), streams)
        }

    @Test
    fun `when loading streams is returned by repository then return from subject`() = runBlocking {
        // given
        val settingsRepository = FakeStreamRepository(listOf(CurrentStreams.Loading))

        // when
        val subject = GetAllStreams(settingsRepository)

        // then
        Assert.assertEquals(subject.streams.first(), CurrentStreams.Loading)
    }

    @Test
    fun `when unsuccessful streams is returned by repository then return from subject`() =
        runBlocking {
            // given
            val error = CurrentStreams.Error(Failure.GenericError("error"))
            val settingsRepository = FakeStreamRepository(listOf(error))

            // when
            val subject = GetAllStreams(settingsRepository)

            // then
            Assert.assertEquals(subject.streams.first(), error)
        }

    @Test
    fun `when repository emits values then subject should emit values in same order`() =
        runBlocking {
            // given
            val streams = listOf(
                CurrentStreams.Loading,
                CurrentStreams.Success(
                    listOf(
                        Stream(
                            id = "id",
                            title = "title",
                            imageUrl = "desc",
                            imageBytes = byteArrayOf(),
                            url = "url"
                        ),
                        Stream(
                            id = "id2",
                            title = "title",
                            imageUrl = "desc",
                            imageBytes = byteArrayOf(),
                            url = "url"
                        ),
                    )
                )
            )
            val settingsRepository = FakeStreamRepository(streams)

            // when
            val subject = GetAllStreams(settingsRepository)

            // then
            Assert.assertEquals(subject.streams.take(2).toList(), streams)
        }
}