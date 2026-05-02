package dev.teogor.traverse.compose.backgesture

import androidx.compose.ui.Modifier

/** Android: `BackHandler` already handles system back — no key modifier needed. */
internal actual fun Modifier.traverseBackKeyModifier(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier = this

