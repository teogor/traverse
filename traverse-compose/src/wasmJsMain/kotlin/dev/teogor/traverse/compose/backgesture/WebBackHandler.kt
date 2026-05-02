package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * WasmJs (browser) back handler.
 *
 * Listens to the browser's `popstate` event to intercept the browser Back button.
 * [onProgress] is not applicable for browser back navigation and is ignored.
 */
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
) {
    DisposableEffect(enabled) {
        val listener: (JsAny?) -> Unit = { if (enabled) onBack() }
        // Push a history entry so the browser has a "previous" state to pop.
        kotlinx.browser.window.history.pushState(null, "", kotlinx.browser.window.location.href)
        kotlinx.browser.window.addEventListener("popstate", listener)
        onDispose { kotlinx.browser.window.removeEventListener("popstate", listener) }
    }
}

