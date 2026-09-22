package com.websbaba.nitigrow.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.model.WabaStatus
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Building blocks for the Settings hub: profile card, grouped rows with a tonal
// icon disc, switch rows and status pills.
// ─────────────────────────────────────────────────────────────────────────────

/** How a status pill is coloured; resolved to a tone at draw time. */
internal enum class PillKind { POSITIVE, WARNING, ERROR, NEUTRAL }

internal data class PillSpec(val label: String, val kind: PillKind)

internal fun wabaPillFor(status: WabaStatus): PillSpec = when (status) {
    WabaStatus.ACTIVE -> PillSpec("CONNECTED", PillKind.POSITIVE)
    WabaStatus.PENDING -> PillSpec("PENDING", PillKind.WARNING)
    WabaStatus.SUSPENDED -> PillSpec("SUSPENDED", PillKind.ERROR)
    WabaStatus.FAILED -> PillSpec("FAILED", PillKind.ERROR)
    WabaStatus.NOT_LINKED -> PillSpec("NOT LINKED", PillKind.NEUTRAL)
}

internal fun teamLabel(count: Int): String = if (count == 1) "1 member" else "$count members"

internal fun formatRupees(paise: Long): String =
    "₹" + NumberFormat.getIntegerInstance(Locale.ENGLISH).format(paise / 100)

/** Native-script language names in the picker get an English gloss underneath. */
internal fun languageGloss(tag: String): String = when (tag) {
    "" -> "Follows your phone"
    "en" -> "en"
    "hi" -> "Hindi"
    "mr" -> "Marathi"
    "gu" -> "Gujarati"
    "ta" -> "Tamil"
    "te" -> "Telugu"
    "kn" -> "Kannada"
    "bn" -> "Bengali"
    else -> tag
}

@Composable
private fun pillTone(kind: PillKind): NitiTone {
    val c = Niti.colors
    return when (kind) {
        PillKind.POSITIVE -> c.primaryTone
        PillKind.WARNING -> c.secondaryTone
        PillKind.ERROR -> c.errorTone
        PillKind.NEUTRAL -> NitiTone(c.surfaceHigh, c.onSurfaceVariant)
    }
}

// ── Profile ──────────────────────────────────────────────────────────────────

@Composable
fun HubProfileCard(name: String, email: String, role: UserRole, onClick: () -> Unit) {
    val tone = Niti.colors.primaryTone
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(tone.container)
            .clickable(role = Role.Button, onClickLabel = "Edit profile", onClick = onClick)
            .padding(18.dp)
    ) {
        Avatar(name = name, url = null, sizeDp = 60)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = NitiType.title.copy(fontSize = 20.sp, lineHeight = 26.sp),
                color = tone.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email,
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = tone.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            RolePill(role, modifier = Modifier.padding(top = 8.dp))
        }
        Icon(NitiIcons.ChevronRight, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RolePill(role: UserRole, modifier: Modifier = Modifier) {
    val c = Niti.colors
    val (bg, fg) = when (role) {
        UserRole.OWNER -> c.surface to c.primary
        UserRole.ADMIN -> c.secondaryTone.container to c.secondaryTone.onContainer
        UserRole.AGENT -> c.surfaceHigh to c.onSurfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 9.dp)
    ) {
        if (role == UserRole.OWNER) {
            Icon(NitiIcons.Shield, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        }
        Text(
            text = role.name,
            style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
            color = fg
        )
    }
}

// ── Sections and rows ────────────────────────────────────────────────────────

/** Small caps-style label over a rounded tonal card that holds [content]. */
@Composable
fun HubSection(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.padding(top = 20.dp)) {
        Text(
            text = label,
            style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
            color = Niti.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Niti.colors.surfaceLow)
                .padding(vertical = 4.dp)
        ) { content() }
    }
}

@Composable
private fun IconDisc(icon: ImageVector, tone: NitiTone) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp).background(tone.container, CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(22.dp))
    }
}

/** Navigational row: icon disc, title, optional value or pill, chevron. */
@Composable
internal fun HubRow(
    icon: ImageVector,
    tone: NitiTone,
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    pill: PillSpec? = null,
    showChevron: Boolean = true,
    titleColor: Color = Niti.colors.onSurface,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconDisc(icon, tone)
        Text(
            text = title,
            style = NitiType.titleUi.copy(fontWeight = FontWeight.Medium),
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        pill?.let { spec ->
            val t = pillTone(spec.kind)
            Text(
                text = spec.label,
                style = NitiType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
                color = t.onContainer,
                modifier = Modifier
                    .heightIn(min = 22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(t.container)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
        value?.let {
            Text(text = it, style = NitiType.label, color = Niti.colors.onSurfaceVariant)
        }
        if (showChevron) {
            Icon(NitiIcons.ChevronRight, contentDescription = null, tint = Niti.colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

/** Switch row; the whole row toggles, and the switch itself is decorative for accessibility. */
@Composable
fun HubToggleRow(icon: ImageVector, tone: NitiTone, title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = Niti.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconDisc(icon, tone)
        Text(
            text = title,
            style = NitiType.titleUi.copy(fontWeight = FontWeight.Medium),
            color = c.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.onPrimary,
                checkedTrackColor = c.primary,
                checkedBorderColor = c.primary,
                uncheckedThumbColor = c.outline,
                uncheckedTrackColor = c.surfaceHigh,
                uncheckedBorderColor = c.outline
            )
        )
    }
}
