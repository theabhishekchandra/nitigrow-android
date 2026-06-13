package com.websbaba.nitigrow.ui.theme

/**
 * The three visual themes from the design explorations, gated by plan tier:
 *  - SOFT_PAPER — warm paper surfaces, viridian primary (every plan)
 *  - BRAND_FORWARD — cream hero surfaces, viridian band headers (Growth+)
 *  - ESPRESSO_PREMIUM — warm espresso dark with turmeric CTAs (Pro+)
 *
 * `key` is the persisted DataStore value — never rename keys, only add.
 */
enum class AppTheme(
    val key: String,
    val displayName: String,
    val tagline: String,
    val minTier: PlanTier,
) {
    SOFT_PAPER(
        key = "soft_paper",
        displayName = "Soft Paper",
        tagline = "Warm paper & viridian — the classic NitiGrow look",
        minTier = PlanTier.STARTER,
    ),
    BRAND_FORWARD(
        key = "brand_forward",
        displayName = "Brand Forward",
        tagline = "Cream heroes and a bold viridian band",
        minTier = PlanTier.GROWTH,
    ),
    ESPRESSO_PREMIUM(
        key = "espresso_premium",
        displayName = "Espresso Premium",
        tagline = "Warm dark espresso with turmeric gold",
        minTier = PlanTier.PRO,
    );

    fun isUnlockedFor(tier: PlanTier): Boolean = tier.rank >= minTier.rank

    companion object {
        val Default = SOFT_PAPER

        fun fromKey(key: String?): AppTheme =
            entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * Ranked subscription tiers, parsed from the backend's plan id/name
 * (`starter` / `growth` / `pro` / `enterprise`). Unknown or missing plans
 * resolve to STARTER so nothing premium leaks to logged-out/free states.
 */
enum class PlanTier(val rank: Int) {
    STARTER(0),
    GROWTH(1),
    PRO(2),
    ENTERPRISE(3);

    companion object {
        fun fromPlanId(planId: String?): PlanTier {
            val id = planId?.lowercase() ?: return STARTER
            return when {
                id.contains("enterprise") -> ENTERPRISE
                id.contains("pro") -> PRO
                id.contains("growth") -> GROWTH
                else -> STARTER
            }
        }
    }
}
