package dev.teogor.traverse.compose.backgesture

import androidx.compose.ui.Modifier

/** iOS: Compose Multiplatform handles swipe-back via the UIKit layer — no key modifier needed. */
internal actual fun Modifier.traverseBackKeyModifier(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier = this

