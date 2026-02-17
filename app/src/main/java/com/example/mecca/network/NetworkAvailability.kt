package com.example.mecca.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import android.net.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * ---------------------------------------------------------
 * SINGLE SOURCE OF TRUTH FOR NETWORK CONNECTIVITY
 * ---------------------------------------------------------
 *
 * This file provides TWO things:
 *
 * 1) isNetworkAvailable()
 *      → Use for moment-in-time checks before network work.
 *
 * 2) rememberIsOffline()
 *      → Use in Compose to react to connectivity changes.
 *
 * Both use the SAME underlying logic so behaviour
 * stays consistent across the app.
 */


/**
 * Returns TRUE only when the device has a VALIDATED internet connection.
 *
 * VALIDATED means Android has confirmed external connectivity,
 * not just connection to a router.
 *
 * This prevents false positives on:
 * - hotel WiFi
 * - captive portals
 * - broken mobile data
 */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isNetworkAvailable(context: Context): Boolean {

    val cm =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    val network = cm.activeNetwork ?: return false

    val capabilities = cm.getNetworkCapabilities(network)
        ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}



/**
 * Compose helper that OBSERVES connectivity changes.
 *
 * No polling.
 * No battery drain.
 * Automatically unregisters callback.
 *
 * Returns a State<Boolean> where:
 *
 * true  = offline
 * false = online
 *
 * (Offline is usually what UI cares about.)
 */
@Composable
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun rememberIsOffline(): State<Boolean> {

    val context = LocalContext.current.applicationContext

    return produceState(
        initialValue = !isNetworkAvailable(context),
        key1 = context
    ) {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun update() {
            value = !isNetworkAvailable(context)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) = update()

            override fun onLost(network: Network) = update()

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) = update()
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        // Ensure correct state immediately
        update()

        awaitDispose {
            // Some manufacturers throw here — guard it.
            runCatching {
                cm.unregisterNetworkCallback(callback)
            }
        }
    }
}

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    fun observe(): Flow<Boolean> = callbackFlow {

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: android.net.Network) {
                trySend(true)
            }

            override fun onLost(network: android.net.Network) {
                trySend(false)
            }
        }

        val request = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

