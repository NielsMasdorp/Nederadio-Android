package com.nielsmasdorp.nederadio.domain.stream

sealed class Streams {
    object Loading : Streams()
    data class Error(val failure: Failure) : Streams()
    data class Success(val streams: List<Stream>) : Streams()
}