package com.ardym.nitigrow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// NitiGrowTheme — single Compose entry-point for the whole app's visual layer.
//   • Provides the warm-Indian-premium NitiGrowColors via CompositionLocal.
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

private fun buildLightScheme(c: NitiGrowColors) = lightColorScheme(
    primary           = c.brand,
    onPrimary         = c.paper,
    primaryContainer  = c.brandSoft,
    onPrimaryContainer= c.brandInk,

    secondary         = c.accent,
    onSecondary       = c.paper,
    secondaryContainer= c.accentSoft,
    onSecondaryContainer = c.accent,

    tertiary          = c.turmeric,
    onTertiary        = c.brandInk,
    tertiaryContainer = c.turmericSoft,
    onTertiaryContainer = c.ink2,

    background        = c.paper,
    onBackground      = c.ink,
    surface           = c.card,
    onSurface         = c.ink,
    surfaceVariant    = c.paper2,
    onSurfaceVariant  = c.ink3,
    surfaceTint       = c.brand,

    error             = c.danger,
    onError           = c.paper,
    errorContainer    = c.accentSoft,
    onErrorContainer  = c.danger,

    outline           = c.border,
    outlineVariant    = c.border2,
)

private fun buildDarkScheme(c: NitiGrowColors) = darkColorScheme(
    primary           = c.brand,
    onPrimary         = c.brandInk,
    primaryContainer  = c.brandSoft,
    onPrimaryContainer= c.brandInk,

    secondary         = c.accent,
    onSecondary       = c.paper,
    secondaryContainer= c.accentSoft,
    onSecondaryContainer = c.accent,

    tertiary          = c.turmeric,
    onTertiary        = c.brandInk,
    tertiaryContainer = c.turmericSoft,
    onTertiaryContainer = c.ink,

    background        = c.paper,
    onBackground      = c.ink,
    surface           = c.card,
    onSurface         = c.ink,
    surfaceVariant    = c.paper2,
    onSurfaceVariant  = c.ink3,
    surfaceTint       = c.brand,

    error             = c.danger,
    onError           = c.paper,
    errorContainer    = c.accentSoft,
    onErrorContainer  = c.danger,

    outline           = c.border,
    outlineVariant    = c.border2,
)

@Composable
fun NitiGrowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val niti = if (darkTheme) NitiGrowDarkColors else NitiGrowLightColors
    val m3 = if (darkTheme) buildDarkScheme(niti) else buildLightScheme(niti)

    // Tint the status bar to brand viridian and flip its icons. Light-mode
    // brand is dark enough that icons must be light (false); dark-mode keeps
    // the same brand but in a darker surrounding, so icons still go light.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = niti.brand.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalNitiGrowColors provides niti) {
        MaterialTheme(
            colorScheme = m3,
            typography = nitiGrowTypography(),
            shapes = NitiGrowShapes,
            content = content,
        )
    }
}

// Sugar so screens can write `Theme.colors.brand` / `Theme.shapes.medium`
// instead of unwrapping the CompositionLocal every time.
object Theme {
    val colors: NitiGrowColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNitiGrowColors.current
}
