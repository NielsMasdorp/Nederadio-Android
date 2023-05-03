package com.nielsmasdorp.nederadio.domain.stream

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class GetSuccessfulStreamsTest {

    @Test
    fun `when successful streams is returned by use case then return from subject`() =
        runBlocking {
            // given
            val streams = Streams.Success(
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
            val getAllStreams: GetAllStreams = mockk()
            coEvery { getAllStreams.streams } returns flowOf(streams)

            // when
            val subject = GetSuccessfulStreams(getAllStreams = getAllStreams)

            // then
            Assert.assertEquals(subject.streams.first(), streams.streams)
        }

    @Test
    fun `when loading streams is returned by use case then emit nothing`() = runBlocking {
        // given
        val streams = Streams.Loading
        val getAllStreams: GetAllStreams = mockk()
        coEvery { getAllStreams.streams } returns emptyFlow()

        // when
        val subject = GetSuccessfulStreams(getAllStreams = getAllStreams)

        // then
        Assert.assertEquals(subject.streams.count(), 0)
    }

    @Test
    fun `when error streams is returned by use case then emit nothing`() = runBlocking {
        // given
        val streams = Streams.Error(Failure.GenericError("test"))
        val getAllStreams: GetAllStreams = mockk()
        coEvery { getAllStreams.streams } returns emptyFlow()

        // when
        val subject = GetSuccessfulStreams(getAllStreams = getAllStreams)

        // then
        Assert.assertEquals(subject.streams.count(), 0)
    }
}