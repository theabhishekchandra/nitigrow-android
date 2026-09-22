package com.websbaba.nitigrow.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// Building blocks for the redesigned screens (Design → "Design system" →
// Components). Every touch target is at least 44dp.
// ─────────────────────────────────────────────────────────────────────────────

/** 48dp round icon button. */
@Composable
fun NitiIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Niti.colors.onSurface,
    enabled: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/**
 * 10dp-radius selectable chip. A selected chip swaps its icon for a check;
 * [count] renders a bold suffix ("Unread 3").
 */
@Composable
fun NitiChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    count: Int? = null,
) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(10.dp)
    val ink = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant
    val glyph = if (selected) NitiIcons.Check else icon
    // 44dp touch target around a 34dp visual chip.
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 44.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 5.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .height(34.dp)
                .clip(shape)
                .background(if (selected) colors.primaryTone.container else Color.Transparent)
                .border(1.dp, if (selected) colors.primaryTone.container else colors.outlineVariant, shape)
                .padding(horizontal = 14.dp),
        ) {
            if (glyph != null) {
                Icon(glyph, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
            }
            Text(text = label, style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Medium), color = ink, maxLines = 1)
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold),
                    color = ink.copy(alpha = 0.85f),
                )
            }
        }
    }
}

/** Filled pill CTA — the one primary action on a screen or sheet. */
@Composable
fun NitiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = Niti.colors
    PillButton(text, onClick, modifier, enabled, loading, colors.primary, colors.onPrimary)
}

/** Softer pill CTA in the primary container tone — for secondary actions. */
@Composable
fun NitiTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val tone = Niti.colors.primaryTone
    PillButton(text, onClick, modifier, enabled, loading, tone.container, tone.onContainer)
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    container: Color,
    content: Color,
) {
    val colors = Niti.colors
    val active = enabled || loading
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (active) container else colors.surfaceHigh)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = content)
        } else {
            Text(
                text = text,
                style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
                color = if (enabled) content else colors.outline,
            )
        }
    }
}

/** Borderless text action such as "Reset" or "Retry". */
@Composable
fun NitiTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Niti.colors.primary,
    enabled: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .defaultMinSize(minWidth = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        Text(
            text = text,
            style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) color else Niti.colors.outline,
        )
    }
}

/** 56dp pill search field on a tonal surface, with a clear button once typed. */
@Composable
fun NitiSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceContainer)
            .padding(start = 18.dp, end = 4.dp),
    ) {
        Icon(NitiIcons.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = NitiType.body.copy(fontSize = 16.sp, color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = NitiType.body.copy(fontSize = 16.sp),
                            color = colors.outline,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f),
        )
        if (query.isNotEmpty()) {
            NitiIconButton(
                icon = NitiIcons.Close,
                contentDescription = "Clear search",
                onClick = { onQueryChange("") },
                tint = colors.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.size(width = 14.dp, height = 1.dp))
        }
    }
}

/** Centered illustration + title + body + optional action, for empty and error states. */
@Composable
fun NitiStateView(
    icon: ImageVector,
    tone: NitiTone,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Niti.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, bottom = 60.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp).background(tone.container, CircleShape),
        ) {
            Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(40.dp))
        }
        Text(
            text = title,
            style = NitiType.title.copy(letterSpacing = (-0.4).sp),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = NitiType.bodyCompact,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        if (actionLabel != null && onAction != null) {
            NitiPrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Small uppercase-free section label: 14/20 · 600, tracked. */
@Composable
fun NitiSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
        color = Niti.colors.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

/** Extended FAB — the one primary action on a list screen. */
@Composable
fun NitiExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = Niti.colors.primaryTone
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(tone.container)
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, end = 20.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(24.dp))
        Text(
            text = text,
            style = NitiType.body.copy(fontWeight = FontWeight.SemiBold),
            color = tone.onContainer,
        )
    }
}

/** Outlined-field colours: transparent fill, primary when focused, outline otherwise. */
@Composable
fun nitiTextFieldColors(): TextFieldColors {
    val colors = Niti.colors
    return TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = colors.primary,
        unfocusedIndicatorColor = colors.outline,
        errorIndicatorColor = colors.error,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        errorLabelColor = colors.error,
        focusedLeadingIconColor = colors.primary,
        unfocusedLeadingIconColor = colors.onSurfaceVariant,
        errorLeadingIconColor = colors.error,
        focusedPlaceholderColor = colors.outline,
        unfocusedPlaceholderColor = colors.outline,
        focusedPrefixColor = colors.onSurfaceVariant,
        unfocusedPrefixColor = colors.onSurfaceVariant,
        cursorColor = colors.primary,
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        errorTextColor = colors.onSurface
    )
}

/** Detail-screen section card: 24dp tonal surface with 18dp content padding. Callers place it via [modifier]. */
@Composable
fun NitiCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Niti.colors.surfaceLow)
            .padding(18.dp),
        content = content
    )
}

/** 24dp radio indicator: ring plus inner dot when [selected]. */
@Composable
fun NitiRadioDot(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .border(2.dp, if (selected) colors.primary else colors.outline, CircleShape),
    ) {
        if (selected) Box(Modifier.size(12.dp).clip(CircleShape).background(colors.primary))
    }
}
