package com.ardym.nitigrow.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Per-app language. Survives process kill via Android 13 metadata. Tag = BCP47 ("hi", "en", "mr").
 * Empty tag = system default.
 */
object LocaleManager {
    val supported = listOf(
        "" to "System default",
        "en" to "English",
        "hi" to "हिन्दी",
        "mr" to "मराठी",
        "gu" to "ગુજરાતી",
        "ta" to "தமிழ்",
        "te" to "తెలుగు",
        "kn" to "ಕನ್ನಡ",
        "bn" to "বাংলা"
    )

    fun apply(tag: String) {
        val locales = if (tag.isBlank()) LocaleListCompat.getEmptyLocaleList()
                      else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
