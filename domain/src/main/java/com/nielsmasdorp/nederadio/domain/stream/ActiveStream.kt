package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
sealed class ActiveStream {
    data class Filled(val stream: Stream): ActiveStream()
    object Empty: ActiveStream()
    object Unknown: ActiveStream()
}
