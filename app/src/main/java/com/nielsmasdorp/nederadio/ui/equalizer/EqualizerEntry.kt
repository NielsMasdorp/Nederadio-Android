package com.nielsmasdorp.nederadio.ui.equalizer

import com.patrykandpatrick.vico.core.entry.ChartEntry

class EqualizerEntry(
    val hertz: String,
    override val x: Float,
    override val y: Float,
) : ChartEntry {
    override fun withY(y: Float) = EqualizerEntry(hertz, x, y)
}
