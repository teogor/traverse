package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// Desktop back navigation is handled at the root Box level via traverseBackKeyModifier
// (Escape / Alt+Left key events via onPreviewKeyEvent). This composable is intentionally
// a no-op — see TraverseKeyModifier.kt in this package for the keyboard implementation.
// onProgress, onSwipeEdge and backStackSize are not applicable on desktop and are ignored.
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    backStackSize: Int,
    onProgress: (Float) -> Unit,
    onSwipeEdge: (Int) -> Unit,
    onBack: () -> Unit,
) = Unit

