package dev.teogor.traverse.core.navigator

import dev.teogor.traverse.core.Destination

/**
 * Options that influence how a [navigate][TraverseNavigator.navigate] call behaves.
 *
 * Pass via the builder lambda on [TraverseNavigator.navigate]:
 * ```kotlin
 * navigator.navigate(Home) {
 *     launchSingleTop = true
 *     popUpTo = Login
 *     popUpToInclusive = true
 * }
 * ```
 */
public data class NavOptions(

    /**
     * If `true` and the destination being navigated to is already the top of the back stack,
     * the navigation is a no-op — the destination is not pushed a second time.
     *
     * Useful for bottom-nav tab roots: tapping the same tab twice should not stack.
     */
    public var launchSingleTop: Boolean = false,

    /**
     * If set, all back-stack entries above this destination (and optionally the destination
     * itself, controlled by [popUpToInclusive]) are removed before pushing the new destination.
     *
     * `null` means no pop-before-navigate.
     */
    public var popUpTo: Destination? = null,

    /**
     * Whether the [popUpTo] destination itself is also removed from the back stack.
     * Only meaningful when [popUpTo] is non-null.
     */
    public var popUpToInclusive: Boolean = false,

    /**
     * If `true`, and the destination being navigated to previously had its state saved
     * (via [popUpTo] on a prior navigate call), that saved state is restored.
     */
    public var restoreState: Boolean = false,
)

