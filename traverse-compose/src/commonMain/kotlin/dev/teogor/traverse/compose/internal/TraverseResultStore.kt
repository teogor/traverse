package dev.teogor.traverse.compose.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * KMP-compatible result store backed by [MutableSharedFlow].
 */
internal class TraverseResultStore {

    private val pending = mutableMapOf<String, Any?>()
    private val events = MutableSharedFlow<Pair<String, Any?>>(replay = 1)

    fun <T> setResult(key: String, value: T) {
        pending[key] = value
        events.tryEmit(key to value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearResult(key: String) {
        pending.remove(key)
        if (pending.isEmpty()) events.resetReplayCache()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getResult(key: String): T? = pending[key] as? T

    @Suppress("UNCHECKED_CAST")
    fun <T> observeResult(key: String): Flow<T> =
        events
            .filter { it.first == key }
            .map { it.second as T }
}


