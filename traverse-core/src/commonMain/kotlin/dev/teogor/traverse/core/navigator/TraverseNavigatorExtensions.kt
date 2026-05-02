package dev.teogor.traverse.core.navigator

import dev.teogor.traverse.core.Destination

/**
 * Pop all back-stack entries up to and including the last entry of type [T],
 * then navigate to [destination].
 *
 * Useful for "go home then open X" flows:
 * ```kotlin
 * navigator.navigateAndClearUpTo<Home>(Settings)
 * // Back stack before: [Home, Feed, Profile]
 * // Back stack after:  [Settings]
 * ```
 */
public inline fun <reified T : Destination> TraverseNavigator.navigateAndClearUpTo(
    destination: Destination,
) {
    navigate(destination) {
        popUpTo = backStack.filterIsInstance<T>().lastOrNull()
        popUpToInclusive = true
    }
}

/**
 * Clear the entire back stack and establish [destination] as the sole entry — i.e. the new root.
 *
 * The type parameter [Root] is used only to document intent at the call site;
 * it does not affect runtime behaviour.
 *
 * ```kotlin
 * navigator.launchAsNewRoot<MainGraph>(Home)
 * // Back stack after: [Home]
 * ```
 */
public inline fun <reified Root : Destination> TraverseNavigator.launchAsNewRoot(
    destination: Destination,
) {
    navigate(destination) {
        popUpTo = backStack.firstOrNull()
        popUpToInclusive = true
    }
}

/**
 * Returns `true` if any entry of type [T] exists anywhere on the back stack.
 *
 * ```kotlin
 * if (navigator.isOnBackStack<Home>()) { /* home is reachable via back */ }
 * ```
 */
public inline fun <reified T : Destination> TraverseNavigator.isOnBackStack(): Boolean =
    backStack.filterIsInstance<T>().isNotEmpty()

/**
 * Returns all back-stack entries that are instances of [T], in back-stack order.
 *
 * ```kotlin
 * val allProfiles: List<Profile> = navigator.entriesOf<Profile>()
 * ```
 */
public inline fun <reified T : Destination> TraverseNavigator.entriesOf(): List<T> =
    backStack.filterIsInstance<T>()

