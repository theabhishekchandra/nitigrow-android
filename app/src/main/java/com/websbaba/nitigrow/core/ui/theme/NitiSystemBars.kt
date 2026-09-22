package com.websbaba.nitigrow.core.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Bookkeeping for [NitiStatusBar]. Screens overlap during navigation: the
 * incoming screen applies its colours *before* the outgoing one disposes, so a
 * naive "restore on dispose" would undo the new screen's setting. Instead the
 * original look is saved by the first active override and restored only when
 * the last one leaves.
 */
private object StatusBarOverrides {
    private var active = 0
    private var savedColor = 0
    private var savedLightIcons = false

    fun acquire(currentColor: Int, currentLightIcons: Boolean) {
        if (active == 0) {
            savedColor = currentColor
            savedLightIcons = currentLightIcons
        }
        active++
    }

    /** Returns the original look once the last override is released, otherwise null. */
    fun release(): Pair<Int, Boolean>? {
        active = (active - 1).coerceAtLeast(0)
        return if (active == 0) savedColor to savedLightIcons else null
    }
}

/**
 * Paints the status bar [color] with light or dark icons while this composable
 * is on screen, restoring the previous look when the last such screen leaves.
 * Lets redesigned screens sit flush under the status bar while unmigrated ones
 * keep the brand band.
 */
@Composable
fun NitiStatusBar(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(color, darkIcons) {
        val window = (view.context as Activity).window
        val controls = WindowCompat.getInsetsController(window, view)
        StatusBarOverrides.acquire(window.statusBarColor, controls.isAppearanceLightStatusBars)
        window.statusBarColor = color.toArgb()
        controls.isAppearanceLightStatusBars = darkIcons
        onDispose {
            StatusBarOverrides.release()?.let { (savedColor, savedIcons) ->
                window.statusBarColor = savedColor
                controls.isAppearanceLightStatusBars = savedIcons
            }
        }
    }
}
