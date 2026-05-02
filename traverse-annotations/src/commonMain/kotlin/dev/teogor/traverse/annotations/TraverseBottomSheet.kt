package dev.teogor.traverse.annotations

/**
 * Marks a [Destination][dev.teogor.traverse.core.Destination] class as a bottom sheet navigation
 * destination to be registered with [bottomSheet][dev.teogor.traverse.compose.graph.TraverseGraphBuilder.bottomSheet].
 *
 * The content is rendered inside a `ModalBottomSheet`. Dismissing (swipe-down or tap scrim)
 * automatically calls `navigator.navigateUp()`.
 *
 * ## Usage
 * ```kotlin
 * @TraverseBottomSheet
 * @Serializable
 * data object TagPicker : Destination
 *
 * @TraverseBottomSheet(skipPartiallyExpanded = true)
 * @Serializable
 * data object FullHeightSheet : Destination
 * ```
 *
 * @param skipPartiallyExpanded When `true`, the sheet jumps straight to fully expanded (no half-height
 *   state). Defaults to `false`.
 *
 * @see TraverseScreen
 * @see TraverseDialog
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class TraverseBottomSheet(
    val skipPartiallyExpanded: Boolean = false,
)

