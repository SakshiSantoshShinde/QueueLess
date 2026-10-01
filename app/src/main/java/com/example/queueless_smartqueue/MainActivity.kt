package com.example.queueless_smartqueue

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.queueless_smartqueue.receiver.NetworkStateMonitor
import com.example.queueless_smartqueue.receiver.NetworkStateReceiver
import com.example.queueless_smartqueue.ui.navigation.AppNavigation
import com.example.queueless_smartqueue.ui.theme.QueueLessTheme

class MainActivity : ComponentActivity() {

    private val networkReceiver = NetworkStateReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register broadcast receiver for connectivity changes
        @Suppress("DEPRECATION")
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        // Perform initial connectivity check
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        @Suppress("DEPRECATION")
        val isConnected = cm?.activeNetworkInfo?.isConnected == true
        NetworkStateMonitor.updateStatus(isConnected, this)

        // Initialize SQLite Database and local managers
        com.example.queueless_smartqueue.data.QueueDatabaseHelper.getInstance(this)
        com.example.queueless_smartqueue.data.UserAuthManager.init(this)
        com.example.queueless_smartqueue.data.LocalQueueEngine.init(this)

        setContent {
            QueueLessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(networkReceiver)
        } catch (_: Exception) {}
    }
}