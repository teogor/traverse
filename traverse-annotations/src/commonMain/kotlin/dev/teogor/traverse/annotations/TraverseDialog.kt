package dev.teogor.traverse.annotations

/**
 * Marks a [Destination][dev.teogor.traverse.core.Destination] class as a dialog navigation
 * destination to be registered with [dialog][dev.teogor.traverse.compose.graph.TraverseGraphBuilder.dialog].
 *
 * The dialog is rendered as a Compose [Dialog] overlay on top of the current back-stack entry.
 * Dismiss by calling `navigator.navigateUp()` from inside the dialog content.
 *
 * ## Usage
 * ```kotlin
 * @TraverseDialog(dismissOnClickOutside = false)
 * @Serializable
 * data class ConfirmDelete(val itemId: String) : Destination
 * ```
 *
 * @param dismissOnBackPress Whether pressing the system back button dismisses the dialog.
 *   Defaults to `true` (matches [DialogProperties][androidx.compose.ui.window.DialogProperties] default).
 * @param dismissOnClickOutside Whether clicking outside the dialog dismisses it.
 *   Defaults to `true` (matches [DialogProperties][androidx.compose.ui.window.DialogProperties] default).
 *
 * @see TraverseScreen
 * @see TraverseBottomSheet
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class TraverseDialog(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
)

