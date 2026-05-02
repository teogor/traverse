package dev.teogor.traverse.compose.backgesture

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlin.coroutines.cancellation.CancellationException

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
 * Per the Android predictive-back-progress documentation:
 * - On **commit** (flow completes): [onBack] is called; [onProgress]/[onSwipeEdge] reset to
 *   `0f`/`-1` so there is no visual artifact as [AnimatedContent] takes over the exit transition.
 * - On **cancel** (CancellationException): [onProgress] is reset to `0f` and [onSwipeEdge] to
 *   `-1`. TraverseHost drives the stored value through `animateFloatAsState(spring(...))` so the
 *   screen bounces back smoothly instead of snapping abruptly.
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
            // Reset visual state before handing off to AnimatedContent exit transition.
            onProgress(0f)
            onSwipeEdge(-1)
            onBack()
        } catch (e: CancellationException) {
            // Gesture was cancelled (user pulled finger back without completing).
            // Reset progress — TraverseHost's animateFloatAsState(spring()) will animate
            // the value smoothly back from wherever the gesture was back to 0f.
            onProgress(0f)
            onSwipeEdge(-1)
            throw e  // must re-throw so the coroutine is properly cancelled
        }
    }
}

