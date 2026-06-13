package com.websbaba.nitigrow.presentation.base

/**
 * Per-screen UI state convention. Each feature defines its own data class
 * implementing nothing — just a data class — but follow this shape:
 *
 *   data class FooUiState(
 *       val isLoading: Boolean = false,
 *       val data: List<Foo> = emptyList(),
 *       val error: String? = null
 *   )
 *
 * One-shot effects (toasts, navigation) flow through Channel<FooEffect>,
 * NOT StateFlow — replaying a navigate-once event causes loops.
 */
