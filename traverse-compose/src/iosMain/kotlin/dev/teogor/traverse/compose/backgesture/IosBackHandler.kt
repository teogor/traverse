package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

// iOS handles swipe-back natively via the Compose iOS UIKit integration.
// onProgress is not applicable on iOS and is ignored.
@Composable
internal actual fun TraverseBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
) = Unit

