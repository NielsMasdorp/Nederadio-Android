package com.nielsmasdorp.sleeply.ui.stream

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.media3.ui.PlayerControlView
import com.nielsmasdorp.sleeply.domain.stream.PlayerControls

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@SuppressLint("UnsafeOptInUsageError")
class SleeplyPlayerControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PlayerControlView(context, attrs, defStyle), PlayerControls {

    init {
        showTimeoutMs = 0 // always show
    }

    override fun getView(): View = this
}