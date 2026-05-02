package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

/**
 * Platform-specific back-gesture handler.
 *
 * - **Android**: wires [PredictiveBackHandler][androidx.activity.compose.PredictiveBackHandler]
 *   from `activity-compose`. On Android 14+ the swipe progress (0.0→1.0) is reported via
 *   [onProgress] and the gesture edge (`0` = left, `1` = right) via [onSwipeEdge] so the host
 *   can animate the leave transition. On older Android versions the flow completes immediately
 *   (no animation possible) and [onProgress] is called once with `0f`.
 * - **iOS**: no-op (Compose iOS handles swipe-back natively via UINavigationController).
 * - **Desktop (JVM)**: no-op in alpha — keyboard shortcut support planned.
 * - **Web (wasmJs/JS)**: uses the browser `popstate` event. [backStackSize] is used as a key so
 *   that a new `history.pushState()` entry is pushed for each Traverse navigation, keeping the
 *   browser history depth in sync with the Traverse back stack depth.
 *
 * @param enabled       Whether back navigation is currently possible.
 * @param backStackSize Current Traverse back-stack depth. Used by browser targets to push one
 *                      browser history entry per Traverse navigation.
 * @param onProgress    Called (0.0 → 1.0) while a predictive back swipe is in progress.
 *                      Called with `0f` when the gesture ends (committed or cancelled).
 *                      Non-Android platforms always ignore this callback.
 * @param onSwipeEdge   Called with `0` (left edge) or `1` (right edge) when a predictive back
 *                      gesture starts. Called with `-1` when the gesture ends. Non-Android
 *                      platforms always ignore this callback.
 * @param onBack        Called when the back action is confirmed.
 */
@Composable
internal expect fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,
    onProgress: (Float) -> Unit,
    onSwipeEdge: (Int) -> Unit,
    onBack: () -> Unit,
)

