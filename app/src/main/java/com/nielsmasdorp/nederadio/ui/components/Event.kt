package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Credits: https://gist.github.com/hvisser/d9e37248c66d6ee23ed9dfdd28b47219
 */
class Event<T>(val value: T, private val onConsumed: () -> Unit) {
    private val consumed = AtomicBoolean(false)

    internal fun consume() {
        if (consumed.compareAndSet(false, true)) {
            onConsumed()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event<*>

        if (value != other.value) return false
        if (consumed != other.consumed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + consumed.hashCode()
        return result
    }
}

@Composable
fun <T> EventHandler(event: Event<T>?, block: suspend CoroutineScope.(T) -> Unit) {
    event?.value?.let { value ->
        LaunchedEffect(value) {
            try {
                block(value)
            } finally {
                event.consume()
            }
        }
    }
}
