package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerBand
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerManager
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerPresets
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerState
import com.nielsmasdorp.nederadio.util.CoroutineTestRule
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
class EqualizerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `when preset changes then set in manager and enable eq`() {
        // given
        val preset = 2.toShort()
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow { emit(EqualizerState.Loading) }
        coEvery { equalizerManager.onPresetChanged(preset = preset) } returns Unit
        coEvery { equalizerManager.onEnabled(enabled = true) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)
        vm.setPreset(preset = preset)

        // then
        coVerify { equalizerManager.onPresetChanged(preset = preset) }
        coVerify { equalizerManager.onEnabled(enabled = true) }
    }

    @Test
    fun `when enabled changes then set in manager`() {
        // given
        val enabled = false
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow { emit(EqualizerState.Loading) }
        coEvery { equalizerManager.onEnabled(enabled = enabled) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)
        vm.setEnabled(enabled = enabled)

        // then
        coVerify { equalizerManager.onEnabled(enabled = enabled) }
    }

    @Test
    fun `when state changes in manager then publish from vm`() = runBlocking {
        // given
        val enabled = false
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow {
            emit(EqualizerState.Loading)
        }
        coEvery { equalizerManager.onEnabled(enabled = enabled) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)

        // then
        Assert.assertEquals(EqualizerState.Loading, vm.equalizerState.first())
    }

    @Test
    fun `when non filled state changes then do not update producer`() = runBlocking {
        // given
        val enabled = false
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow {
            emit(EqualizerState.Loading)
        }
        coEvery { equalizerManager.onEnabled(enabled = enabled) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)

        // then
        Assert.assertEquals(0, vm.equalizerProducer.getModel().entries.size)
    }

    @Test
    fun `when non invalid state changes then do not update producer`() = runBlocking {
        // given
        val enabled = false
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow {
            emit(EqualizerState.NotAvailableWhileCasting)
        }
        coEvery { equalizerManager.onEnabled(enabled = enabled) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)

        // then
        Assert.assertEquals(0, vm.equalizerProducer.getModel().entries.size)
    }

    @Test
    fun `when non valid state changes then update producer`() = runBlocking {
        // given
        val enabled = false
        val equalizerManager = mockk<EqualizerManager>()
        every { equalizerManager.equalizerState } returns flow {
            emit(
                EqualizerState.Filled(
                    enabled = true,
                    min = 0,
                    max = 0,
                    presets = EqualizerPresets(
                        currentPreset = 0,
                        presets = listOf()
                    ),
                    bands = listOf(
                        EqualizerBand(level = 0, frequency = 1_000),
                        EqualizerBand(level = 1, frequency = 1_000_000)
                    )
                )
            )
        }
        coEvery { equalizerManager.onEnabled(enabled = enabled) } returns Unit
        // when
        val vm = createViewModel(equalizerManager = equalizerManager)

        // then
        val entries = vm.equalizerProducer.getModel().entries.first()
        Assert.assertEquals(2, entries.size)
        Assert.assertEquals("1Hz", (entries[0] as EqualizerEntry).hertz)
        Assert.assertEquals("1kHz", (entries[1] as EqualizerEntry).hertz)
    }

    private fun createViewModel(
        equalizerManager: EqualizerManager
    ): EqualizerViewModel {
        return EqualizerViewModel(equalizerManager = equalizerManager)
    }
}
