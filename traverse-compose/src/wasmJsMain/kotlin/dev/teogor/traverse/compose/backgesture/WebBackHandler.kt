package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// TODO: Web back navigation via browser popstate event (planned post-alpha).
@Composable
internal actual fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

