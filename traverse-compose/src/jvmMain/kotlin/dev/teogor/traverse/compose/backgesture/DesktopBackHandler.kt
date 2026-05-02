package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// TODO: Desktop back navigation via Escape / Alt+Left key events (planned post-alpha).
@Composable
internal actual fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

