package com.nielsmasdorp.nederadio.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
data class Stream(
    val id: String,
    val url: String,
    val title: String,
    val track: String? = null,
    val imageUrl: String,
    val imageBytes: ByteArray,
    val isFavorite: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stream

        if (id != other.id) return false
        if (url != other.url) return false
        if (title != other.title) return false
        if (track != other.track) return false
        if (imageUrl != other.imageUrl) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (isFavorite != other.isFavorite) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (track?.hashCode() ?: 0)
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + isFavorite.hashCode()
        return result
    }
}