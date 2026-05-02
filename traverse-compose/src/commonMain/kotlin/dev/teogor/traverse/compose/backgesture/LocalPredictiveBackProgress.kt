package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Composition local that carries the current predictive-back gesture progress (0.0 – 1.0).
 *
 * - **Android 14+ (API 34+)**: updated continuously as the user swipes back via
 *   [PredictiveBackHandler][androidx.activity.compose.PredictiveBackHandler].
 * - **All other platforms / Android < 14**: always `0f` (feature not supported).
 *
 * Screen composables can read this value to apply a custom leave animation:
 *
 * ```kotlin
 * @Composable
 * fun ProfileScreen() {
 *     val backProgress = LocalPredictiveBackProgress.current
 *     val scale by animateFloatAsState(if (backProgress > 0f) 1f - backProgress * 0.08f else 1f)
 *     Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
 *         // … your content …
 *     }
 * }
 * ```
 */
public val LocalPredictiveBackProgress: ProvidableCompositionLocal<Float> =
    compositionLocalOf { 0f }

/**
 * Composition local that carries the swipe edge of the active predictive-back gesture.
 *
 * - `0` — gesture started from the **left** edge ([BackEventCompat.EDGE_LEFT]).
 * - `1` — gesture started from the **right** edge ([BackEventCompat.EDGE_RIGHT]).
 * - `-1` — no active gesture (default / gesture ended).
 *
 * Only meaningful on Android 14+ (API 34+); always `-1` on all other platforms.
 */
public val LocalPredictiveBackSwipeEdge: ProvidableCompositionLocal<Int> =
    compositionLocalOf { -1 }





