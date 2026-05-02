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




