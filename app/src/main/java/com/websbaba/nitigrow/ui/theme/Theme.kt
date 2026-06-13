package com.websbaba.nitigrow.ui.theme

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
    appTheme: AppTheme = AppTheme.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Palette per exploration theme:
    //  • SOFT_PAPER follows the system light/dark toggle (classic behaviour).
    //  • BRAND_FORWARD is a light-theme variant; in system dark it falls back
    //    to the standard dark palette so text stays readable.
    //  • ESPRESSO_PREMIUM is inherently dark and ignores the system toggle.
    val niti = when (appTheme) {
        AppTheme.SOFT_PAPER ->
            if (darkTheme) NitiGrowDarkColors else NitiGrowLightColors
        AppTheme.BRAND_FORWARD ->
            if (darkTheme) NitiGrowDarkColors else BrandForwardColors
        AppTheme.ESPRESSO_PREMIUM -> EspressoPremiumColors
    }
    val m3 = if (niti.isLight) buildLightScheme(niti) else buildDarkScheme(niti)

    // Status bar: brand tint as before — except Espresso Premium, whose brand
    // is bright turmeric (illegible behind light icons, jarring above dark
    // surfaces), so it uses the palette's paper instead. Navigation-bar
    // appearance keys off the *palette's* lightness (not the system toggle)
    // so Espresso Premium always gets light nav icons even when the device is
    // in light mode.
    val statusBar = if (appTheme == AppTheme.ESPRESSO_PREMIUM) niti.paper else niti.brand
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBar.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = niti.isLight
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
