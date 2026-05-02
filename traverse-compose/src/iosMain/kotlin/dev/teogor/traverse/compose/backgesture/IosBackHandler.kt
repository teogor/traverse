package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// iOS handles swipe-back natively via the Compose iOS UIKit integration.
// onProgress, onSwipeEdge and backStackSize are not applicable on iOS and are ignored.
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,
    onProgress: (Float) -> Unit,
    onSwipeEdge: (Int) -> Unit,
    onBack: () -> Unit,
) = Unit

