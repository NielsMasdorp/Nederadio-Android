package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerManager
import com.nielsmasdorp.nederadio.domain.equalizer.EqualizerState
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.*

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
                            hertz = "${item.frequency.div(1000)}Hz",
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
}
