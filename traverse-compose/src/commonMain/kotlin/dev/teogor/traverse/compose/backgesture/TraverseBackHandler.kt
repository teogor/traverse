package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

/**
 * Platform-specific back-gesture handler.
 *
 * - **Android**: wires [PredictiveBackHandler][androidx.activity.compose.PredictiveBackHandler]
 *   from `activity-compose`. On Android 14+ the swipe progress (0.0→1.0) is reported via
 *   [onProgress] so the host can animate the leave transition. On older Android versions the
 *   flow completes immediately (no animation possible) and [onProgress] is called once with `0f`.
 * - **iOS**: no-op (Compose iOS handles swipe-back natively via UINavigationController).
 * - **Desktop (JVM)**: no-op in alpha — keyboard shortcut support planned.
 * - **Web (wasmJs/JS)**: uses the browser `popstate` event; [onProgress] is never called.
 *
 * @param enabled    Whether back navigation is currently possible.
 * @param onProgress Called (0.0 → 1.0) while a predictive back swipe is in progress.
 *                   Called with `0f` when the gesture ends (committed or cancelled).
 *                   Non-Android platforms always ignore this callback.
 * @param onBack     Called when the back action is confirmed.
 */
@Composable
internal expect fun TraverseBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
)

