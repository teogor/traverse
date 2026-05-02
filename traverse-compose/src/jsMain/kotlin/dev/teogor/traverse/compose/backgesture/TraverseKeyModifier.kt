package dev.teogor.traverse.compose.backgesture

import androidx.compose.ui.Modifier

/** JS browser: browser history API integration planned — no-op stub. */
internal actual fun Modifier.traverseBackKeyModifier(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier = this

