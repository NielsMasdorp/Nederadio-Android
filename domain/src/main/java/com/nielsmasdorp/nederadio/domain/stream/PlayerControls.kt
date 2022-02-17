package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Used as marker interface to bridge between domain layer and presentation layer
 */
interface PlayerControls<T> {

    fun getView(): T
}