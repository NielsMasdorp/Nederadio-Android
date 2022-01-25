package com.nielsmasdorp.sleeply.domain.connectivity

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Manager for network related stuff
 */
interface NetworkManager {

    /**
     * @return whether the device is currently connected to WiFi
     */
    fun isOnWifi(): Boolean
}