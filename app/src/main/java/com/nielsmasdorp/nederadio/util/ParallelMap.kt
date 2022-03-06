package com.nielsmasdorp.nederadio.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * @author Niels Masdorp
 */
suspend fun <A, B> Iterable<A>.pMap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}