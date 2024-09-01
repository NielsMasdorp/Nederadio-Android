package com.nielsmasdorp.nederadio.ui.extension

import android.content.res.ColorStateList
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerControlView

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Set colors for the relevant controls
 */
@OptIn(UnstableApi::class)
fun PlayerControlView.setColors(playPauseColor: Int) {
    children.first().findViewWithTag<AppCompatImageView>("playPause")?.apply {
        backgroundTintList = ColorStateList.valueOf(playPauseColor)
        imageTintList = ColorStateList.valueOf(playPauseColor)
    }
}
