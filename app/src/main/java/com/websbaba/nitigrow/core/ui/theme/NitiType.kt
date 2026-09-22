package com.websbaba.nitigrow.core.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.R

// ─────────────────────────────────────────────────────────────────────────────
// Redesigned type scale (Design → "Design system" → Type).
//   Bricolage Grotesque — display, headings and big numbers
//   Geist               — reading text (already declared in Type.kt)
// Devanagari glyphs are missing from both, so Android falls back per-glyph to
// the system Devanagari font for Hindi and Marathi.
// ─────────────────────────────────────────────────────────────────────────────

private val Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val BricolageGF = GoogleFont("Bricolage Grotesque")

val Bricolage = FontFamily(
    Font(googleFont = BricolageGF, fontProvider = Provider, weight = FontWeight.Medium),
    Font(googleFont = BricolageGF, fontProvider = Provider, weight = FontWeight.SemiBold),
    Font(googleFont = BricolageGF, fontProvider = Provider, weight = FontWeight.Bold),
)

object NitiType {
    /** 34/40 · 600 — screen greeting / hero title. */
    val display = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.9).sp,
    )

    /** 26/32 · 600 */
    val headline = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp,
    )

    /** 22/28 · 600 */
    val title = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp,
    )

    /** 16/22 · 600 — card and section titles. */
    val titleUi = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    )

    /** 15/22 · 400 */
    val body = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    )

    /** 15/20 · 500 — list row titles. */
    val bodyStrong = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp,
    )

    /** 14/20 · 400 — dense secondary text such as list previews. */
    val bodyCompact = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )

    /** 13/18 · 500 */
    val label = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp,
    )

    /** 12/16 · 500 */
    val caption = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    )

    // Numerals — Bricolage bold, tightened as they grow.
    val numberXl = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-1.5).sp,
    )
    val numberLg = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-1).sp,
    )
    val numberMd = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.5).sp,
    )
    val numberSm = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 18.sp, lineHeight = 24.sp,
    )
}
