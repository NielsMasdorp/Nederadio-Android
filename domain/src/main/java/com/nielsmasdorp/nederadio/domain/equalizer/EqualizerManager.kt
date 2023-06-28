package com.nielsmasdorp.nederadio.domain.equalizer

import kotlinx.coroutines.flow.Flow

interface EqualizerManager {

    /**
     * The current state of the equalizer as set by the user
     */
    val equalizerState: Flow<EqualizerState>

    /**
     * Initialize the equalizer with a given
     * @param audioSessionId session from the player
     */
    fun initialize(audioSessionId: Int)

    /**
     * Called when cast status is changed
     * @param isCasting whether the casting session is in progress
     */
    fun onCastingStatusChanged(isCasting: Boolean)

    /**
     * Enable the equalizer
     */
    fun onEnabled(enabled: Boolean)

    /**
     * Change the equalizer to a different preset
     * @param preset as index from 0 to n
     */
    fun onPresetChanged(preset: Short)
}
