package com.nielsmasdorp.nederadio.domain.stream

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class UpdateStreamsTest {

    @Test
    fun `when streams should be updated, update in repository`() = runBlocking {
        // given
        val streamRepository: StreamRepository = mockk()
        coEvery { streamRepository.forceUpdate() } returns Unit

        // when
        val subject = UpdateStreams(streamRepository)
        subject.invoke()

        // then
        coVerify { streamRepository.forceUpdate() }
    }
}