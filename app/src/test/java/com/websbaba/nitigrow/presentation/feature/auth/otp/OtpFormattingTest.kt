package com.websbaba.nitigrow.presentation.feature.auth.otp

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OtpFormattingTest {

    @Test
    fun `phone is grouped for ten digits and for a 91 prefixed number`() {
        assertThat(formatPhone("9876543210")).isEqualTo("+91 98765 43210")
        assertThat(formatPhone("919876543210")).isEqualTo("+91 98765 43210")
    }

    @Test
    fun `other lengths fall back to a plain plus number and blank reads as a placeholder`() {
        assertThat(formatPhone("14155550100")).isEqualTo("+14155550100")
        assertThat(formatPhone("+91 98765")).isEqualTo("+9198765")
        assertThat(formatPhone("")).isEqualTo("your WhatsApp number")
    }

    @Test
    fun `resend countdown renders minutes and zero padded seconds`() {
        assertThat(resendLabel(24)).isEqualTo("0:24")
        assertThat(resendLabel(5)).isEqualTo("0:05")
        assertThat(resendLabel(60)).isEqualTo("1:00")
        assertThat(resendLabel(0)).isEqualTo("0:00")
    }
}
