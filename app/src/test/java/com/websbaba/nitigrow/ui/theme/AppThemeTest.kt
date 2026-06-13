package com.websbaba.nitigrow.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppThemeTest {

    // --- PlanTier parsing ---

    @Test
    fun `fromPlanId maps backend plan ids to tiers`() {
        assertThat(PlanTier.fromPlanId("starter")).isEqualTo(PlanTier.STARTER)
        assertThat(PlanTier.fromPlanId("growth")).isEqualTo(PlanTier.GROWTH)
        assertThat(PlanTier.fromPlanId("pro")).isEqualTo(PlanTier.PRO)
        assertThat(PlanTier.fromPlanId("enterprise")).isEqualTo(PlanTier.ENTERPRISE)
    }

    @Test
    fun `fromPlanId is case-insensitive and tolerates decorated ids`() {
        assertThat(PlanTier.fromPlanId("Growth")).isEqualTo(PlanTier.GROWTH)
        assertThat(PlanTier.fromPlanId("plan_pro_annual")).isEqualTo(PlanTier.PRO)
        assertThat(PlanTier.fromPlanId("ENTERPRISE-2026")).isEqualTo(PlanTier.ENTERPRISE)
    }

    @Test
    fun `fromPlanId falls back to STARTER for null or unknown`() {
        assertThat(PlanTier.fromPlanId(null)).isEqualTo(PlanTier.STARTER)
        assertThat(PlanTier.fromPlanId("")).isEqualTo(PlanTier.STARTER)
        assertThat(PlanTier.fromPlanId("free-trial")).isEqualTo(PlanTier.STARTER)
    }

    // --- Theme gating ---

    @Test
    fun `soft paper is unlocked for every tier`() {
        PlanTier.entries.forEach { tier ->
            assertThat(AppTheme.SOFT_PAPER.isUnlockedFor(tier)).isTrue()
        }
    }

    @Test
    fun `brand forward unlocks at growth`() {
        assertThat(AppTheme.BRAND_FORWARD.isUnlockedFor(PlanTier.STARTER)).isFalse()
        assertThat(AppTheme.BRAND_FORWARD.isUnlockedFor(PlanTier.GROWTH)).isTrue()
        assertThat(AppTheme.BRAND_FORWARD.isUnlockedFor(PlanTier.PRO)).isTrue()
    }

    @Test
    fun `espresso premium unlocks at pro`() {
        assertThat(AppTheme.ESPRESSO_PREMIUM.isUnlockedFor(PlanTier.STARTER)).isFalse()
        assertThat(AppTheme.ESPRESSO_PREMIUM.isUnlockedFor(PlanTier.GROWTH)).isFalse()
        assertThat(AppTheme.ESPRESSO_PREMIUM.isUnlockedFor(PlanTier.PRO)).isTrue()
        assertThat(AppTheme.ESPRESSO_PREMIUM.isUnlockedFor(PlanTier.ENTERPRISE)).isTrue()
    }

    // --- Persistence keys ---

    @Test
    fun `fromKey round-trips every theme`() {
        AppTheme.entries.forEach { theme ->
            assertThat(AppTheme.fromKey(theme.key)).isEqualTo(theme)
        }
    }

    @Test
    fun `fromKey falls back to default for null or unknown`() {
        assertThat(AppTheme.fromKey(null)).isEqualTo(AppTheme.Default)
        assertThat(AppTheme.fromKey("neon_disco")).isEqualTo(AppTheme.Default)
    }

    @Test
    fun `persisted keys never change`() {
        // These exact strings live in users' DataStore files — renaming a key
        // silently resets that user's theme. Lock them down.
        assertThat(AppTheme.SOFT_PAPER.key).isEqualTo("soft_paper")
        assertThat(AppTheme.BRAND_FORWARD.key).isEqualTo("brand_forward")
        assertThat(AppTheme.ESPRESSO_PREMIUM.key).isEqualTo("espresso_premium")
    }
}
