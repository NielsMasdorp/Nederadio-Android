package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Note: only used in [Flow]s
 */
sealed class StreamingError {

    data class Filled(val error: String) : StreamingError() {
        @Suppress("EqualsAlwaysReturnsTrueOrFalse")
        override fun equals(other: Any?): Boolean =
            false // never equal, so duplicate values are accepted by [Flow]

        override fun hashCode(): Int = error.hashCode()
    }

    object Empty : StreamingError()
}
