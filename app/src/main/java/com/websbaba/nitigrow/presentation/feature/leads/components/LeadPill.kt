package com.websbaba.nitigrow.presentation.feature.leads.components

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType

/** Stage chip shared by the list card and the detail header. */
@Composable
fun LeadStagePill(stage: LeadStage, modifier: Modifier = Modifier) {
    val tone = stageTone(stage, Niti.colors)
    Text(
        text = stage.label,
        style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        color = tone.onContainer,
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tone.container)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}
