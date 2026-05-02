package dev.teogor.traverse.core.navigator

import dev.teogor.traverse.core.Destination
import kotlinx.coroutines.flow.Flow

/**
 * The primary navigation interface for Traverse.
 *
 * Obtain the current navigator from [LocalTraverseNavigator][dev.teogor.traverse.compose.navigator.LocalTraverseNavigator]:
 * ```kotlin
 * @Composable
 * fun HomeScreen() {
 *     val navigator = LocalTraverseNavigator.current
 *     Button(onClick = { navigator.navigate(Profile(userId = "42")) }) {
 *         Text("Open Profile")
 *     }
 * }
 * ```
 *
 * ## Implementations
 * - **Production:** `DefaultTraverseNavigator` in `traverse-compose` — backed by a
 *   [SnapshotStateList][androidx.compose.runtime.snapshots.SnapshotStateList]`<Destination>`.
 * - **Tests:** `FakeTraverseNavigator` in `traverse-test` — records all calls for assertion.
 */
public interface TraverseNavigator {

    // ── Back stack ────────────────────────────────────────────────────────────

    /**
     * The current back stack. The last element is the active (visible) destination.
     * Read-only — mutate via [navigate], [navigateUp], and [popTo].
     */
    public val backStack: List<Destination>

    /**
     * The currently active destination — equivalent to `backStack.last()`.
     */
    public val currentDestination: Destination
        get() = backStack.last()

    /**
     * `true` when calling [navigateUp] would actually pop an entry.
     * `false` when the back stack has exactly one entry (at the root).
     */
    public val canNavigateUp: Boolean
        get() = backStack.size > 1

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Navigate to [destination], optionally configuring the behaviour via [builder].
     *
     * ```kotlin
     * // Simple navigate
     * navigator.navigate(Profile(userId = "42"))
     *
     * // Single-top (don't push if already on top)
     * navigator.navigate(Home) { launchSingleTop = true }
     *
     * // Pop-and-navigate (clear stack up to Login before pushing Dashboard)
     * navigator.navigate(Dashboard) {
     *     popUpTo = Login
     *     popUpToInclusive = true
     * }
     * ```
     */
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})

    /**
     * Pop the top entry from the back stack.
     *
     * @return `true` if navigation occurred (back stack had more than one entry),
     *         `false` if already at the root.
     */
    public fun navigateUp(): Boolean

    /**
     * Pop all back-stack entries above [destination].
     *
     * @param inclusive if `true`, [destination] itself is also removed.
     * @return `true` if [destination] was found on the stack and entries were removed;
     *         `false` if it was not found.
     */
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean

    // ── Results ───────────────────────────────────────────────────────────────

    /**
     * Store a result identified by [key] so it can be collected by a preceding destination
     * via [observeResult].
     *
     * Prefer the convenience extensions [setResultAndNavigateUp] and [setResultAndPopTo]
     * rather than calling this directly.
     */
    public fun <T> setResult(key: String, value: T)

    /**
     * Remove a stored result identified by [key].
     * Called automatically by [CollectTraverseResultOnce][dev.teogor.traverse.compose.result.CollectTraverseResultOnce]
     * after the result has been delivered.
     */
    public fun clearResult(key: String)

    /**
     * Returns a cold [Flow] that emits whenever a result with [key] is set.
     *
     * Prefer [CollectTraverseResultOnce][dev.teogor.traverse.compose.result.CollectTraverseResultOnce]
     * which handles collection, type casting, and clearing automatically.
     */
    public fun <T> observeResult(key: String): Flow<T>

    // ── Deep Links ────────────────────────────────────────────────────────────

    /**
     * Navigate to the destination associated with [uri] based on the deep links registered
     * in the current [TraverseHost] graph.
     *
     * Returns `true` if a matching deep link was found and navigation was triggered,
     * `false` if no registered pattern matched [uri].
     *
     * ```kotlin
     * // Programmatic deep-link navigation:
     * val handled = navigator.navigateToDeepLink("https://example.com/user/42")
     *
     * // Android — handle Activity intent:
     * intent?.data?.toString()?.let { navigator.navigateToDeepLink(it) }
     * ```
     */
    public fun navigateToDeepLink(uri: String): Boolean = false
}

