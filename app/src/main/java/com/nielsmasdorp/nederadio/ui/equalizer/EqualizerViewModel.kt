package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerManager
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerState
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

class EqualizerViewModel(
    private val equalizerManager: EqualizerManager
) : ViewModel() {

    val equalizerProducer = ChartEntryModelProducer()

    val equalizerState: StateFlow<EqualizerState> =
        equalizerManager.equalizerState.onEach { state ->
            if (state is EqualizerState.Filled) {
                equalizerProducer.setEntries(
                    state.bands.mapIndexed { index, item ->
                        EqualizerEntry(
                            hertz = formatHertz(frequency = item.frequency.toFloat()),
                            x = index.toFloat(),
                            y = item.level.toFloat()
                        )
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = EqualizerState.Loading
        )

    fun setPreset(preset: Short) {
        equalizerManager.onPresetChanged(preset = preset)
        equalizerManager.onEnabled(enabled = true)
    }

    fun setEnabled(enabled: Boolean) {
        equalizerManager.onEnabled(enabled = enabled)
    }

    @Suppress("MagicNumber")
    private fun formatHertz(frequency: Float): String {
        return if (frequency < 1_000_000) {
            "${frequency.div(1_000).roundToInt()}Hz"
        } else {
            "${frequency.div(1_000_000).roundToInt()}kHz"
        }
    }
}
