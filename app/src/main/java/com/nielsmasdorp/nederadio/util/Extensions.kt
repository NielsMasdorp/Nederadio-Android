package com.nielsmasdorp.nederadio.util

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerControlView
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nielsmasdorp.nederadio.R
import com.nielsmasdorp.nederadio.domain.stream.Failure
import com.nielsmasdorp.nederadio.domain.stream.PlayerControls
import com.nielsmasdorp.nederadio.domain.stream.Stream
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException

/**
 * @author Niels Masdorp
 */
@Suppress("MagicNumber")
fun Exception.toFailure(resources: Resources): Failure = when (this) {
    is ServerResponseException -> Failure.HttpErrorInternalServerError(
        resources.getString(R.string.streams_fetch_error_general)
    )
    is ClientRequestException ->
        when (this.response.status.value) {
            400 -> Failure.HttpErrorBadRequest(resources.getString(R.string.streams_fetch_error_general))
            401 -> Failure.HttpErrorUnauthorized(resources.getString(R.string.streams_fetch_error_general))
            403 -> Failure.HttpErrorForbidden(resources.getString(R.string.streams_fetch_error_general))
            404 -> Failure.HttpErrorNotFound(
                resources.getString(R.string.streams_fetch_error_general)
            )
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
    connectedControllers.forEach { controller ->
        sendCustomCommand(controller, SessionCommand(key, value), Bundle())
    }
    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
}

@UnstableApi
fun MediaController.sendCommandToService(
    key: String,
    value: Bundle
): ListenableFuture<SessionResult> {
    return sendCustomCommand(SessionCommand(key, value), Bundle())
}

@UnstableApi
fun Stream.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(url.toUri())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                .setSubtitle(title)
                .setArtist(title)
                .setArtworkUri(imageUrl.toUri())
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .build()
        )
        .build()
}

/**
 * Return the name of the connected cast device, if any
 */
fun CastContext.connectedDeviceName(): String? {
    return sessionManager
        .currentCastSession
        ?.castDevice
        ?.friendlyName
}

fun CastContext.castingSubtitle(context: Context): String {
    val castDeviceName = connectedDeviceName()
    return if (!castDeviceName.isNullOrBlank()) {
        context.getString(R.string.stream_subtitle_casting_device, castDeviceName)
    } else {
        context.getString(R.string.stream_subtitle_casting)
    }
}

@OptIn(UnstableApi::class)
fun PlayerControls<*>.view(): PlayerControlView = getView() as PlayerControlView
