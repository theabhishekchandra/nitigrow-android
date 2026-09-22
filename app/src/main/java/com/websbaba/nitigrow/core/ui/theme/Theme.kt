package com.websbaba.nitigrow.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// NitiGrowTheme — single Compose entry-point for the whole app's visual layer.
//   • Provides the warm-Indian-premium NitiColors via CompositionLocal.
//   • Mirrors a minimal Material 3 ColorScheme so stock components (Snackbar,
//     TextField, etc.) inherit the right brand colours without per-site work.
//   • Locks status bar tint + light/dark appearance to the brand top bar so
//     the splash → app transition feels continuous.
//   • Routes typography through nitiGrowTypography() so it auto-swaps to
//     Tiro Devanagari Hindi when the active locale is `hi` or `mr`.
//
// Note: dynamicColor (Material You) is deliberately OFF — this is a branded
// product, not a Material showcase. The viridian brand must look the same on
// every device.
// ─────────────────────────────────────────────────────────────────────────────

private fun buildColorScheme(c: NitiColors): ColorScheme {
    val tones = c.errorTone
    return if (c.isLight) {
        lightColorScheme(
            primary = c.primary,
            onPrimary = c.onPrimary,
            primaryContainer = c.primaryTone.container,
            onPrimaryContainer = c.primaryTone.onContainer,
            secondary = c.secondaryTone.onContainer,
            onSecondary = c.surface,
            secondaryContainer = c.secondaryTone.container,
            onSecondaryContainer = c.secondaryTone.onContainer,
            tertiary = c.tertiaryTone.onContainer,
            onTertiary = c.surface,
            tertiaryContainer = c.tertiaryTone.container,
            onTertiaryContainer = c.tertiaryTone.onContainer,
            background = c.surface,
            onBackground = c.onSurface,
            surface = c.surface,
            onSurface = c.onSurface,
            surfaceVariant = c.surfaceContainer,
            onSurfaceVariant = c.onSurfaceVariant,
            surfaceTint = c.primary,
            error = c.error,
            onError = c.surface,
            errorContainer = tones.container,
            onErrorContainer = tones.onContainer,
            outline = c.outline,
            outlineVariant = c.outlineVariant,
        )
    } else {
        darkColorScheme(
            primary = c.primary,
            onPrimary = c.onPrimary,
            primaryContainer = c.primaryTone.container,
            onPrimaryContainer = c.primaryTone.onContainer,
            secondary = c.secondaryTone.onContainer,
            onSecondary = c.surface,
            secondaryContainer = c.secondaryTone.container,
            onSecondaryContainer = c.secondaryTone.onContainer,
            tertiary = c.tertiaryTone.onContainer,
            onTertiary = c.surface,
            tertiaryContainer = c.tertiaryTone.container,
            onTertiaryContainer = c.tertiaryTone.onContainer,
            background = c.surface,
            onBackground = c.onSurface,
            surface = c.surface,
            onSurface = c.onSurface,
            surfaceVariant = c.surfaceContainer,
            onSurfaceVariant = c.onSurfaceVariant,
            surfaceTint = c.primary,
            error = c.error,
            onError = tones.container,
            errorContainer = tones.container,
            onErrorContainer = tones.onContainer,
            outline = c.outline,
            outlineVariant = c.outlineVariant,
        )
    }
}

@Composable
fun NitiGrowTheme(
    appTheme: AppTheme = AppTheme.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Palette per exploration theme:
    //  • SOFT_PAPER follows the system light/dark toggle.
    //  • BRAND_FORWARD is a light variant; in system dark it falls back to the dark palette.
    //  • ESPRESSO_PREMIUM is inherently dark and ignores the system toggle.
    val colors = when (appTheme) {
        AppTheme.SOFT_PAPER -> if (darkTheme) NitiDarkColors else NitiLightColors
        AppTheme.BRAND_FORWARD -> if (darkTheme) NitiDarkColors else NitiBrandForwardColors
        AppTheme.ESPRESSO_PREMIUM -> NitiDarkColors
    }

    // Baseline status bar: viridian behind light icons in light palettes, the surface in
    // dark ones (bright turmeric would be illegible behind light icons). Screens refine it
    // through NitiStatusBar. Navigation-bar icons follow the *palette's* lightness so
    // Espresso Premium gets light icons even when the device is in light mode.
    val statusBar = if (colors.isLight) colors.primary else colors.surface
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBar.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = colors.isLight
        }
    }

    CompositionLocalProvider(LocalNitiColors provides colors) {
        MaterialTheme(
            colorScheme = buildColorScheme(colors),
            typography = nitiGrowTypography(),
            shapes = NitiGrowShapes,
            content = content,
        )
    }
}
