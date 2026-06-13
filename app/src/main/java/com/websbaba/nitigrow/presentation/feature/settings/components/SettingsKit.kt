package com.websbaba.nitigrow.presentation.feature.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// SettingsKit — shared design primitives for the Settings hub + sub-screens,
// matching the Claude Design prototype (warm-paper cards, custom toggle,
// Fraunces back-headers, bordered inputs, brand CTA).
// ─────────────────────────────────────────────────────────────────────────────

/** Pill corner used everywhere a fully-rounded chip/pill appears. */
val PillShape = RoundedCornerShape(999.dp)

/** Back-arrow header with a Fraunces 21sp title and optional subtitle/actions. */
@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.ink,
                modifier = Modifier.size(21.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                color = colors.ink
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = colors.muted
                )
            }
        }
        actions()
    }
}

/**
 * Custom 44×26dp toggle from the prototype: 13dp-radius track that animates
 * brand (on) / muted3 (off), with a 20dp floating thumb.
 */
@Composable
fun NgToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = Theme.colors
    val track by animateColorAsState(
        targetValue = if (checked) colors.brand else colors.muted3,
        label = "toggleTrack"
    )
    val thumbX by animateDpAsState(
        targetValue = if (checked) 21.dp else 3.dp,
        label = "toggleThumb"
    )
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (enabled) track else track.copy(alpha = 0.5f))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbX, y = 3.dp)
                .size(20.dp)
                .shadow(2.dp, CircleShape)
                .background(if (colors.isLight) colors.card else colors.ink, CircleShape)
        )
    }
}

/** 12sp / w600 / ink3 field label with the 6dp gap from the prototype. */
@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = Theme.colors.ink3,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/** Bordered card-background input (radius 12, 13×14 padding, 14sp ink). */
@Composable
fun NgTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val colors = Theme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (enabled) colors.ink else colors.muted
        ),
        cursorBrush = SolidColor(colors.brand),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) { inner() }
        },
        modifier = modifier.fillMaxWidth()
    )
}

/** Small status pill: 10sp w700 text on a soft background. */
@Composable
fun StatusPill(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .background(bg, PillShape)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp)
    }
}

/** Brand-filled primary CTA (radius 14, 15dp vertical padding, 14.5sp w600). */
@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val colors = Theme.colors
    val bg = if (enabled && !loading) colors.brand else colors.brand.copy(alpha = 0.5f)
    val fg = if (colors.isLight) colors.paper else colors.brandInk
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(vertical = 15.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = fg)
        } else {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}
