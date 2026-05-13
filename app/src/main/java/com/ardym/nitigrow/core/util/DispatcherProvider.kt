package com.ardym.nitigrow.core.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Inject this instead of hardcoding Dispatchers.IO. Lets tests swap in TestDispatcher.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
