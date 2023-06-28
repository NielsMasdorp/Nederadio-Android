package com.nielsmasdorp.nederadio.data.equalizer

import android.media.audiofx.Equalizer
import com.nielsmasdorp.nederadio.domain.equalizer.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidEqualizerManager(
    private val getEqualizerSettings: GetEqualizerSettings,
    private val setEqualizerSettings: SetEqualizerSettings
) : EqualizerManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val equalizerState: MutableStateFlow<EqualizerState> =
        MutableStateFlow(EqualizerState.Loading)

    lateinit var equalizer: Equalizer

    override fun initialize(audioSessionId: Int) {
        scope.launch {
            equalizer = Equalizer(/* priority */ 0, audioSessionId).apply {
                val (isEnabled, preset) = getEqualizerSettings()
                enabled = isEnabled
                usePreset(preset)
            }.also { eq ->
                equalizerState.value = eq.toState()
            }
        }
    }

    override fun onCastingStatusChanged(isCasting: Boolean) {
        if (isCasting) {
            equalizerState.value = if (isCasting) {
                EqualizerState.NotAvailableWhileCasting
            } else {
                equalizer.toState()
            }
        }
    }

    override fun onEnabled(enabled: Boolean) {
        scope.launch {
            setEqualizerSettings(enabled = enabled, preset = equalizer.currentPreset)
            equalizer.enabled = enabled
            equalizerState.value = equalizer.toState()
        }
    }

    override fun onPresetChanged(preset: Short) {
        scope.launch {
            setEqualizerSettings(enabled = equalizer.enabled, preset = preset)
            equalizer.usePreset(preset)
            equalizerState.value = equalizer.toState()
        }
    }

    private fun Equalizer.toState(): EqualizerState {
        val bands = mutableListOf<EqualizerBand>()
        val presets = mutableListOf<String>()
        for (i in 0 until numberOfBands) {
            bands.add(
                EqualizerBand(
                    frequency = getCenterFreq(i.toShort()),
                    level = getBandLevel(i.toShort())
                )
            )
        }
        for (j in 0 until numberOfPresets) {
            presets.add(getPresetName(j.toShort()))
        }
        return EqualizerState.Filled(
            enabled = enabled,
            presets = EqualizerPresets(
                currentPreset = currentPreset,
                presets = presets
            ),
            min = bandLevelRange[0],
            max = bandLevelRange[1],
            bands = bands
        )
    }
}
