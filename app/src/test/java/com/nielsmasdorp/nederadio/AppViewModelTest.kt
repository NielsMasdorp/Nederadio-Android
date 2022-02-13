package com.nielsmasdorp.nederadio

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.nederadio.domain.settings.GetLastPlayedId
import com.nielsmasdorp.nederadio.domain.stream.GetAllStreams
import com.nielsmasdorp.nederadio.domain.stream.StreamManager
import com.nielsmasdorp.nederadio.domain.util.CoroutineTestRule
import com.nielsmasdorp.nederadio.ui.AppViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppViewModelTest {

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
        getLastPlayedId: GetLastPlayedId
    ): AppViewModel {
        return AppViewModel(
            getAllStreams = getAllStreams,
            streamManager = streamManager,
            getLastPlayedIndex = getLastPlayedId,
            dispatchers = coroutineTestRule.testDispatcherProvider
        )
    }
}