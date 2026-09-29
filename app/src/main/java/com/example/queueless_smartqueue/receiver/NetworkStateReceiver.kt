package com.example.queueless_smartqueue.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.queueless_smartqueue.data.LocalQueueEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global Network State Monitor providing reactive connectivity state to composables.
 */
object NetworkStateMonitor {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastStatusMessage = MutableStateFlow("App connected to live network")
    val lastStatusMessage: StateFlow<String> = _lastStatusMessage.asStateFlow()

    fun updateStatus(online: Boolean, context: Context? = null) {
        val wasOnline = _isOnline.value
        _isOnline.value = online

        if (!online) {
            _lastStatusMessage.value = "Offline Mode: Pages & queues loaded from local cache"
            Log.d("NetworkStateMonitor", "Network disconnected. Offline broadcast triggered -> Loading cached pages.")
            // Ensure local engine has all default organizations, services, and counters loaded
            LocalQueueEngine.filterServicesForOrg("org_1")
        } else {
            _lastStatusMessage.value = "Connected: Live queue synchronization active"
            Log.d("NetworkStateMonitor", "Network connected. Live sync enabled.")
        }
    }
}

/**
 * BroadcastReceiver for network connectivity changes.
 * When the app is not using internet, it catches the connectivity change broadcast
 * and triggers instant page loading from local storage/cache.
 */
class NetworkStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isConnected = checkConnectivity(connectivityManager)

        NetworkStateMonitor.updateStatus(isConnected, context)
    }

    private fun checkConnectivity(connectivityManager: ConnectivityManager?): Boolean {
        if (connectivityManager == null) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}
