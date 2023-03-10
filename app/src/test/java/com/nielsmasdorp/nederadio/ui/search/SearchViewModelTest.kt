package com.nielsmasdorp.nederadio.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.util.CoroutineTestRule
import com.nielsmasdorp.nederadio.util.getOrAwaitValue
import io.mockk.*
import kotlinx.coroutines.flow.flow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()
    @Test
    fun `when search query changes, emit new value`() {
        // given
        val query = "query"
        val getAllStreams = mockk<GetAllStreams>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            streamManager = mockk()
        )
        vm.onSearchQueryChanged(query = query)

        // then
        Assert.assertEquals(query, vm.searchQuery.value)
    }

    @Test
    fun `when stream is picked, then pick stream in manager`() {
        // given
        val id = "id"
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { manager.streamPicked(id = id) } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            streamManager = manager
        )
        vm.onStreamPicked(id = id)

        // then
        verify { manager.streamPicked(id = id) }
    }

    @Test
    fun `when streams are updated without query, then propagate all streams`() {
        // given
        val query = ""
        val streams = Streams.Success(
            listOf(
                Stream(
                    isActive = false,
                    id = "1",
                    url = "url",
                    title = "title",
                    imageUrl = "imageUrl",
                    isFavorite = true
                ),
                Stream(
                    isActive = false,
                    id = "2",
                    url = "url",
                    title = "title",
                    imageUrl = "imageUrl",
                    isFavorite = false
                ),
            )
        )
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        every { getAllStreams.streams } returns flow { emit(streams) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            streamManager = manager
        )
        vm.onSearchQueryChanged(query = query)

        // then
        Assert.assertEquals(streams.streams, vm.searchedStreams.getOrAwaitValue())
    }

    @Test
    fun `when streams are updated with query, then propagate all streams`() {
        // given
        val query = "lame"
        val streams = Streams.Success(
            listOf(
                Stream(
                    isActive = false,
                    id = "1",
                    url = "url",
                    title = "cool title",
                    imageUrl = "imageUrl",
                    isFavorite = true
                ),
                Stream(
                    isActive = false,
                    id = "2",
                    url = "url",
                    title = "lame title",
                    imageUrl = "imageUrl",
                    isFavorite = false
                ),
            )
        )
        val expected = listOf(
            Stream(
                isActive = false,
                id = "2",
                url = "url",
                title = "lame title",
                imageUrl = "imageUrl",
                isFavorite = false
            )
        )
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        every { getAllStreams.streams } returns flow { emit(streams) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            streamManager = manager
        )
        vm.onSearchQueryChanged(query = query)

        // then
        Assert.assertEquals(expected, vm.searchedStreams.getOrAwaitValue())
    }

    private fun createViewModel(
        getAllStreams: GetAllStreams,
        streamManager: StreamManager,
    ): SearchViewModel {
        return SearchViewModel(
            getAllStreams = getAllStreams,
            streamManager = streamManager,
        )
    }
}