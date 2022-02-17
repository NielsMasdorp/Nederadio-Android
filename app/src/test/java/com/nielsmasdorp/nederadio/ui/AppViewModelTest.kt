package com.nielsmasdorp.nederadio.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.nederadio.domain.settings.AddToFavorites
import com.nielsmasdorp.nederadio.domain.settings.RemoveFromFavorites
import com.nielsmasdorp.nederadio.domain.stream.GetAllStreams
import com.nielsmasdorp.nederadio.domain.stream.PlayerControls
import com.nielsmasdorp.nederadio.domain.stream.StreamManager
import com.nielsmasdorp.nederadio.domain.stream.UpdateStreams
import com.nielsmasdorp.nederadio.util.CoroutineTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `when on started with controls, initialise manager with controls`() {
        // given
        val manager = mock(StreamManager::class.java)
        val controls: List<PlayerControls<*>> = listOf(object : PlayerControls<String> {
            override fun getView(): String {
                return "first control"
            }
        },
            object : PlayerControls<String> {
                override fun getView(): String {
                    return "second control"
                }
            }
        )

        // when
        val vm = createViewModel(
            getAllStreams = mock(GetAllStreams::class.java),
            addToFavorites = mock(AddToFavorites::class.java),
            removeFromFavorites = mock(RemoveFromFavorites::class.java),
            updateStreams = mock(UpdateStreams::class.java),
            streamManager = manager
        )
        vm.onStarted(controls = controls)

        // then
        verify(manager).initialize(controls = controls)
    }

    private fun createViewModel(
        getAllStreams: GetAllStreams,
        addToFavorites: AddToFavorites,
        removeFromFavorites: RemoveFromFavorites,
        updateStreams: UpdateStreams,
        streamManager: StreamManager,
    ): AppViewModel {
        return AppViewModel(
            getAllStreams = getAllStreams,
            addToFavorites = addToFavorites,
            removeFromFavorites = removeFromFavorites,
            updateStreams = updateStreams,
            streamManager = streamManager,
            dispatchers = coroutineTestRule.testDispatcherProvider
        )
    }
}