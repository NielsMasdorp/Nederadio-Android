package com.nielsmasdorp.sleeply.domain

import com.nielsmasdorp.sleeply.domain.stream.GetStreamById
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
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class GetStreamByIdTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `stream should be returned by repository with valid id`() = runBlocking {
        // given
        val id = "id"
        val stream =
            Stream(id = "id", title = "title", desc = "desc", smallImgRes = -1, url = "url")
        val settingsRepository = mock<StreamRepository> {
            onBlocking { getStreamById(id) } doReturn stream
        }

        // when
        val subject = GetStreamById(settingsRepository, coroutineTestRule.testDispatcherProvider)
        val output = subject.invoke(id)

        // then
        verify(settingsRepository).getStreamById(id)
        Assert.assertEquals(stream, output)
    }
}