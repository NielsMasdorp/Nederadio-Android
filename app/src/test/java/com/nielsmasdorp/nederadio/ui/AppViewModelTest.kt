package com.nielsmasdorp.nederadio.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.nederadio.domain.settings.AddToFavorites
import com.nielsmasdorp.nederadio.domain.settings.RemoveFromFavorites
import com.nielsmasdorp.nederadio.domain.stream.*
import com.nielsmasdorp.nederadio.util.CoroutineTestRule
import com.nielsmasdorp.nederadio.util.getOrAwaitValue
import dev.burnoo.cokoin.get
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class AppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `when on started with controls, initialise manager with controls`() {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
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
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        every { manager.initialize(controls = controls) } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onStarted(controls = controls)

        // then
        verify { manager.initialize(controls = controls) }
    }

    @Test
    fun `when on stopped, release manager`() {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        every { manager.release() } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onStopped()

        // then
        verify { manager.release() }
    }

    @Test
    fun `when on stream picked, pick stream in manager`() {
        // given
        val id = "id"
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        every { manager.streamPicked(id = id) } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onStreamPicked(id = id)

        // then
        verify { manager.streamPicked(id = id) }
    }

    @Test
    fun `when sleep timer picked, propagate event to show sleep timer`() = runBlocking {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onTimerPicked()

        // then
        Assert.assertEquals(vm.showSleepTimer.first(), true)
    }

    @Test
    fun `when sleep timer set then set in manager and hide sleep timer`() = runBlocking {
        // given
        val index = 0
        val ms = 0L
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        every { manager.sleepTimerSet(ms = ms) } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.setSleepTimer(index = index)

        // then
        verify { manager.sleepTimerSet(ms = ms) }
        Assert.assertEquals(vm.showSleepTimer.first(), false)
    }

    @Test
    fun `when about picked, propagate event to show about`() = runBlocking {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onAboutPicked()

        // then
        Assert.assertEquals(vm.showAboutApp.first(), true)
    }

    @Test
    fun `when dialog is dismissed, propagate event to hide all dialogs`() = runBlocking {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onAlertDismissed()

        // then
        Assert.assertEquals(vm.showAboutApp.first(), false)
        Assert.assertEquals(vm.showSleepTimer.first(), false)
    }

    @Test
    fun `when stream is added to favorites, execute correct usecase`() {
        // given
        val id = "id"
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        val addToFavorites = mockk<AddToFavorites>()
        val removeFromFavorites = mockk<RemoveFromFavorites>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = addToFavorites,
            removeFromFavorites = removeFromFavorites,
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onStreamFavoriteChanged(id = id, isFavorite = true)

        // then
        coVerify { addToFavorites.invoke(id = id) }
        coVerify(exactly = 0) { removeFromFavorites(id = id) }
    }

    @Test
    fun `when stream is removed from favorites, execute correct usecase`() {
        // given
        val id = "id"
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        val addToFavorites = mockk<AddToFavorites>()
        val removeFromFavorites = mockk<RemoveFromFavorites>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        coEvery { removeFromFavorites(id = id) } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = addToFavorites,
            removeFromFavorites = removeFromFavorites,
            updateStreams = mockk(),
            streamManager = manager
        )
        vm.onStreamFavoriteChanged(id = id, isFavorite = false)

        // then
        coVerify { removeFromFavorites.invoke(id = id) }
        coVerify(exactly = 0) { addToFavorites(id = id) }
    }

    @Test
    fun `when streams need to be retried, execute correct usecase`() {
        // given
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        val updateStreams = mockk<UpdateStreams>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }
        coEvery { updateStreams() } returns Unit

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = updateStreams,
            streamManager = manager
        )
        vm.onRetryStreams()

        // then
        coVerify { updateStreams() }
    }

    @Test
    fun `when zero sleep timer is updated by manager, propagate formatted event`() {
        // given
        val ms = 0L
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(ms) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals("", vm.sleepTimer.getOrAwaitValue())
    }

    @Test
    fun `when second sleep timer is updated by manager, propagate formatted event`() {
        // given
        val ms = 1_000L
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(ms) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals("00:01", vm.sleepTimer.getOrAwaitValue())
    }

    @Test
    fun `when minute sleep timer is updated by manager, propagate formatted event`() {
        // given
        val ms = 1_00_000L
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(ms) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals("01:40", vm.sleepTimer.getOrAwaitValue())
    }

    @Test
    fun `when hour sleep timer is updated by manager, propagate formatted event`() {
        // given
        val ms = 3_600_0000L
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(ms) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals("10:00:00", vm.sleepTimer.getOrAwaitValue())
    }

    @Test
    fun `when streams are updated by manager, send streams to livedata`() {
        // given
        val streams = Streams.Loading
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(streams) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals(streams, vm.streams.getOrAwaitValue())
    }

    @Test
    fun `when streams are updated by manager, send favorites according to favorite status to livedata`() {
        // given
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
        val favorites = streams.streams.filter { it.isFavorite }
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(streams) }
        every { getActiveStream.stream} returns flow { emit(ActiveStream.Empty) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals(favorites, vm.favorites.getOrAwaitValue())
    }

    @Test
    fun `when current stream is updated by manager, send stream to livedata`() {
        // given
        val stream = ActiveStream.Empty
        val manager = mockk<StreamManager>()
        val getAllStreams = mockk<GetAllStreams>()
        val getActiveStream = mockk<GetActiveStream>()
        every { getAllStreams.streams } returns flow { emit(Streams.Loading) }
        every { getActiveStream.stream } returns flow { emit(stream) }
        every { manager.sleepTimerFlow } returns flow { emit(null) }
        every { manager.errorFlow } returns flow { emit(StreamingError.Empty) }

        // when
        val vm = createViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = mockk(),
            removeFromFavorites = mockk(),
            updateStreams = mockk(),
            streamManager = manager
        )

        // then
        Assert.assertEquals(stream, vm.activeStream.getOrAwaitValue())
    }

    private fun createViewModel(
        getAllStreams: GetAllStreams,
        getActiveStream: GetActiveStream,
        addToFavorites: AddToFavorites,
        removeFromFavorites: RemoveFromFavorites,
        updateStreams: UpdateStreams,
        streamManager: StreamManager,
    ): AppViewModel {
        return AppViewModel(
            getAllStreams = getAllStreams,
            getActiveStream = getActiveStream,
            addToFavorites = addToFavorites,
            removeFromFavorites = removeFromFavorites,
            updateStreams = updateStreams,
            streamManager = streamManager,
            dispatchers = coroutineTestRule.testDispatcherProvider
        )
    }
}