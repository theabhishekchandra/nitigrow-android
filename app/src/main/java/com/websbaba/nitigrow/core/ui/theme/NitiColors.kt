package com.websbaba.nitigrow.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 tonal tokens for the redesigned app (Design → "Design system").
// Same brand DNA — viridian, turmeric, terracotta on warm paper — expressed as
// M3 roles. Light uses viridian as primary; dark ("espresso") promotes turmeric.
//
// This is the app's single palette: screens read it via `Niti.colors`, and
// NitiGrowTheme derives the Material 3 ColorScheme from it.
// ─────────────────────────────────────────────────────────────────────────────

/** A container fill with the ink that sits on top of it. */
@Immutable
data class NitiTone(val container: Color, val onContainer: Color)

@Immutable
data class NitiColors(
    // Surfaces (lowest → highest emphasis)
    val surface: Color,
    val surfaceLow: Color,
    val surfaceContainer: Color,
    val surfaceHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    /** Unfilled part of progress rings and bars. */
    val track: Color,

    // Brand
    val primary: Color,
    val onPrimary: Color,

    /** Tonal containers — viridian, turmeric, terracotta and indigo in light. */
    val primaryTone: NitiTone,
    val secondaryTone: NitiTone,
    val tertiaryTone: NitiTone,
    val infoTone: NitiTone,
    /** Error container — closed windows, failed sends. */
    val errorTone: NitiTone,

    // Feature surfaces
    /** Big hero card (revenue). */
    val hero: Color,
    val onHero: Color,
    val navigationBar: Color,
    /** Unread / notification badge. */
    val badge: Color,
    val onBadge: Color,
    /** Filled series colours for charts. */
    val chartPrimary: Color,
    val chartSecondary: Color,
    /** Tile behind the brand mark. */
    val logoTile: Color,

    // Chat bubbles
    val bubbleOut: Color,
    val onBubbleOut: Color,
    val bubbleIn: Color,
    val onBubbleIn: Color,
    /** Read ticks drawn on an outbound bubble. */
    val readOnBubble: Color,

    /** Delivery ticks once a message is read. */
    val read: Color,
    val error: Color,

    // Status hues for pills, dots and icons.
    val success: Color,
    val warning: Color,
    val info: Color,
    val isLight: Boolean,
)

/** Contact-avatar fills and their initials, picked by a stable hash of the name. Same in light and dark. */
val NitiAvatarTones: List<NitiTone> = listOf(
    NitiTone(Color(0xFFF5D78C), Color(0xFF6B4A0F)),
    NitiTone(Color(0xFFF5B7A0), Color(0xFF7A2F1A)),
    NitiTone(Color(0xFFC8D9B0), Color(0xFF3A5223)),
    NitiTone(Color(0xFFE8A94A), Color(0xFF4A2F0A)),
    NitiTone(Color(0xFFB5D4D0), Color(0xFF1F4845)),
    NitiTone(Color(0xFFD9C4E8), Color(0xFF422D5A)),
    NitiTone(Color(0xFFF0C9B5), Color(0xFF5A2A12)),
)

