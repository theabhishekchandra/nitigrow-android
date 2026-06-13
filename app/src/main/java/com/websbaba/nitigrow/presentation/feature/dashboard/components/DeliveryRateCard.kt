package com.websbaba.nitigrow.presentation.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme

/** Labelled progress bar — 8dp paper2 track, 4dp radius, brand/turmeric/accent fill. */
@Composable
fun RateRow(
    label: String,
    rate: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = Theme.colors.brand
) {
    val fraction = rate.coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Theme.colors.ink3
            )
            Text(
                text = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.ink
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Theme.colors.paper2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(fillColor)
            )
        }
    }
}

/** Engagement card — delivery (brand) + read (turmeric) bars on a KPI-style card. */
@Composable
fun DeliveryReadCard(deliveryRate: Float, readRate: Float, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.colors.card)
            .border(1.dp, Theme.colors.border, shape)
            .padding(14.dp)
    ) {
        Text(
            text = "ENGAGEMENT",
            style = MaterialTheme.typography.labelSmall,
            color = Theme.colors.muted
        )
        Spacer(Modifier.height(12.dp))
        RateRow("Delivery rate", deliveryRate, fillColor = Theme.colors.brand)
        Spacer(Modifier.height(10.dp))
        RateRow("Read rate", readRate, fillColor = Theme.colors.turmeric)
    }
}

@Preview(showBackground = true)
@Composable
private fun DeliveryReadCardPreview() {
    NitiGrowTheme {
        Box(Modifier.background(Theme.colors.paper).padding(18.dp)) {
            DeliveryReadCard(deliveryRate = 0.96f, readRate = 0.81f)
        }
    }
}
