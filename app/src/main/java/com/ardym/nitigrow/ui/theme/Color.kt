package com.ardym.nitigrow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// NitiGrow palette — "Warm Indian Premium" (mirrors app/src/index.css)
// Deep viridian brand, terracotta + turmeric accents, warm-stone neutrals.
// Material 3's ColorScheme only covers ~25 roles; we surface the rest via
// the NitiGrowColors data class + CompositionLocal so every screen can read
// `Theme.colors.brandSoft` etc. directly.
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class NitiGrowColors(
    // Brand — viridian
    val brand: Color,
    val brandHover: Color,
    val brandSoft: Color,
    val brandInk: Color,
    val brandRing: Color,

    // Accents — terracotta (clay) + turmeric (haldi)
    val accent: Color,
    val accentSoft: Color,
    val turmeric: Color,
    val turmericSoft: Color,
    val turmericInk: Color,

    // Surfaces — warm cream paper
    val paper: Color,
    val paper2: Color,
    val paper3: Color,
    val card: Color,
    val cardHover: Color,

    // Ink — warm near-black, not cold zinc
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val muted: Color,
    val muted2: Color,
    val muted3: Color,

    // Hairlines — warm sand
    val border: Color,
    val border2: Color,
    val border3: Color,

    // Semantic
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,

    // Chat bubbles — match app/src/index.css .bubble-{in,out}
    val bubbleOut: Color,
    val bubbleOutInk: Color,
    val bubbleIn: Color,
    val bubbleInInk: Color,
    val bubbleInBorder: Color,

    // Sidebar (dark espresso strip on tablets / future split-pane)
    val sidebarBg: Color,
    val sidebarInk: Color,
    val sidebarActive: Color,
    val sidebarTextActive: Color,

    // Avatar palette — warm tones, 7-way rotation (matches .av-0 .. .av-6)
    val avatars: List<Pair<Color, Color>>, // (background, foreground)

    val isLight: Boolean,
)

// Convenience constants — keep raw hex centralised so the dark scheme can
// reference the same source values where they overlap.
private object Hex {
    val BrandLight       = Color(0xFF0F7F5E)
    val BrandHoverLight  = Color(0xFF16A37A)
    val BrandSoftLight   = Color(0xFFE6F4EE)
    val BrandInkLight    = Color(0xFF063527)
    val BrandRingLight   = Color(0x290F7F5E) // rgba(15,127,94,0.16)

    val BrandDark        = Color(0xFF16A37A)
    val BrandHoverDark   = Color(0xFF1CC291)
    val BrandSoftDark    = Color(0x1F16A37A) // rgba(22,163,122,0.12)
    val BrandInkDark     = Color(0xFFD1EFE3)
    val BrandRingDark    = Color(0x3D16A37A) // rgba(22,163,122,0.24)

    val Accent           = Color(0xFFC55A2B)
    val AccentSoftLight  = Color(0xFFFBEADF)
    val AccentSoftDark   = Color(0x24C55A2B) // rgba(197,90,43,0.14)
    val Turmeric         = Color(0xFFE8A94A)
    val TurmericSoftLight = Color(0xFFFCF1D9)
    val TurmericSoftDark  = Color(0x24E8A94A) // rgba(232,169,74,0.14)
    val TurmericInkLight  = Color(0xFFB07818) // text on turmericSoft (pills, banners)
    val TurmericInkDark   = Color(0xFFF5D78C)

    val PaperLight       = Color(0xFFFBF8F3)
    val Paper2Light      = Color(0xFFF5F0E6)
    val Paper3Light      = Color(0xFFEEE7D8)
    val CardLight        = Color(0xFFFFFDF8)
    val CardHoverLight   = Color(0xFFFAF6EC)

    val PaperDark        = Color(0xFF12100C)
    val Paper2Dark       = Color(0xFF1A1712)
    val Paper3Dark       = Color(0xFF231E17)
    val CardDark         = Color(0xFF1A1712)
    val CardHoverDark    = Color(0xFF231E17)

    val InkLight         = Color(0xFF1A1714)
    val Ink2Light        = Color(0xFF2E2A25)
    val Ink3Light        = Color(0xFF5A534B)
    val MutedLight       = Color(0xFF85796D)
    val Muted2Light      = Color(0xFFB8AE9F)
    val Muted3Light      = Color(0xFFD9D1C3)

    val InkDark          = Color(0xFFF3ECD9)
    val Ink2Dark         = Color(0xFFD9D0B9)
    val Ink3Dark         = Color(0xFFA89E87)
    val MutedDark        = Color(0xFF877D6A)
    val Muted2Dark       = Color(0xFF5C5447)
    val Muted3Dark       = Color(0xFF3A3429)

    val BorderLight      = Color(0xFFE8DFCC)
    val Border2Light     = Color(0xFFF0E9D9)
    val Border3Light     = Color(0xFFD9D1C3)
    val BorderDark       = Color(0xFF2D2820)
    val Border2Dark      = Color(0xFF221D17)
    val Border3Dark      = Color(0xFF3A3429)

