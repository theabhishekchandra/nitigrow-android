package com.ardym.nitigrow.presentation.feature.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RateRow(label: String, rate: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label: ${(rate * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { rate.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}

@Composable
fun DeliveryReadCard(deliveryRate: Float, readRate: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Engagement",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            RateRow("Delivery rate", deliveryRate)
            Spacer(Modifier.height(8.dp))
            RateRow("Read rate", readRate)
        }
    }
}
