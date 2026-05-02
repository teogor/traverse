package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// TODO: JS/browser back navigation via browser history API (planned post-alpha).
@Composable
internal actual fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

