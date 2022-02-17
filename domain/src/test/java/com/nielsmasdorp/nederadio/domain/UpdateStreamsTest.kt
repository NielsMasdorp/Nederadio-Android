package com.nielsmasdorp.nederadio.domain

import com.nielsmasdorp.nederadio.domain.stream.StreamRepository
import com.nielsmasdorp.nederadio.domain.stream.UpdateStreams
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class UpdateStreamsTest {

    @Test
    fun `when streams should be updated, update in repository`() = runBlocking {
        // given
        val streamRepository: StreamRepository = mock()

        // when
        val subject = UpdateStreams(streamRepository)
        subject.invoke()

        // then
        verify(streamRepository).forceUpdate()
    }
}