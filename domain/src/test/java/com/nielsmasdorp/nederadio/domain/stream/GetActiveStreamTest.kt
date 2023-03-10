package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class GetActiveStreamTest {

    @Test
    fun `when streams is successful and has active stream, return filled active stream`() =
        runBlocking {
            // given
            val activeStream = Stream(
                isActive = true,
                id = "id",
                title = "title",
                imageUrl = "desc",
                url = "url"
            )
            val streams = Streams.Success(
                listOf(
                    activeStream,
                    Stream(
                        isActive = false,
                        id = "id2",
                        title = "title",
                        imageUrl = "desc",
                        url = "url"
                    ),
                )
            )
            val settingsRepository = FakeStreamRepository(listOf(streams))

            // when
            val subject = GetActiveStream(settingsRepository)

            // then
            Assert.assertEquals(subject.stream.first(), ActiveStream.Filled(stream = activeStream))
        }

    @Test
    fun `when streams is successful and has no active stream, return empty stream`() =
        runBlocking {
            // given
            val streams = Streams.Success(
                listOf(
                    Stream(
                        isActive = false,
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
            val settingsRepository = FakeStreamRepository(listOf(streams))

            // when
            val subject = GetActiveStream(settingsRepository)

            // then
            Assert.assertEquals(subject.stream.first(), ActiveStream.Empty)
        }

    @Test
    fun `when streams is loading, return unknown stream`() =
        runBlocking {
            // given
            val streams = Streams.Loading
            val settingsRepository = FakeStreamRepository(listOf(streams))

            // when
            val subject = GetActiveStream(settingsRepository)

            // then
            Assert.assertEquals(subject.stream.first(), ActiveStream.Unknown)
        }

    @Test
    fun `when streams has error, return unknown stream`() =
        runBlocking {
            // given
            val streams = Streams.Error(Failure.GenericError("test"))
            val settingsRepository = FakeStreamRepository(listOf(streams))

            // when
            val subject = GetActiveStream(settingsRepository)

            // then
            Assert.assertEquals(subject.stream.first(), ActiveStream.Unknown)
        }
}