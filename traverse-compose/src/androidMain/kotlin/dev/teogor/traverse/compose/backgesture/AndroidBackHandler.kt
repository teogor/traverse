package dev.teogor.traverse.compose.backgesture

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation of [TraverseBackHandler].
 *
 * Uses [PredictiveBackHandler] (available since `activity-compose 1.8.0`).
 * - **Android 14+ (API 34+)**: provides predictive swipe animation via a Flow of
 *   [BackEventCompat][androidx.activity.BackEventCompat] events. Progress is forwarded to the
 *   caller via [onProgress] so [TraverseHost] can animate the exit of the current screen.
 * - **Android < 14**: flow completes immediately; [onProgress] gets `0f` then the gesture is
 *   treated as a regular back press.
 *
 * When the gesture is **cancelled** the coroutine is cancelled and [onBack] is never called;
 * [onProgress] is called with `0f` via `finally` to reset any in-progress animation.
 */
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,       // not used by Android — PredictiveBackHandler handles all events
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
) {
    PredictiveBackHandler(enabled = enabled) { backEventFlow ->
        try {
            backEventFlow.collect { event ->
                onProgress(event.progress)
            }
            // Flow completed normally → user committed the back gesture.
            onBack()
        } finally {
            // Reset animation progress regardless of completion or cancellation.
            onProgress(0f)
        }
    }
}

