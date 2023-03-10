package com.nielsmasdorp.nederadio.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.PlayerControls

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class PlayerControlsView(private val view: View) : PlayerControls<View> {

    override fun getView(): View = view

    companion object {

        /**
         * Create the control views for controlling playback
         * This is needed because Exoplayer and Media3 do not support Jetpack Compose yet
         * TODO remove once jetpack compose support is present in media3
         */
        @SuppressLint("InflateParams")
        fun createViews(layoutInflater: LayoutInflater): List<PlayerControlsView> {
            return listOf(
                PlayerControlsView(
                    view = layoutInflater.inflate(
                        R.layout.exo_player_view_small,
                        null,
                        false
                    )
                ),
                PlayerControlsView(
                    view = layoutInflater.inflate(
                        R.layout.exo_ployer_view_large,
                        null,
                        false
                    )
                )
            )
        }
    }
}