package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// Desktop back navigation is handled at the root Box level via traverseBackKeyModifier
// (Escape / Alt+Left key events via onPreviewKeyEvent). This composable is intentionally
// a no-op — see TraverseKeyModifier.kt in this package for the keyboard implementation.
@Composable
internal actual fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

