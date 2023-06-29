package com.nielsmasdorp.nederadio.domain.equalizer

sealed class EqualizerState(val isEnabled: Boolean, val canBeEnabled: Boolean) {

    data class Filled(
        val enabled: Boolean,
        val min: Short,
        val max: Short,
        val presets: EqualizerPresets,
        val bands: List<EqualizerBand>
    ) : EqualizerState(isEnabled = enabled, canBeEnabled = true)

    object NotAvailableWhileCasting : EqualizerState(isEnabled = false, canBeEnabled = false)

    object Loading : EqualizerState(isEnabled = false, canBeEnabled = false)
}

data class EqualizerPresets(
    val currentPreset: Short,
    val presets: List<String>
)

data class EqualizerBand(
    val level: Short,
    val frequency: Int
)
