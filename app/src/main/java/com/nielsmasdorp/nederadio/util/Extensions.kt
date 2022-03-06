package com.nielsmasdorp.nederadio.util

import android.content.res.Resources
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.Failure
import io.ktor.client.features.*

/**
 * @author Niels Masdorp
 */
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

@UnstableApi
fun MediaSession.sendCommandToController(
    key: String,
    value: Bundle = Bundle()
): ListenableFuture<SessionResult> {
    return connectedControllers.firstOrNull()?.let { controller ->
        sendCustomCommand(controller, SessionCommand(key, value), Bundle())
    } ?: Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
}

@UnstableApi
fun MediaController.sendCommandToService(
    key: String,
    value: Bundle
): ListenableFuture<SessionResult> {
    return sendCustomCommand(SessionCommand(key, value), Bundle())
}