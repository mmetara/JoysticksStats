package com.joysticks.stats.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build

object NetworkUtils {
    private const val TARGET_SSID = "mmetara"

    fun isTargetWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // 1. Check if WiFi is the active transport
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return false
        }

        // 2. Check the SSID
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        
        val currentSsid = info.ssid?.replace("\"", "") ?: ""
        
        // Android 10+ requires Location to be ON to see the SSID.
        // If it's "<unknown ssid>", we can't be 100% sure, but we can't block the user 
        // if they are actually on the right network but GPS is off.
        
        if (currentSsid == "<unknown ssid>") {
            // Fallback: If we can't see the name but we ARE on WiFi, we allow it 
            // OR we can log a warning. For now, let's be strict but handle the string.
            return false 
        }
        
        return currentSsid.equals(TARGET_SSID, ignoreCase = true)
    }
}
