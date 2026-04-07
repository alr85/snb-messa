package com.snb.inspect.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import android.net.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * ---------------------------------------------------------
 * SINGLE SOURCE OF TRUTH FOR NETWORK CONNECTIVITY
 * ---------------------------------------------------------
 */

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    
    // Check for both INTERNET and VALIDATED. 
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Compose helper that OBSERVES connectivity changes.
 * Returns true if offline, false if online.
 * 
 * ADDED ROBUSTNESS: 
 * 1. Uses a 3-second delay before confirming "offline" status to prevent flickering during network handovers.
 * 2. Immediately switches to "online" status when a connection is detected.
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
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Connection is back! Switch UI immediately.
                value = false
            }

            override fun onLost(network: Network) {
                // Connection lost. Wait a few seconds before showing the red banner
                // to see if it's just a temporary dip or handover.
                launch {
                    delay(3000) 
                    if (!isNetworkAvailable(context)) {
                        value = true
                    }
                }
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                
                if (isValidated && hasInternet) {
                    value = false
                } else {
                    // Validation dip. Delay confirmation.
                    launch {
                        delay(3000)
                        if (!isNetworkAvailable(context)) {
                            value = true
                        }
                    }
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)
        
        // Initial sync
        value = !isNetworkAvailable(context)

        awaitDispose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }
}

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun observe(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val isNowOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isNowOnline)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged().conflate()
}
