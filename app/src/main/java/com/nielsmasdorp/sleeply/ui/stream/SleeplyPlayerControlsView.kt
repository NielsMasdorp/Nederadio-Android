package com.nielsmasdorp.sleeply.ui.stream

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerControlView
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@UnstableApi
class SleeplyPlayerControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PlayerControlView(context, attrs, defStyle), PlayerControls {

    init {
        showTimeoutMs = 0 // always show
    }

    /**
     * Set colors for the relevant controls
     */
    fun setColors(playPauseColor: Int, controlColor: Int) {
        children.first().findViewWithTag<AppCompatImageView>("play").backgroundTintList =
            ColorStateList.valueOf(playPauseColor)
        children.first().findViewWithTag<AppCompatImageView>("pause").backgroundTintList =
            ColorStateList.valueOf(playPauseColor)
        children.first().findViewWithTag<AppCompatImageView>("prev").backgroundTintList =
            ColorStateList.valueOf(controlColor)
        children.first().findViewWithTag<AppCompatImageView>("next").backgroundTintList =
            ColorStateList.valueOf(controlColor)
    }

    override fun getView(): View = this
}