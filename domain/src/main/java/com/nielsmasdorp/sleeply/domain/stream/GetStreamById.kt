package com.nielsmasdorp.sleeply.domain.stream

import com.nielsmasdorp.sleeply.domain.DefaultDispatcherProvider
import com.nielsmasdorp.sleeply.domain.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * @author Niels Masdorp (NielsMasdorp)
 *
 * Use case for retrieving a stream by id
 * @param repository stream repository
 */
class GetStreamById(
    private val repository: StreamRepository,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) {

    suspend operator fun invoke(id: String): Stream = withContext(dispatcher.io()) {
        repository.getStreamById(id)
    }
}