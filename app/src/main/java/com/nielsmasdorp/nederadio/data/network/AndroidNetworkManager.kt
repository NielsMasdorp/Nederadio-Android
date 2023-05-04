package com.nielsmasdorp.nederadio.data.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.nielsmasdorp.nederadio.domain.connectivity.NetworkManager

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class AndroidNetworkManager(private val connectivityManager: ConnectivityManager) : NetworkManager {

    override fun isConnected(): Boolean {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}