val NitiLightColors = NitiColors(
    surface = Color(0xFFFBF8F2),
    surfaceLow = Color(0xFFF6F2E9),
    surfaceContainer = Color(0xFFF0EBDF),
    surfaceHigh = Color(0xFFEAE4D5),
    onSurface = Color(0xFF1C1B16),
    onSurfaceVariant = Color(0xFF4C473D),
    outline = Color(0xFF7A7264),
    outlineVariant = Color(0xFFD8D0BF),
    track = Color(0xFFE2DBC9),

    primary = Color(0xFF0F7F5E),
    onPrimary = Color(0xFFFFFFFF),

    primaryTone = NitiTone(Color(0xFFC4EBD8), Color(0xFF00281C)),
    secondaryTone = NitiTone(Color(0xFFFFE2A6), Color(0xFF3A2A00)),
    tertiaryTone = NitiTone(Color(0xFFFFDCCB), Color(0xFF380D00)),
    infoTone = NitiTone(Color(0xFFDCE3FF), Color(0xFF1F2F80)),
    errorTone = NitiTone(Color(0xFFF9DEDC), Color(0xFF410E0B)),

    hero = Color(0xFF0F7F5E),
    onHero = Color(0xFFFFFFFF),
    navigationBar = Color(0xFFF3EFE4),
    badge = Color(0xFFA7481C),
    onBadge = Color(0xFFFFFFFF),
    chartPrimary = Color(0xFF0F7F5E),
    chartSecondary = Color(0xFFE8A94A),
    logoTile = Color(0xFFFFFFFF),

    bubbleOut = Color(0xFF0F7F5E),
    onBubbleOut = Color(0xFFFFFFFF),
    bubbleIn = Color(0xFFEAE4D5),
    onBubbleIn = Color(0xFF1C1B16),
    readOnBubble = Color(0xFFBFE9FF),

    read = Color(0xFF3B5BD6),
    error = Color(0xFFB3261E),
    success = Color(0xFF0F7F5E),
    warning = Color(0xFFE8A94A),
    info = Color(0xFF4C6EF5),
    isLight = true,
)

val NitiDarkColors = NitiColors(
    surface = Color(0xFF14110D),
    surfaceLow = Color(0xFF1B1712),
    surfaceContainer = Color(0xFF221D16),
    surfaceHigh = Color(0xFF2B251D),
    onSurface = Color(0xFFEFE7D6),
    onSurfaceVariant = Color(0xFFC9BFAA),
    outline = Color(0xFF948B79),
    outlineVariant = Color(0xFF453E33),
    track = Color(0xFF363028),

    primary = Color(0xFFE8A94A),
    onPrimary = Color(0xFF3F2900),

    // Dark swaps the light hues one slot over: turmeric leads, viridian follows.
    primaryTone = NitiTone(Color(0xFF5A3F0A), Color(0xFFFFDEA5)),
    secondaryTone = NitiTone(Color(0xFF17382C), Color(0xFFB8F0D6)),
    tertiaryTone = NitiTone(Color(0xFF5E2A10), Color(0xFFFFDCCB)),
    infoTone = NitiTone(Color(0xFF233066), Color(0xFFB6C4FF)),
    errorTone = NitiTone(Color(0xFF601410), Color(0xFFF9DEDC)),

    hero = Color(0xFF5A3F0A),
    onHero = Color(0xFFFFDEA5),
    navigationBar = Color(0xFF1B1712),
    badge = Color(0xFFE8A94A),
    onBadge = Color(0xFF3F2900),
    chartPrimary = Color(0xFFE8A94A),
    chartSecondary = Color(0xFFE8A94A),
    logoTile = Color(0xFF0F0D0A),

    bubbleOut = Color(0xFFE8A94A),
    onBubbleOut = Color(0xFF3F2900),
    bubbleIn = Color(0xFF2C261E),
    onBubbleIn = Color(0xFFEFE7D6),
    readOnBubble = Color(0xFF3F2900),

    read = Color(0xFF8FA6FF),
    error = Color(0xFFF2B8B5),
    success = Color(0xFF16A37A),
    warning = Color(0xFFE8A94A),
    info = Color(0xFF8FA6FF),
    isLight = false,
)

/**
 * Brand Forward (AppTheme.BRAND_FORWARD, light only): Soft Paper with cream hero
 * surfaces and deep-green heading ink. In system dark it falls back to [NitiDarkColors].
 */
val NitiBrandForwardColors = NitiLightColors.copy(
    surfaceLow = Color(0xFFF7EED7),
    surfaceContainer = Color(0xFFF2E7CB),
    primaryTone = NitiTone(Color(0xFFC4EBD8), Color(0xFF1F4D36)),
)

// Default is light so disconnected @Previews render; real screens always get
// the right scheme from NitiGrowTheme.
val LocalNitiColors = staticCompositionLocalOf { NitiLightColors }

/** `Niti.colors.primary` — sugar over the CompositionLocal. */
object Niti {
    val colors: NitiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNitiColors.current
}
