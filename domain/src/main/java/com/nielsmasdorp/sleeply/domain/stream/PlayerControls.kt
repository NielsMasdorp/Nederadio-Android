package com.nielsmasdorp.sleeply.domain.stream

import android.view.View

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Used as marker interface to bridge between domain layer and presentation layer
 */
interface PlayerControls {

    fun getView(): View
}