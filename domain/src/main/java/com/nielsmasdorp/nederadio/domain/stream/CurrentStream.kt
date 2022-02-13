package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
sealed class CurrentStream {

    data class Filled(val stream: Stream): CurrentStream()
    object Empty: CurrentStream()
    object Unknown: CurrentStream()
}
