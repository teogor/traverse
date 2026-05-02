package dev.teogor.traverse.compose.backgesture

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation of [TraverseBackHandler].
 *
 * Uses [PredictiveBackHandler] (available since `activity-compose 1.8.0`).
 * - **Android 14+ (API 34+)**: provides predictive swipe animation via a Flow of
 *   [BackEventCompat][androidx.activity.BackEventCompat] events. Progress (0→1) is forwarded via
 *   [onProgress] and the swipe edge (`0`=left, `1`=right) via [onSwipeEdge] so [TraverseHost]
 *   can animate the exit of the current screen.
 * - **Android < 14**: flow completes immediately; [onProgress] gets `0f` and [onSwipeEdge]
 *   gets `-1`; the gesture is treated as a regular back press.
 *
 * When the gesture is **cancelled** the coroutine is cancelled and [onBack] is never called;
 * [onProgress] is called with `0f` and [onSwipeEdge] with `-1` via `finally` to reset any
 * in-progress animation.
 */
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,       // not used by Android — PredictiveBackHandler handles all events
    onProgress: (Float) -> Unit,
    onSwipeEdge: (Int) -> Unit,
    onBack: () -> Unit,
) {
    PredictiveBackHandler(enabled = enabled) { backEventFlow ->
        try {
            backEventFlow.collect { event ->
                onSwipeEdge(event.swipeEdge)
                onProgress(event.progress)
            }
            // Flow completed normally → user committed the back gesture.
            onBack()
        } finally {
            // Reset animation progress and edge regardless of completion or cancellation.
            onProgress(0f)
            onSwipeEdge(-1)
        }
    }
}

