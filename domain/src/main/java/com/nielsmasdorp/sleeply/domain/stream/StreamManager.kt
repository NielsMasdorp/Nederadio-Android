package com.nielsmasdorp.sleeply.domain.stream

import kotlinx.coroutines.flow.Flow

/**
 * Manager responsible for everything related to the streams
 */
interface StreamManager {

    /**
     * [Flow] of stream currently selected
     */
    val stateFlow: Flow<Stream?>

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
     * @param streams all streams that are supported
     * @param startIndex index of the stream that should be active
     * @param controls the controls used to manipulate the streams
     */
    fun initialize(streams: List<Stream>, startIndex: Int, controls: PlayerControls<*>)

    /**
     * Release all stream related memory
     */
    fun release()

    /**
     * Select a new stream
     * @param index the index of the [Stream] that should be switched to
     */
    fun streamPicked(index: Int)

    /**
     * Sleep timer is selected
     * @param ms milliseconds length of selected sleep timer
     */
    fun sleepTimerSet(ms: Long)
}