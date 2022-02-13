package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.flow.Flow

/**
 * Manager responsible for everything related to the streams
 */
interface StreamManager {

    /**
     * [Flow] of stream currently selected
     */
    val currentStreamFlow: Flow<CurrentStream>

    /**
     * [Flow] of milliseconds left in the sleep timer
     */
    val sleepTimerFlow: Flow<Long?>

    /**
     * [Flow] of optional error
     */
    val errorFlow: Flow<StreamingError>

    /**
     * Initialize this manager
     * @param controls the controls used to manipulate the streams
     */
    fun initialize(controls: List<PlayerControls<*>>)

    /**
     * Release all stream related memory
     */
    fun release()

    /**
     * Select a new stream
     * @param id the id of the [Stream] that should be switched to
     */
    fun streamPicked(id: String)

    /**
     * Sleep timer is selected
     * @param ms milliseconds length of selected sleep timer
     */
    fun sleepTimerSet(ms: Long)
}