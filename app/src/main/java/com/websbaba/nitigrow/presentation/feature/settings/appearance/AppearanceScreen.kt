package com.websbaba.nitigrow.presentation.feature.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.settings.components.PillShape
import com.websbaba.nitigrow.presentation.feature.settings.components.StatusPill
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import com.websbaba.nitigrow.core.ui.theme.AppTheme
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiBrandForwardColors
import com.websbaba.nitigrow.core.ui.theme.NitiColors
import com.websbaba.nitigrow.core.ui.theme.NitiDarkColors
import com.websbaba.nitigrow.core.ui.theme.NitiLightColors
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.PlanTier
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// AppearanceScreen — Settings ▸ Appearance.
//
//   ‹ Appearance
//   ┌ Soft Paper mock · name/tagline · ✓ ┐         (always unlocked)
//   ┌ Brand Forward mock · name/tagline · GROWTH PLAN ┐
//   ┌ Espresso Premium mock · name/tagline · PRO PLAN ┐
//
// Each card renders a mini app mock in that theme's *actual* palette (band
// strip, heading line, stat-card row, CTA pill) so the user previews the look
// before switching. Unlocked taps apply instantly via ThemeDataStore; locked
// taps surface an upgrade dialog that deep-links to Billing & plan.
// ─────────────────────────────────────────────────────────────────────────────

private val CardSpacing = 12.dp

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    var upgradeTheme by remember { mutableStateOf<AppTheme?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is AppearanceEffect.UpgradeRequired -> upgradeTheme = e.theme
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            SubScreenHeader(
                title = "Appearance",
                subtitle = "Pick how NitiGrow looks on this device",
                onBack = onBack
            )
        }
    ) { padding ->
        AppearanceBody(
            state = state,
            onSelect = viewModel::onSelect,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }

    upgradeTheme?.let { theme ->
        val plan = planLabel(theme.minTier)
        AlertDialog(
            onDismissRequest = { upgradeTheme = null },
            title = { Text("${theme.displayName} is locked") },
            text = { Text("Unlock with the $plan plan to use this look across the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        upgradeTheme = null
                        onUpgrade()
                    }
                ) { Text("View plans", color = Niti.colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { upgradeTheme = null }) { Text("Not now") }
            }
        )
    }
}

@Composable
private fun AppearanceBody(
    state: AppearanceUiState,
    onSelect: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeCard(
                theme = theme,
                selected = theme == state.selected,
                locked = !theme.isUnlockedFor(state.tier),
                onClick = { onSelect(theme) }
            )
        }
    }
}

// ── theme card ───────────────────────────────────────────────────────────────

@Composable
private fun ThemeCard(
    theme: AppTheme,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceLow)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        ThemeMock(theme = theme, palette = paletteFor(theme))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    theme.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    theme.tagline,
                    fontSize = 11.5.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (locked) {
                StatusPill(
                    "${planLabel(theme.minTier)} plan",
                    bg = colors.secondaryTone.container,
                    fg = colors.secondaryTone.onContainer
                )
            }
            SelectedIndicator(selected)
        }
    }
}

@Composable
private fun SelectedIndicator(selected: Boolean) {
    val colors = Niti.colors
    if (selected) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .background(colors.primary, CircleShape)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected",
                tint = if (colors.isLight) colors.surface else colors.primaryTone.onContainer,
                modifier = Modifier.size(14.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.5.dp, colors.outlineVariant, CircleShape)
        )
    }
}

// ── mini app mock in the theme's own palette ─────────────────────────────────

/**
 * Band strip + heading line + stat-card row + CTA pill, drawn entirely from
 * [palette] (never the active [Niti.colors]) so each card is a faithful
 * preview of that theme. Brand Forward shows its cream hero surface
 * and deep-green heading ink; Espresso shows turmeric promoted to the CTA.
 */
@Composable
private fun ThemeMock(theme: AppTheme, palette: NitiColors) {
    val mockBg = if (theme == AppTheme.BRAND_FORWARD) palette.surfaceLow else palette.surface
    val headingInk = if (theme == AppTheme.BRAND_FORWARD) palette.primaryTone.onContainer else palette.onSurface
    val onBrand = palette.onPrimary
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(mockBg)
            .border(1.dp, palette.outlineVariant, shape)
    ) {
        // Status-bar / band header strip.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.primary)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            repeat(3) {
                Box(modifier = Modifier.size(4.dp).background(onBrand, CircleShape))
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            // Heading line.
            Box(
                modifier = Modifier
                    .size(width = 92.dp, height = 9.dp)
                    .background(headingInk, RoundedCornerShape(4.dp))
            )
            // Stat-card row.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                repeat(3) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(palette.surfaceLow)
                            .border(1.dp, palette.outlineVariant, RoundedCornerShape(7.dp))
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = 7.dp)
                                .background(palette.onSurface, RoundedCornerShape(3.dp))
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(width = 30.dp, height = 5.dp)
                                .background(palette.onSurfaceVariant, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
            // Primary CTA pill.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 86.dp, height = 20.dp)
                    .background(palette.primary, PillShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 6.dp)
                        .background(onBrand, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

// ── helpers ──────────────────────────────────────────────────────────────────

private fun paletteFor(theme: AppTheme): NitiColors = when (theme) {
    AppTheme.SOFT_PAPER -> NitiLightColors
    AppTheme.BRAND_FORWARD -> NitiBrandForwardColors
    AppTheme.ESPRESSO_PREMIUM -> NitiDarkColors
}

private fun planLabel(tier: PlanTier): String = when (tier) {
    PlanTier.STARTER -> "Starter"
    PlanTier.GROWTH -> "Growth"
    PlanTier.PRO -> "Pro"
    PlanTier.ENTERPRISE -> "Enterprise"
}

@Preview(showBackground = true, name = "Appearance — body")
@Composable
private fun PreviewAppearanceBody() {
    NitiGrowTheme {
        AppearanceBody(
            state = AppearanceUiState(
                selected = AppTheme.SOFT_PAPER,
                tier = PlanTier.GROWTH
            ),
            onSelect = {}
        )
    }
}
