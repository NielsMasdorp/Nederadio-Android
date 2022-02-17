package com.nielsmasdorp.nederadio.domain.stream

sealed class Failure(open val message: String) {
    data class HttpErrorInternalServerError(override val message: String) : Failure(message)
    data class HttpErrorBadRequest(override val message: String) : Failure(message)
    data class HttpErrorUnauthorized(override val message: String) : Failure(message)
    data class HttpErrorForbidden(override val message: String) : Failure(message)
    data class HttpErrorNotFound(override val message: String) : Failure(message)
    data class HttpError(override val message: String) : Failure(message)
    data class GenericError(override val message: String) : Failure(message)
    data class NoNetworkConnection(override val message: String) : Failure(message)
}