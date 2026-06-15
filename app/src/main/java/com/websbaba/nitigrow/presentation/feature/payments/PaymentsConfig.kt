package com.websbaba.nitigrow.presentation.feature.payments

// ─────────────────────────────────────────────────────────────────────────────
// Temporary feature gate for ONLINE payment links.
//
// The online flow (POST /payments/link → Razorpay) is fully wired end-to-end,
// but it cannot succeed until the Razorpay gateway clears merchant KYC /
// activation. While that is pending we keep this `false`, which makes the
// Payments screen show a MANUAL-payment popup instead of the (non-functional)
// online "create link" action — honest UX, no fake success.
//
// FLIP TO `true` the moment live Razorpay keys are set on the backend. No other
// code change is required: the online create/list/webhook path is already live.
// ─────────────────────────────────────────────────────────────────────────────
internal const val ONLINE_PAYMENTS_ENABLED = false
