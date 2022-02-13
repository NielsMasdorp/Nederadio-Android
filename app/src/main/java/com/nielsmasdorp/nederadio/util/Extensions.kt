package com.nielsmasdorp.nederadio.util

import android.content.res.Resources
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.Failure
import io.ktor.client.features.*

fun Exception.toFailure(resources: Resources): Failure = when (this) {
    is ServerResponseException -> Failure.HttpErrorInternalServerError(resources.getString(R.string.streams_fetch_error_general))
    is ClientRequestException ->
        when (this.response.status.value) {
            400 -> Failure.HttpErrorBadRequest(resources.getString(R.string.streams_fetch_error_general))
            401 -> Failure.HttpErrorUnauthorized(resources.getString(R.string.streams_fetch_error_general))
            403 -> Failure.HttpErrorForbidden(resources.getString(R.string.streams_fetch_error_general))
            404 -> Failure.HttpErrorNotFound(resources.getString(R.string.streams_fetch_error_general))
            else -> Failure.HttpError(resources.getString(R.string.streams_fetch_error_general))
        }
    is RedirectResponseException -> Failure.HttpError(resources.getString(R.string.streams_fetch_error_general))
    else -> Failure.GenericError(resources.getString(R.string.streams_fetch_error_general))
}

