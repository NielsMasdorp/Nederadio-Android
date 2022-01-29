package com.nielsmasdorp.sleeply.domain.stream

import com.nielsmasdorp.sleeply.domain.DefaultDispatcherProvider
import com.nielsmasdorp.sleeply.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving all streams of this app
 * @param repository stream repository
 */
class GetAllStreams(
    private val repository: StreamRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(): List<Stream> = withContext(dispatcher.io()) {
        repository.getStreams()
    }
}