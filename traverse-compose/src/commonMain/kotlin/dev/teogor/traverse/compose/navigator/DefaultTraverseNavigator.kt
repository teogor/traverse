package dev.teogor.traverse.compose.navigator

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.teogor.traverse.compose.internal.TraverseResultStore
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.NavOptions
import dev.teogor.traverse.core.navigator.TraverseNavigator
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Production implementation of [TraverseNavigator] backed by a [SnapshotStateList].
 *
 * Created internally by [TraverseHost]. Callers should never instantiate this directly.
 * nav3 types are not used here — Traverse is self-contained.
 */
internal class DefaultTraverseNavigator(
    private val _backStack: SnapshotStateList<Destination>,
    private val nestedGraphKeys: Map<KClass<out Destination>, Destination>,
) : TraverseNavigator {

    private val resultStore = TraverseResultStore()

    // ── Back stack ────────────────────────────────────────────────────────────

    override val backStack: List<Destination>
        get() = _backStack

    // ── Navigation ────────────────────────────────────────────────────────────

    override fun navigate(destination: Destination, builder: NavOptions.() -> Unit) {
        val options = NavOptions().apply(builder)

        // Resolve nested graph key → its start destination
        val resolved = nestedGraphKeys[destination::class] ?: destination

        // Pop-before-navigate
        options.popUpTo?.let { target ->
            val index = _backStack.indexOfLast { it == target }
            if (index >= 0) {
                val removeFrom = if (options.popUpToInclusive) index else index + 1
                repeat(_backStack.size - removeFrom) { _backStack.removeLastOrNull() }
            } else {
                // Target not found — clear the whole stack
                _backStack.clear()
            }
        }

        // Single-top guard
        if (options.launchSingleTop &&
            _backStack.lastOrNull()?.let { it::class } == resolved::class) return

        _backStack.add(resolved)
    }

    override fun navigateUp(): Boolean {
        if (_backStack.size <= 1) return false
        _backStack.removeLastOrNull()
        return true
    }

    override fun popTo(destination: Destination, inclusive: Boolean): Boolean {
        val index = _backStack.indexOfLast { it == destination }
        if (index < 0) return false
        val removeFrom = if (inclusive) index else index + 1
        repeat(_backStack.size - removeFrom) { _backStack.removeLastOrNull() }
        return true
    }

    // ── Results ───────────────────────────────────────────────────────────────

    override fun <T> setResult(key: String, value: T) = resultStore.setResult(key, value)
    override fun clearResult(key: String) = resultStore.clearResult(key)
    override fun <T> observeResult(key: String): Flow<T> = resultStore.observeResult(key)
}
