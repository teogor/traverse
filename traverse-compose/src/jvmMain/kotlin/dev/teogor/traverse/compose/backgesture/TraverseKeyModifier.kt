package dev.teogor.traverse.compose.backgesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Desktop (JVM): intercepts `Escape` and `Alt+Left` key events to trigger back navigation.
 *
 * Uses `onPreviewKeyEvent` so that the event is consumed before any focused child
 * composable (e.g. a text field) processes it.  This gives consistent Escape-goes-back
 * behaviour across the whole window, which is the standard UX for desktop navigation.
 */
internal actual fun Modifier.traverseBackKeyModifier(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier {
    if (!enabled) return this
    return this.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            event.key == Key.Escape -> {
                onBack()
                true
            }
            event.key == Key.DirectionLeft && event.isAltPressed -> {
                onBack()
                true
            }
            else -> false
        }
    }
}

