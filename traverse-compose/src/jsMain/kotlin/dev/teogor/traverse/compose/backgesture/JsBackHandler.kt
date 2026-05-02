package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * JS (browser) back handler with full browser history API integration.
 *
 * ### How it works
 * 1. Every time the Traverse back stack grows ([backStackSize] increases), a new
 *    `history.pushState()` entry is added so the browser's Back button can be pressed
 *    once per Traverse page.
 * 2. When `popstate` fires (user pressed the browser Back button), [onBack] is called
 *    (which triggers [TraverseNavigator.navigateUp]).
 * 3. When [enabled] is `false` (back stack has exactly one entry), no listener is
 *    registered — the browser's native back behaviour is preserved.
 *
 * [onProgress] and [onSwipeEdge] are not applicable for browser back navigation and are ignored.
 */
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,
    onProgress: (Float) -> Unit,
    onSwipeEdge: (Int) -> Unit,
    onBack: () -> Unit,
) {
    // Use both `enabled` and `backStackSize` as keys:
    //  • When backStackSize increases (new navigation) → push a new history entry so
    //    the browser has a matching entry to pop.
    //  • When enabled changes false→true (first navigate away from root) → start listening.
    //  • When enabled changes true→false (popped back to root) → remove listener.
    DisposableEffect(enabled, backStackSize) {
        if (!enabled) {
            // At root — no listener needed; let the browser navigate away normally.
            onDispose { }
        } else {
            // Push a browser history entry for this Traverse page.
            // This keeps the browser history depth aligned with the Traverse stack depth.
            kotlinx.browser.window.history.pushState(
                null,
                "",
                kotlinx.browser.window.location.href,
            )
            val listener: (dynamic) -> Unit = { onBack() }
            kotlinx.browser.window.addEventListener("popstate", listener)
            onDispose {
                kotlinx.browser.window.removeEventListener("popstate", listener)
            }
        }
    }
}
