package com.websbaba.nitigrow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.R

// ─────────────────────────────────────────────────────────────────────────────
// Typography — matches app/src/index.css
//   Display  → Fraunces (warm Indian serif)
//   Sans     → Geist (clean modern sans)
//   Hindi/Mr → Tiro Devanagari Hindi (when active locale uses Devanagari)
// Fonts are downloaded at runtime via the Google Fonts provider, so the APK
// stays small and any future font swap is a one-line change.
// ─────────────────────────────────────────────────────────────────────────────

private val Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val FrauncesGF = GoogleFont("Fraunces")
private val GeistGF = GoogleFont("Geist")
private val TiroHindiGF = GoogleFont("Tiro Devanagari Hindi")

val Fraunces = FontFamily(
    Font(googleFont = FrauncesGF, fontProvider = Provider, weight = FontWeight.Medium),
    Font(googleFont = FrauncesGF, fontProvider = Provider, weight = FontWeight.SemiBold),
    Font(googleFont = FrauncesGF, fontProvider = Provider, weight = FontWeight.Bold),
)

val Geist = FontFamily(
    Font(googleFont = GeistGF, fontProvider = Provider, weight = FontWeight.Normal),
    Font(googleFont = GeistGF, fontProvider = Provider, weight = FontWeight.Medium),
    Font(googleFont = GeistGF, fontProvider = Provider, weight = FontWeight.SemiBold),
    Font(googleFont = GeistGF, fontProvider = Provider, weight = FontWeight.Bold),
)

val TiroHindi = FontFamily(
    Font(googleFont = TiroHindiGF, fontProvider = Provider, weight = FontWeight.Normal),
)

// Latin (default) typography. Display slots use Fraunces; everything else
// is Geist. Sizes / line-heights / tracking mirror the app's `--den-type-*`
// + the Tailwind text-* utilities used across the dashboard.
val LatinTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp),
    displaySmall  = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    headlineLarge = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    headlineMedium= TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,    fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    titleLarge    = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium   = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, lineHeight = 22.sp),
    titleSmall    = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge     = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.Normal,    fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge    = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    labelMedium   = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    // .caps in the app — tiny tracked uppercase, used for eyebrows + table headers
    labelSmall    = TextStyle(fontFamily = Geist,    fontWeight = FontWeight.SemiBold,  fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp),
)

// Devanagari typography. Fraunces ships no Devanagari subset, so Hindi /
// Marathi locales need an explicit family override on every slot. We use
// Tiro Devanagari Hindi (matches --f-hindi in the app); body text drops
// to a tighter letter-spacing to feel right with the script.
private fun TextStyle.devanagari() = copy(
    fontFamily = TiroHindi,
    textDirection = TextDirection.ContentOrLtr,
    letterSpacing = 0.sp,
)

val DevanagariTypography = Typography(
    displayLarge  = LatinTypography.displayLarge.devanagari(),
    displayMedium = LatinTypography.displayMedium.devanagari(),
    displaySmall  = LatinTypography.displaySmall.devanagari(),
    headlineLarge = LatinTypography.headlineLarge.devanagari(),
    headlineMedium= LatinTypography.headlineMedium.devanagari(),
    headlineSmall = LatinTypography.headlineSmall.devanagari(),
    titleLarge    = LatinTypography.titleLarge.devanagari(),
    titleMedium   = LatinTypography.titleMedium.devanagari(),
    titleSmall    = LatinTypography.titleSmall.devanagari(),
    bodyLarge     = LatinTypography.bodyLarge.devanagari(),
    bodyMedium    = LatinTypography.bodyMedium.devanagari(),
    bodySmall     = LatinTypography.bodySmall.devanagari(),
    labelLarge    = LatinTypography.labelLarge.devanagari(),
    labelMedium   = LatinTypography.labelMedium.devanagari(),
    labelSmall    = LatinTypography.labelSmall.devanagari(),
)

// Picks the right scale for the active locale. Called from NitiGrowTheme so
// every recomposition reacts to language changes (per-app locale switching
// in LocaleManager is what drives this).
@Composable
@ReadOnlyComposable
fun nitiGrowTypography(): Typography {
    val lang = LocalConfiguration.current.locales[0].language
    return if (lang == "hi" || lang == "mr") DevanagariTypography else LatinTypography
}
