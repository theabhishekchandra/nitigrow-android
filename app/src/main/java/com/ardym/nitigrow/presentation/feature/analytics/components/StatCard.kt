package com.ardym.nitigrow.presentation.feature.analytics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.ui.theme.Theme

private val CardPadding: Dp = 14.dp
private val TitleGap: Dp = 6.dp
private val TrendGap: Dp = 4.dp

/**
 * Compact analytics tile — uppercase label, big number, optional trend %.
 * Positive trends render in `success`, negative in `danger`, neutral (null) in `muted`.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    trendPct: Float? = null,
    subtitle: String? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
    ) {
        Column(modifier = Modifier.padding(CardPadding)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(TitleGap))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.ink,
            )
            if (trendPct != null || subtitle != null) {
                Spacer(Modifier.height(TrendGap))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trendPct != null) {
                        val sign = if (trendPct >= 0f) "+" else ""
                        val color = when {
                            trendPct > 0f -> Theme.colors.success
                            trendPct < 0f -> Theme.colors.danger
                            else -> Theme.colors.muted
                        }
                        Text(
                            text = "$sign${"%.1f".format(trendPct)}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = if (trendPct != null) "  $subtitle" else subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = Theme.colors.muted,
                        )
                    }
                }
            }
        }
    }
}
