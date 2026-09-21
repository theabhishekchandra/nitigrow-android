package com.websbaba.nitigrow.presentation.components.a11y

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// A11yPreviews — design-review surface for the a11y helpers. These previews
// are not used at runtime; they exist so designers + reviewers can eyeball
// the helpers without spinning up an Activity.
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "A11y helpers — sampler")
@Composable
private fun A11ySamplerPreview() {
    NitiGrowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Theme.colors.paper)
                .padding(16.dp),
        ) {
            Text(
                text = "Accessibility helpers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.ink,
                modifier = Modifier.semanticHeading(),
            )

            Text(
                text = "1. A11yIconButton — enforced contentDescription + 48dp touch target",
                style = MaterialTheme.typography.titleSmall,
                color = Theme.colors.ink2,
                modifier = Modifier.semanticHeading(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                A11yIconButton(onClick = {}, contentDescription = "Send message") {
                    Icon(Icons.Filled.Send, contentDescription = null, tint = Theme.colors.brand)
                }
                A11yIconButton(onClick = {}, contentDescription = "Open notifications") {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Theme.colors.accent)
                }
            }

            Text(
                text = "2. A11yStatusDot — colour + spoken label",
                style = MaterialTheme.typography.titleSmall,
                color = Theme.colors.ink2,
                modifier = Modifier.semanticHeading(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabeledDot(color = Theme.colors.success, text = "Delivered")
                LabeledDot(color = Theme.colors.warning, text = "Pending")
                LabeledDot(color = Theme.colors.danger,  text = "Failed")
            }

            Text(
                text = "3. Modifier.minTouchTarget() — visible 24dp, tappable 48dp",
                style = MaterialTheme.typography.titleSmall,
                color = Theme.colors.ink2,
                modifier = Modifier.semanticHeading(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                A11yIconButton(
                    onClick = {},
                    contentDescription = "Demo action",
                    modifier = Modifier.minTouchTarget(),
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, tint = Theme.colors.brand)
                }
                Text(
                    text = "Min 48dp via Modifier.minTouchTarget()",
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.colors.muted,
                )
            }
        }
    }
}

@Composable
private fun LabeledDot(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        A11yStatusDot(color = color, label = text)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Theme.colors.ink2)
    }
}
