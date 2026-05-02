package dev.teogor.traverse.compose.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.TraverseNavigator

/**
 * Creates and remembers a [TraverseNavigator] with its own back stack, starting at
 * [startDestination].
 *
 * Use this function when you need to maintain a navigator instance **outside** of
 * [TraverseHost] — for example, to implement tab navigation where each tab keeps its
 * own independent back stack across tab switches:
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     var selectedTab by remember { mutableStateOf(Tab.Home) }
 *
 *     val homeNav   = rememberTraverseNavigator(Home)
 *     val searchNav = rememberTraverseNavigator(Search)
 *     val profileNav = rememberTraverseNavigator(Profile)
 *
 *     Column {
 *         Box(Modifier.weight(1f)) {
 *             when (selectedTab) {
 *                 Tab.Home    -> TraverseHost(startDestination = Home,    navigator = homeNav)    { … }
 *                 Tab.Search  -> TraverseHost(startDestination = Search,  navigator = searchNav)  { … }
 *                 Tab.Profile -> TraverseHost(startDestination = Profile, navigator = profileNav) { … }
 *             }
 *         }
 *         NavigationBar {
 *             NavigationBarItem(selected = selectedTab == Tab.Home,    onClick = { selectedTab = Tab.Home })
 *             NavigationBarItem(selected = selectedTab == Tab.Search,  onClick = { selectedTab = Tab.Search })
 *             NavigationBarItem(selected = selectedTab == Tab.Profile, onClick = { selectedTab = Tab.Profile })
 *         }
 *     }
 * }
 * ```
 *
 * When this navigator is passed to [TraverseHost] via the `navigator` parameter:
 * - The full animated navigation engine is used (same as without an external navigator).
 * - Nested graph key resolution is wired up automatically from the `TraverseHost` graph builder.
 * - The `startDestination` parameter of [TraverseHost] is ignored (the back stack already starts
 *   at the destination provided to [rememberTraverseNavigator]).
 *
 * @param startDestination The initial destination in this navigator's back stack.
 * @return A [TraverseNavigator] whose `backStack` is a reactive [SnapshotStateList].
 */
@Composable
public fun rememberTraverseNavigator(startDestination: Destination): TraverseNavigator {
    val backStack = remember { mutableStateListOf(startDestination) }
    return remember(backStack) {
        DefaultTraverseNavigator(
            _backStack = backStack,
            nestedGraphKeys = emptyMap(), // populated by TraverseHost when wired
        )
    }
}

