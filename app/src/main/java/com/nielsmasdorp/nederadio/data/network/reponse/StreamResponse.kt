package com.nielsmasdorp.nederadio.data.network.reponse

import com.nielsmasdorp.nederadio.domain.stream.Stream
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Serializable
data class StreamResponse(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String,
    @SerialName("image")
    val imageUrl: String,
) {

    fun toDomain(
        isCurrent: Boolean,
        isFavorite: Boolean
    ) = Stream(
        isActive = isCurrent,
        isFavorite = isFavorite,
        id = id,
        url = url,
        title = name,
        imageUrl = imageUrl
    )
}
