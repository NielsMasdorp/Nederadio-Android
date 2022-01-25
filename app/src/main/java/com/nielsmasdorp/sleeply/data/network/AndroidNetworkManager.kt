package com.nielsmasdorp.sleeply.data.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.nielsmasdorp.sleeply.domain.connectivity.NetworkManager

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class AndroidNetworkManager(private val connectivityManager: ConnectivityManager) : NetworkManager {

    override fun isOnWifi(): Boolean {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}