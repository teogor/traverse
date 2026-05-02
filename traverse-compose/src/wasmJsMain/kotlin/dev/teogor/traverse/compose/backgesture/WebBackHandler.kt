package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * WasmJs (browser) back handler with full browser history API integration.
 *
 * Identical behaviour to the JS target — see [JsBackHandler] for documentation.
 * The only difference is the WasmJs FFI: event listeners use `JsAny?` parameters and
 * [`kotlinx.browser`][kotlinx.browser.window] is accessed via the Wasm browser module.
 *
 * [onProgress] is not applicable for browser back navigation and is ignored.
 */
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
) {
    DisposableEffect(enabled, backStackSize) {
        if (!enabled) {
            onDispose { }
        } else {
            kotlinx.browser.window.history.pushState(
                null,
                "",
                kotlinx.browser.window.location.href,
            )
            val listener: (JsAny?) -> Unit = { onBack() }
            kotlinx.browser.window.addEventListener("popstate", listener)
            onDispose {
                kotlinx.browser.window.removeEventListener("popstate", listener)
            }
        }
    }
}
