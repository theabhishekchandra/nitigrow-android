package com.ardym.nitigrow.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ardym.nitigrow.core.realtime.RealtimeClient

/**
 * Mounts WebSocket on ON_START, disconnects on ON_STOP. Use in MainScaffold so socket
 * lives for the duration of authenticated usage.
 */
@Composable
fun RealtimeLifecycle(realtime: RealtimeClient) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> realtime.connect()
                Lifecycle.Event.ON_STOP -> realtime.disconnect()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
