package com.nielsmasdorp.nederadio.data.stream

import com.nielsmasdorp.nederadio.data.network.reponse.StreamResponse
import com.nielsmasdorp.nederadio.domain.stream.Stream
import com.nielsmasdorp.nederadio.domain.stream.StreamProvider
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class ApiStreamProvider(private val client: HttpClient) : StreamProvider {

    override suspend fun getStreams(
        isFavorite: suspend (String) -> Boolean,
        isCurrent: suspend (String) -> Boolean
    ): List<Stream> {
        return client.get("/data.json")
            .body<List<StreamResponse>>()
            .map { stream ->
                stream.toDomain(
                    isCurrent = isCurrent(stream.id),
                    isFavorite = isFavorite(stream.id)
                )
            }
    }
}
