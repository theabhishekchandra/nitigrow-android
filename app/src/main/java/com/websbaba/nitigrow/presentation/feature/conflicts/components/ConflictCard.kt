package com.websbaba.nitigrow.presentation.feature.conflicts.components

import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.util.relativeTime
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.presentation.feature.conflicts.ConflictAction
import com.websbaba.nitigrow.presentation.feature.conflicts.ConflictRule
import com.websbaba.nitigrow.presentation.feature.conflicts.SyncConflict
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// ConflictCard — single conflict row inside ConflictsScreen.
// Layout:
//   • Header: entity-type icon + entity name + relative time
//   • Description line
//   • Two side-by-side preview boxes ("Yours (offline)" / "Server"), with the
//     recommended side outlined in Niti.colors.primary based on rule
//   • Action row: Use yours / Use server / Dismiss
//
// Accessibility:
//   • The whole card carries a single semantics description (merged children
//     are still readable individually), e.g.:
//       "Conflict: Priya Sharma — edited offline and on web. Choose how to resolve."
//   • Each action button has a meaningful label (the visible text is enough
//     for TalkBack since they are real Buttons).
// ─────────────────────────────────────────────────────────────────────────────

private val CardShape = RoundedCornerShape(14.dp)
private val InnerPadding = 16.dp
private val SectionGap = 12.dp
private val PreviewBoxShape = RoundedCornerShape(10.dp)
private val PreviewBoxPadding = 12.dp
private val PreviewBoxMinHeight = 88.dp
private val RecommendedBorderWidth = 2.dp
private val EntityIconSize = 36.dp

@Composable
fun ConflictCard(
    conflict: SyncConflict,
    isResolving: Boolean,
    onResolve: (ConflictAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val a11yText = buildA11yText(conflict)

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Niti.colors.surfaceLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = a11yText },
    ) {
        Column(modifier = Modifier.padding(InnerPadding)) {

            // ── Header: icon + name + time ────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EntityIcon(entityType = conflict.entityType)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conflict.entityName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Niti.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${conflict.entityType.replaceFirstChar { it.uppercaseChar() }} · ${relativeTime(conflict.detectedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Niti.colors.onSurfaceVariant,
                    )
                }
                RuleChip(rule = conflict.rule)
            }

            Box(modifier = Modifier.height(SectionGap))

            // ── Description ───────────────────────────────────────────────
            Text(
                text = conflict.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Niti.colors.onSurface,
            )

            Box(modifier = Modifier.height(SectionGap))

            // ── Preview boxes ─────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PreviewBox(
                    label = "Yours (offline)",
                    value = conflict.localValue,
                    recommended = conflict.rule == ConflictRule.LOCAL_WINS,
                    modifier = Modifier.weight(1f),
                )
                PreviewBox(
                    label = "Server",
                    value = conflict.serverValue,
                    recommended = conflict.rule == ConflictRule.SERVER_WINS,
                    modifier = Modifier.weight(1f),
                )
            }

            Box(modifier = Modifier.height(SectionGap))

            // ── Action row ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = { onResolve(ConflictAction.UseLocal) },
                    enabled = !isResolving,
                    modifier = Modifier.weight(1f),
                ) { Text("Use yours") }
                Button(
                    onClick = { onResolve(ConflictAction.UseServer) },
                    enabled = !isResolving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Niti.colors.primary,
                        contentColor = Niti.colors.surface,
                    ),
                    modifier = Modifier.weight(1f),
                ) { Text("Use server") }
                TextButton(
                    onClick = { onResolve(ConflictAction.Dismiss) },
                    enabled = !isResolving,
                ) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun EntityIcon(entityType: String) {
    val icon: ImageVector = when (entityType) {
        "contact" -> Icons.Filled.Person
        "message" -> Icons.Filled.Chat
        "campaign" -> Icons.Filled.Campaign
        else -> Icons.Filled.Warning
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(EntityIconSize)
            .clip(RoundedCornerShape(10.dp))
            .background(Niti.colors.primaryTone.container),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative — entity name already in header
            tint = Niti.colors.primaryTone.onContainer,
        )
    }
}

