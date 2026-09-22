package com.websbaba.nitigrow.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionState { Available, Lost }

@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun observe(): Flow<ConnectionState> = callbackFlow {
        val cm = context.getSystemService<ConnectivityManager>()
            ?: run { close(); return@callbackFlow }

        // Default-network callback: the OS reports one callback stream for whichever
        // network currently carries traffic. A plain registerNetworkCallback fires
        // onLost for every network that drops, so a Wi-Fi -> cellular handover
        // (onAvailable(cell), then onLost(wifi)) would wrongly end as "offline".
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(ConnectionState.Available) }
            override fun onLost(network: Network) { trySend(ConnectionState.Lost) }
        }

        cm.registerDefaultNetworkCallback(callback)

        // initial
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        trySend(
            if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
                ConnectionState.Available else ConnectionState.Lost
        )

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
