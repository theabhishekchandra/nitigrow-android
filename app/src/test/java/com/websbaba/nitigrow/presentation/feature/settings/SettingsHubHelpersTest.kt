package com.websbaba.nitigrow.presentation.feature.settings

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.util.LocaleManager
import com.websbaba.nitigrow.domain.model.WabaStatus
import org.junit.Test

class SettingsHubHelpersTest {

    @Test
    fun `every WABA status has a pill and only connected reads as positive`() {
        WabaStatus.entries.forEach { assertThat(wabaPillFor(it).label).isNotEmpty() }

        assertThat(wabaPillFor(WabaStatus.ACTIVE)).isEqualTo(PillSpec("CONNECTED", PillKind.POSITIVE))
        assertThat(wabaPillFor(WabaStatus.PENDING).kind).isEqualTo(PillKind.WARNING)
        assertThat(wabaPillFor(WabaStatus.SUSPENDED).kind).isEqualTo(PillKind.ERROR)
        assertThat(wabaPillFor(WabaStatus.FAILED).kind).isEqualTo(PillKind.ERROR)
        assertThat(wabaPillFor(WabaStatus.NOT_LINKED).kind).isEqualTo(PillKind.NEUTRAL)
    }

    @Test
    fun `team label is singular for one member`() {
        assertThat(teamLabel(1)).isEqualTo("1 member")
        assertThat(teamLabel(3)).isEqualTo("3 members")
    }

    @Test
    fun `rupees are converted from paise and grouped`() {
        assertThat(formatRupees(200_000)).isEqualTo("₹2,000")
        assertThat(formatRupees(99)).isEqualTo("₹0")
    }

    @Test
    fun `every supported language has a gloss and system default explains itself`() {
        assertThat(languageGloss("")).isEqualTo("Follows your phone")
        assertThat(languageGloss("hi")).isEqualTo("Hindi")
        LocaleManager.supported.forEach { (tag, _) ->
            // A missing entry would fall back to echoing the raw tag, which is only right for "en".
            if (tag != "en") assertThat(languageGloss(tag)).isNotEqualTo(tag)
        }
    }
}
