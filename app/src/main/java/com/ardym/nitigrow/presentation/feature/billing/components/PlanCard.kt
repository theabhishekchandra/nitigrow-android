package com.ardym.nitigrow.presentation.feature.billing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// PlanCard — one plan option inside the "Choose a plan" bottom sheet.
//
//   ┌──────────────────────────────────────┐         ┌──────────(CURRENT)┐
//   │ Starter                    ₹999/mo   │         │ Growth   ₹1,499/mo│ ← 2dp brand
//   │ 1,000 conversations · 2 seats        │         │ 2,500 conv · 5 …  │   border +
//   └──────────────────────────────────────┘         └───────────────────┘   brandSoft bg
//
// The CURRENT plan gets a floating mini-pill straddling the top-right border.
// ─────────────────────────────────────────────────────────────────────────────

private val nf = NumberFormat.getInstance(Locale("en", "IN"))

@Composable
fun PlanCard(
    plan: Plan,
    isCurrent: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Theme.colors
    val shape = RoundedCornerShape(15.dp)
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp) // leaves room for the floating CURRENT pill
                .clip(shape)
                .background(if (isCurrent) colors.brandSoft else colors.card)
                .border(
                    width = if (isCurrent) 2.dp else 1.dp,
                    color = if (isCurrent) colors.brand else colors.border,
                    shape = shape,
                )
                .clickable(enabled = enabled && !isCurrent, onClick = onClick)
                .padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = plan.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${nf.format(plan.priceInr)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink,
                    )
                    Text(
                        text = periodLabel(plan.periodDays),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 1.dp),
                    )
                }
            }
            if (plan.features.isNotEmpty()) {
                Spacer(Modifier.padding(top = 4.dp))
                Text(
                    text = plan.features.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) colors.ink2 else colors.muted,
                )
            }
        }
        if (isCurrent) {
            Text(
                text = "CURRENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = if (colors.isLight) colors.paper else colors.brandInk,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.brand)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }
}

private fun periodLabel(periodDays: Int): String =
    if (periodDays in 28..31) "/mo" else "/${periodDays}d"

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "PlanCard — current")
@Composable
private fun PreviewPlanCardCurrent() {
    NitiGrowTheme {
        PlanCard(
            plan = Plan(
                id = "growth", name = "Growth", priceInr = 1_499, periodDays = 30,
                features = listOf("2,500 conversations", "5 seats", "leads + analytics"),
                isPopular = true,
            ),
            isCurrent = true,
            enabled = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "PlanCard — other")
@Composable
private fun PreviewPlanCardOther() {
    NitiGrowTheme {
        PlanCard(
            plan = Plan(
                id = "starter", name = "Starter", priceInr = 999, periodDays = 30,
                features = listOf("1,000 conversations", "2 seats"),
                isPopular = false,
            ),
            isCurrent = false,
            enabled = true,
            onClick = {},
        )
    }
}
