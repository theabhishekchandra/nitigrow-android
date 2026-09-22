package com.websbaba.nitigrow.presentation.feature.inbox.chat

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.formatWindowRemaining
import org.junit.Test

class WindowBannerTest {

    private fun minutes(m: Long) = m * 60_000L

    @Test
    fun `hours and minutes are both shown`() {
        assertThat(formatWindowRemaining(minutes(2 * 60 + 14))).isEqualTo("2h 14m")
    }

    @Test
    fun `whole hours drop the minutes`() {
        assertThat(formatWindowRemaining(minutes(3 * 60))).isEqualTo("3h")
    }

    @Test
    fun `under an hour shows minutes only`() {
        assertThat(formatWindowRemaining(minutes(45))).isEqualTo("45m")
    }

    @Test
    fun `sub-minute remainders never read 0m`() {
        assertThat(formatWindowRemaining(30_000L)).isEqualTo("1m")
    }

    @Test
    fun `a full 24 hour window reads 24h`() {
        assertThat(formatWindowRemaining(minutes(24 * 60))).isEqualTo("24h")
    }
}
