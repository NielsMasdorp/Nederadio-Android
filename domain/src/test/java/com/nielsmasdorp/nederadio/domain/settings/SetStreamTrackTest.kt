package com.nielsmasdorp.nederadio.domain.settings

import com.nielsmasdorp.nederadio.domain.stream.FakeStreamRepository
import com.nielsmasdorp.nederadio.domain.stream.SetStreamTrack
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.domain.stream.Streams
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SetStreamTrackTest {

    @Test
    fun `when updating track, track should be updated in repository only for active stream`() =
        runBlocking {
            // given
            val old = Streams.Success(
                listOf(
                    Stream(
                        isActive = true,
                        id = "id",
                        title = "title",
                        track = "old",
                        imageUrl = "desc",
                        url = "url"
                    ),
                    Stream(
                        isActive = false,
                        id = "id2",
                        title = "title",
                        track = "old",
                        imageUrl = "desc",
                        url = "url"
                    ),
                )
            )
            val new = Streams.Success(
                listOf(
                    Stream(
                        isActive = true,
                        id = "id",
                        title = "title",
                        track = "new",
                        imageUrl = "desc",
                        url = "url"
                    ),
                    Stream(
                        isActive = false,
                        id = "id2",
                        title = "title",
                        track = "old",
                        imageUrl = "desc",
                        url = "url"
                    ),
                )
            )
            val track = "new"
            val settingsRepository = FakeStreamRepository(listOf(old))

            // when
            val subject = SetStreamTrack(settingsRepository)
            subject(track)

            // then
            Assert.assertEquals(settingsRepository.streamsFlow.first(), new)
        }
}