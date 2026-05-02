package dev.teogor.traverse.compose.navigator

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.teogor.traverse.compose.deeplink.TraverseDeepLinkRegistry
import dev.teogor.traverse.compose.internal.TraverseResultStore
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.NavOptions
import dev.teogor.traverse.core.navigator.TraverseNavigator
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Production implementation of [TraverseNavigator] backed by a [SnapshotStateList].
 *
 * Created internally by [TraverseHost] or exposed via [rememberTraverseNavigator].
 * Callers should never instantiate this class directly; use [rememberTraverseNavigator]
 * when you need to hold a navigator reference outside the host (e.g. for tab navigation).
 *
 * nav3 types are not used here — Traverse is self-contained.
 */
internal class DefaultTraverseNavigator(
    private val _backStack: SnapshotStateList<Destination>,
    /**
     * Maps graph-key destination classes to their nested graph's start destination.
     * Populated by [TraverseHost] from the [TraverseGraphBuilder]. When a
     * [DefaultTraverseNavigator] is created via [rememberTraverseNavigator], this starts
     * as an empty map and is then updated by [TraverseHost] before the first composition.
     */
    internal var nestedGraphKeys: Map<KClass<out Destination>, Destination>,
) : TraverseNavigator {

    private val resultStore = TraverseResultStore()

    /**
     * The underlying [SnapshotStateList] so [TraverseHost] can attach it to the
     * [AnimatedContent] engine when an external navigator is provided.
     */
    internal val snapshotBackStack: SnapshotStateList<Destination>
        get() = _backStack

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

    // ── Deep Links ────────────────────────────────────────────────────────────

    /**
     * The deep-link registry built from the host's [TraverseGraphBuilder] entries.
     * Injected by [TraverseHost] before the first composition.
     */
    internal var deepLinkRegistry: TraverseDeepLinkRegistry? = null

    /**
     * Matches [uri] against every registered deep-link pattern, reconstructs the
     * destination via `kotlinx.serialization`, and navigates to it.
     *
     * @return `true` if a match was found and navigation occurred, `false` otherwise.
     */
    override fun navigateToDeepLink(uri: String): Boolean {
        val registry = deepLinkRegistry
        if (registry == null || registry.isEmpty) return false
        val destination = registry.resolve(uri) ?: return false
        navigate(destination)
        return true
    }
}
