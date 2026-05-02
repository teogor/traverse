package dev.teogor.traverse.test

import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.NavOptions
import dev.teogor.traverse.core.navigator.TraverseNavigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Test double for [TraverseNavigator].
 *
 * Records all navigation calls so tests can assert on them without launching a Compose UI.
 * Applies the same `popUpTo` and `launchSingleTop` logic as the production navigator,
 * so back-stack state is realistic.
 *
 * ## Basic usage
 *
 * ```kotlin
 * @Test
 * fun clickingOpenProfile_navigatesToProfile() {
 *     val fake = FakeTraverseNavigator(Home)
 *     // Call the logic under test (e.g. a ViewModel):
 *     onOpenProfileClicked(fake, userId = "42")
 *     // Assert:
 *     fake.assertNavigatedTo<Profile>()
 *     fake.assertCurrentDestination<Profile>()
 * }
 * ```
 *
 * After each test, call [reset] to wipe the navigation history while keeping the back stack.
 *
 * @param startDestination The initial entry on the back stack.
 */
public class FakeTraverseNavigator(
    startDestination: Destination,
) : TraverseNavigator {

    // ── Back stack ────────────────────────────────────────────────────────────

    private val _backStack: MutableList<Destination> = mutableListOf(startDestination)

    override val backStack: List<Destination> get() = _backStack

    // currentDestination and canNavigateUp inherit their default implementations from
    // TraverseNavigator (backStack.last() and backStack.size > 1 respectively).

    // ── Navigation history ────────────────────────────────────────────────────

    /** All [navigate] calls recorded since creation or the last [reset]. */
    public val navigateCalls: List<NavigationCall> get() = _navigateCalls
    private val _navigateCalls: MutableList<NavigationCall> = mutableListOf()

    /** Number of [navigateUp] calls that returned `true` since creation or the last [reset]. */
    public var navigateUpCount: Int = 0
        private set

    /** All [popTo] calls recorded since creation or the last [reset]. */
    public val popToCalls: List<PopToCall> get() = _popToCalls
    private val _popToCalls: MutableList<PopToCall> = mutableListOf()

    /** All [navigateToDeepLink] calls recorded since creation or the last [reset]. */
    public val deepLinkCalls: List<String> get() = _deepLinkCalls
    private val _deepLinkCalls: MutableList<String> = mutableListOf()

    // ── Results ───────────────────────────────────────────────────────────────

    /** Snapshot of all currently stored results (set via [setResult], cleared via [clearResult]). */
    public val results: Map<String, Any?> get() = _results
    private val _results: MutableMap<String, Any?> = mutableMapOf()
    private val _resultEvents = MutableSharedFlow<Pair<String, Any?>>(replay = 1)

    // ── TraverseNavigator implementation ──────────────────────────────────────

    override fun navigate(destination: Destination, builder: NavOptions.() -> Unit) {
        val options = NavOptions().apply(builder)
        _navigateCalls += NavigationCall(destination, options)

        // Pop-before-navigate
        options.popUpTo?.let { target ->
            val index = _backStack.indexOfLast { it == target }
            if (index >= 0) {
                val removeFrom = if (options.popUpToInclusive) index else index + 1
                repeat(_backStack.size - removeFrom) { _backStack.removeLastOrNull() }
            } else {
                _backStack.clear()
            }
        }

        // launchSingleTop guard — skip push if top is already the same type
        if (options.launchSingleTop &&
            _backStack.lastOrNull()?.let { it::class } == destination::class
        ) return

        _backStack.add(destination)
    }

    override fun navigateUp(): Boolean {
        if (_backStack.size <= 1) return false
        navigateUpCount++
        _backStack.removeLastOrNull()
        return true
    }

    override fun popTo(destination: Destination, inclusive: Boolean): Boolean {
        val index = _backStack.indexOfLast { it == destination }
        if (index < 0) return false
        _popToCalls += PopToCall(destination, inclusive)
        val removeFrom = if (inclusive) index else index + 1
        repeat(_backStack.size - removeFrom) { _backStack.removeLastOrNull() }
        return true
    }

    override fun <T> setResult(key: String, value: T) {
        _results[key] = value
        _resultEvents.tryEmit(key to value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun clearResult(key: String) {
        _results.remove(key)
        if (_results.isEmpty()) _resultEvents.resetReplayCache()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observeResult(key: String): Flow<T> =
        _resultEvents
            .filter { it.first == key }
            .map { it.second as T }

    override fun navigateToDeepLink(uri: String): Boolean {
        _deepLinkCalls += uri
        return false // FakeTraverseNavigator has no deep-link registry; use assertDeepLinkNavigatedTo only.
    }

    // ── Test helpers ──────────────────────────────────────────────────────────

    /**
     * Clears all recorded navigation calls ([navigateCalls], [popToCalls], [navigateUpCount])
     * and all stored results. Does **not** modify the back stack.
     */
    public fun reset() {
        _navigateCalls.clear()
        _popToCalls.clear()
        _deepLinkCalls.clear()
        navigateUpCount = 0
        _results.clear()
    }
}

/**
 * A single recorded [TraverseNavigator.navigate] call.
 *
 * @property destination The destination that was navigated to.
 * @property options     The [NavOptions] applied to this call.
 */
public data class NavigationCall(
    val destination: Destination,
    val options: NavOptions,
)

/**
 * A single recorded [TraverseNavigator.popTo] call.
 *
 * @property destination The destination that was popped to.
 * @property inclusive   Whether the destination itself was also popped.
 */
public data class PopToCall(
    val destination: Destination,
    val inclusive: Boolean,
)