    val Success          = Color(0xFF0F7F5E)
    val Warning          = Color(0xFFE8A94A)
    val Danger           = Color(0xFFC03B3B)
    val Info             = Color(0xFF4C6EF5)

    // Sidebar (used the same in light + dark — always espresso)
    val SidebarBg          = Color(0xFF1F1A14)
    val SidebarInk         = Color(0xFFF5EFDF)
    val SidebarActive      = Color(0x24E8A94A) // rgba(232,169,74,0.14)
    val SidebarTextActive  = Color(0xFFF5D78C)

    // Avatar palette — warm tones (matches app/.av-0..6)
    val Avatars = listOf(
        Color(0xFFF5D78C) to Color(0xFF6B4A0F),
        Color(0xFFF5B7A0) to Color(0xFF7A2F1A),
        Color(0xFFC8D9B0) to Color(0xFF3A5223),
        Color(0xFFE8A94A) to Color(0xFF4A2F0A),
        Color(0xFFB5D4D0) to Color(0xFF1F4845),
        Color(0xFFD9C4E8) to Color(0xFF422D5A),
        Color(0xFFF0C9B5) to Color(0xFF5A2A12),
    )
}

val NitiGrowLightColors = NitiGrowColors(
    brand          = Hex.BrandLight,
    brandHover     = Hex.BrandHoverLight,
    brandSoft      = Hex.BrandSoftLight,
    brandInk       = Hex.BrandInkLight,
    brandRing      = Hex.BrandRingLight,

    accent         = Hex.Accent,
    accentSoft     = Hex.AccentSoftLight,
    turmeric       = Hex.Turmeric,
    turmericSoft   = Hex.TurmericSoftLight,
    turmericInk    = Hex.TurmericInkLight,

    paper          = Hex.PaperLight,
    paper2         = Hex.Paper2Light,
    paper3         = Hex.Paper3Light,
    card           = Hex.CardLight,
    cardHover      = Hex.CardHoverLight,

    ink            = Hex.InkLight,
    ink2           = Hex.Ink2Light,
    ink3           = Hex.Ink3Light,
    muted          = Hex.MutedLight,
    muted2         = Hex.Muted2Light,
    muted3         = Hex.Muted3Light,

    border         = Hex.BorderLight,
    border2        = Hex.Border2Light,
    border3        = Hex.Border3Light,

    success        = Hex.Success,
    warning        = Hex.Warning,
    danger         = Hex.Danger,
    info           = Hex.Info,

    bubbleOut      = Hex.BrandLight,
    bubbleOutInk   = Hex.PaperLight,
    bubbleIn       = Color(0xFFFFFFFF),
    bubbleInInk    = Hex.InkLight,
    bubbleInBorder = Hex.BorderLight,

    sidebarBg          = Hex.SidebarBg,
    sidebarInk         = Hex.SidebarInk,
    sidebarActive      = Hex.SidebarActive,
    sidebarTextActive  = Hex.SidebarTextActive,

    avatars        = Hex.Avatars,
    isLight        = true,
)

val NitiGrowDarkColors = NitiGrowColors(
    brand          = Hex.BrandDark,
    brandHover     = Hex.BrandHoverDark,
    brandSoft      = Hex.BrandSoftDark,
    brandInk       = Hex.BrandInkDark,
    brandRing      = Hex.BrandRingDark,

    accent         = Hex.Accent,
    accentSoft     = Hex.AccentSoftDark,
    turmeric       = Hex.Turmeric,
    turmericSoft   = Hex.TurmericSoftDark,
    turmericInk    = Hex.TurmericInkDark,

    paper          = Hex.PaperDark,
    paper2         = Hex.Paper2Dark,
    paper3         = Hex.Paper3Dark,
    card           = Hex.CardDark,
    cardHover      = Hex.CardHoverDark,

    ink            = Hex.InkDark,
    ink2           = Hex.Ink2Dark,
    ink3           = Hex.Ink3Dark,
    muted          = Hex.MutedDark,
    muted2         = Hex.Muted2Dark,
    muted3         = Hex.Muted3Dark,

    border         = Hex.BorderDark,
    border2        = Hex.Border2Dark,
    border3        = Hex.Border3Dark,

    success        = Hex.BrandDark,
    warning        = Hex.Warning,
    danger         = Color(0xFFE87A7A),
    info           = Color(0xFF7A96F7),

    bubbleOut      = Hex.BrandDark,
    bubbleOutInk   = Color(0xFF052A1E),
    bubbleIn       = Hex.Paper3Dark,
    bubbleInInk    = Hex.InkDark,
    bubbleInBorder = Hex.BorderDark,

    sidebarBg          = Color(0xFF0C0A07),
    sidebarInk         = Color(0xFFF3ECD9),
    sidebarActive      = Hex.SidebarActive,
    sidebarTextActive  = Hex.SidebarTextActive,

    avatars        = Hex.Avatars,
    isLight        = false,
)

// CompositionLocal default kept light to avoid NPE in disconnected Previews;
// real screens always receive the right scheme through NitiGrowTheme.
val LocalNitiGrowColors = staticCompositionLocalOf { NitiGrowLightColors }