@Composable
private fun PreviewBox(
    label: String,
    value: String,
    recommended: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (recommended) Niti.colors.primary else Niti.colors.outlineVariant
    val borderWidth = if (recommended) RecommendedBorderWidth else 1.dp

    // Recommended state must be conveyed without colour alone (a11y rule):
    // we prefix the label with "Recommended · " when it's the suggested side.
    val labelText = if (recommended) "Recommended · $label" else label

    Column(
        modifier = modifier
            .clip(PreviewBoxShape)
            .background(Niti.colors.surfaceLow)
            .border(BorderStroke(borderWidth, borderColor), PreviewBoxShape)
            .padding(PreviewBoxPadding),
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelSmall,
            color = if (recommended) Niti.colors.primary else Niti.colors.onSurfaceVariant,
            fontWeight = if (recommended) FontWeight.Bold else FontWeight.Normal,
        )
        Box(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Niti.colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .heightAtLeast(PreviewBoxMinHeight),
        )
    }
}

// Tiny modifier helper kept private so the file is self-contained.
private fun Modifier.heightAtLeast(min: androidx.compose.ui.unit.Dp): Modifier =
    this.defaultMinSize(minHeight = min)

@Composable
private fun RuleChip(rule: ConflictRule) {
    val (label, bg, fg) = when (rule) {
        ConflictRule.SERVER_WINS -> Triple("Server wins", Niti.colors.primaryTone.container, Niti.colors.primary)
        ConflictRule.LOCAL_WINS  -> Triple("Yours wins",  Niti.colors.secondaryTone.container, Niti.colors.warning)
        ConflictRule.ASK_USER    -> Triple("Needs you",   Niti.colors.tertiaryTone.container, Niti.colors.tertiaryTone.onContainer)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

private fun buildA11yText(c: SyncConflict): String = buildString {
    append("Conflict: ")
    append(c.entityName)
    append(" — ")
    append(c.description)
    append(" Choose how to resolve. ")
    append(
        when (c.rule) {
            ConflictRule.SERVER_WINS -> "Recommended: keep server version."
            ConflictRule.LOCAL_WINS  -> "Recommended: keep your offline version."
            ConflictRule.ASK_USER    -> "No recommendation — please pick."
        }
    )
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "ConflictCard — server wins")
@Composable
private fun ConflictCardPreview() {
    NitiGrowTheme {
        Box(modifier = Modifier.background(Niti.colors.surface).padding(16.dp)) {
            ConflictCard(
                conflict = SyncConflict(
                    id = "p-1",
                    entityType = "contact",
                    entityName = "Priya Sharma",
                    detectedAt = Instant.now().minus(2, ChronoUnit.HOURS),
                    description = "You edited tags offline. Web also changed tags at the same time.",
                    localValue = "Tags: Lead, Hot",
                    serverValue = "Tags: Lead, VIP",
                    rule = ConflictRule.SERVER_WINS,
                ),
                isResolving = false,
                onResolve = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "ConflictCard — ask user")
@Composable
private fun ConflictCardAskPreview() {
    NitiGrowTheme {
        Box(modifier = Modifier.background(Niti.colors.surface).padding(16.dp)) {
            ConflictCard(
                conflict = SyncConflict(
                    id = "p-2",
                    entityType = "message",
                    entityName = "Anand Mehta — Invoice draft",
                    detectedAt = Instant.now().minus(35, ChronoUnit.MINUTES),
                    description = "You drafted 'Send invoice' offline. The chat was archived on web.",
                    localValue = "Draft: \"Hi Anand, your invoice for ₹12,400 is attached.\"",
                    serverValue = "Chat archived 28 minutes ago by Rahul.",
                    rule = ConflictRule.ASK_USER,
                ),
                isResolving = false,
                onResolve = {},
            )
        }
    }
}

