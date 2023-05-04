package com.nielsmasdorp.nederadio.domain.connectivity

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Manager for network related stuff
 */
interface NetworkManager {

    /**
     * @return whether the device is currently connected to the internet
     */
    fun isConnected(): Boolean
}
