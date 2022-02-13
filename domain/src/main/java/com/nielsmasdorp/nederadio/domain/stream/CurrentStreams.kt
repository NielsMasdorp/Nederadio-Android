package com.nielsmasdorp.nederadio.domain.stream

sealed class CurrentStreams {
    object Loading : CurrentStreams()
    data class Error(val failure: Failure) : CurrentStreams()
    data class Success(val streams: List<Stream>) : CurrentStreams()
}