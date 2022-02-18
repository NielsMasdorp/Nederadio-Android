package com.nielsmasdorp.nederadio.domain.stream

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake [StreamRepository] implementation
 *
 * @author Niels Masdorp (NielsMasdorp)
 */
class FakeStreamRepository(private val values: List<CurrentStreams>) : StreamRepository {

    private var update = false

    override val streamsFlow: Flow<CurrentStreams>
        get() = flow {
            for (value in values) {
                emit(value)
            }
            while (!update) {
                delay(100L)
            }
            for (value in values) {
                emit(value)
            }
            update = false
        }

    override fun forceUpdate() {
        update = true
    }
}