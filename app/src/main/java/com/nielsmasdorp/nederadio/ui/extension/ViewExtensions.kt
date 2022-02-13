package com.nielsmasdorp.nederadio.ui.extension

import android.content.res.ColorStateList
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import androidx.media3.ui.PlayerControlView

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Set colors for the relevant controls
 */
fun PlayerControlView.setColors(playPauseColor: Int, controlColor: Int) {
    children.first().findViewWithTag<AppCompatImageView>("play")?.apply {
        backgroundTintList = ColorStateList.valueOf(playPauseColor)
        imageTintList = ColorStateList.valueOf(playPauseColor)
    }
    children.first().findViewWithTag<AppCompatImageView>("pause")?.apply {
        backgroundTintList = ColorStateList.valueOf(playPauseColor)
        imageTintList = ColorStateList.valueOf(playPauseColor)
    }
    children.first().findViewWithTag<AppCompatImageView>("prev")?.backgroundTintList =
        ColorStateList.valueOf(controlColor)
    children.first().findViewWithTag<AppCompatImageView>("next")?.backgroundTintList =
        ColorStateList.valueOf(controlColor)
    children.first().findViewWithTag<AppCompatImageView>("shuffle")?.imageTintList =
        ColorStateList.valueOf(controlColor)
}