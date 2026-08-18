package com.avatarsdk.metaperson

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Pads [view] by the system bar and display cutout insets.
 *
 * Needed from targetSdk 36 onwards: apps always draw edge-to-edge on Android 16 and can no
 * longer opt out, so content would otherwise render underneath the status and navigation bars.
 */
fun applySystemBarInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}
