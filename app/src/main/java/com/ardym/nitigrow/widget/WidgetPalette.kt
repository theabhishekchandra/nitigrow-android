package com.ardym.nitigrow.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Fixed design-token colors for the home-screen widgets.
 *
 * The widget cards are fixed-color surfaces by design (the espresso card stays
 * espresso in both light and dark mode, and the Quick Broadcast card stays
 * paper-light, like the design comp). The app's GlanceTheme has no custom
 * brand color scheme set up, so we pin the design hex values here instead of
 * routing through GlanceTheme.colors — both cards remain legible on any
 * wallpaper and in dark mode because text colors are fixed to their own card.
 */
internal object WidgetPalette {
    /** #FBF8F3 — paper, used as text-on-accent and text-on-brand. */
    val paper = ColorProvider(Color(0xFFFBF8F3))

    /** #1A1714 — ink, primary text on the light card. */
    val ink = ColorProvider(Color(0xFF1A1714))

    /** #85796D — muted, secondary text on the light card. */
    val muted = ColorProvider(Color(0xFF85796D))

    /** #F5EFDF — cream, primary text on the espresso card. */
    val cream = ColorProvider(Color(0xFFF5EFDF))

    /** #F5D78C — gold, festival glyph on the espresso card. */
    val gold = ColorProvider(Color(0xFFF5D78C))

    /** #877D6A — muted-gold, ALL-CAPS kicker on the espresso card. */
    val espressoKicker = ColorProvider(Color(0xFF877D6A))

    /** #A89E87 — muted helper text on the espresso card. */
    val espressoMuted = ColorProvider(Color(0xFFA89E87))

    /** #4A2F0A — dark text on the turmeric pill button. */
    val turmericInk = ColorProvider(Color(0xFF4A2F0A))
}
