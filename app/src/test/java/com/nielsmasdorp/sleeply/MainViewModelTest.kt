package com.nielsmasdorp.sleeply

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.sleeply.domain.settings.GetLastPlayedIndex
import com.nielsmasdorp.sleeply.domain.stream.GetAllStreams
import com.nielsmasdorp.sleeply.domain.stream.StreamManager
import com.nielsmasdorp.sleeply.domain.util.CoroutineTestRule
import com.nielsmasdorp.sleeply.ui.stream.MainViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `test`() {
        // TODO
    }

    private fun createViewModel(
        getAllStreams: GetAllStreams,
        streamManager: StreamManager,
        getLastPlayedIndex: GetLastPlayedIndex
    ): MainViewModel {
        return MainViewModel(
            getAllStreams = getAllStreams,
            streamManager = streamManager,
            getLastPlayedIndex = getLastPlayedIndex,
            dispatchers = coroutineTestRule.testDispatcherProvider
        )
    }
}