package com.nielsmasdorp.sleeply.domain

import com.nielsmasdorp.sleeply.domain.stream.GetAllStreams
import com.nielsmasdorp.sleeply.domain.stream.Stream
import com.nielsmasdorp.sleeply.domain.stream.StreamRepository
import com.nielsmasdorp.sleeply.domain.util.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class GetAllStreamsTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `streams should be returned by repository`() = runBlocking {
        // given
        val streams = listOf(
            Stream(id = "id", title = "title", desc = "desc", smallImgRes = -1, url = "url"),
            Stream(id = "id2", title = "title", desc = "desc", smallImgRes = -1, url = "url")
        )
        val settingsRepository = mock<StreamRepository> {
            onBlocking { getStreams() } doReturn streams
        }

        // when
        val subject = GetAllStreams(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke()

        // then
        Assert.assertEquals(streams, output)
    }
}