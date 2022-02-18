package com.nielsmasdorp.nederadio.data.network

import com.nielsmasdorp.nederadio.data.network.reponse.StreamResponse
import io.ktor.client.*
import io.ktor.client.request.*

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class StreamApi(private val client: HttpClient) {

    suspend fun getStreams(): List<StreamResponse> = client.get("/data.json")
}