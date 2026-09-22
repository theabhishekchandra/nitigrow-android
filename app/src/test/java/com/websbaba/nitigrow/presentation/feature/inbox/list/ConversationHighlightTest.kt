package com.websbaba.nitigrow.presentation.feature.inbox.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.highlighted
import org.junit.Test

class ConversationHighlightTest {

    private val style = SpanStyle(color = Color.Red)

    @Test
    fun `blank query leaves the text unstyled`() {
        val result = highlighted("Priya Sharma", "  ", style)

        assertThat(result.text).isEqualTo("Priya Sharma")
        assertThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `matches are case-insensitive and keep the original casing`() {
        val result = highlighted("Ask Priya to confirm", "priya", style)

        assertThat(result.text).isEqualTo("Ask Priya to confirm")
        assertThat(result.spanStyles).hasSize(1)
        assertThat(result.spanStyles[0].start).isEqualTo(4)
        assertThat(result.spanStyles[0].end).isEqualTo(9)
    }

    @Test
    fun `every occurrence is highlighted`() {
        val result = highlighted("ab ab", "ab", style)

        assertThat(result.spanStyles.map { it.start to it.end })
            .containsExactly(0 to 2, 3 to 5).inOrder()
    }

    @Test
    fun `no match returns plain text`() {
        val result = highlighted("Rahul Verma", "zzz", style)

        assertThat(result.text).isEqualTo("Rahul Verma")
        assertThat(result.spanStyles).isEmpty()
    }
}
