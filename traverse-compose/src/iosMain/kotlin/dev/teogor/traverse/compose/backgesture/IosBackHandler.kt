package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// iOS handles swipe-back natively via the Compose iOS UIKit integration.
@Composable
internal actual fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

