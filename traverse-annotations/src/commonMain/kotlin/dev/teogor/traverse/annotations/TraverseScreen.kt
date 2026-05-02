package dev.teogor.traverse.annotations

/**
 * Marks a [Destination][dev.teogor.traverse.core.Destination] class as a full-screen navigation
 * destination to be registered with [screen][dev.teogor.traverse.compose.graph.TraverseGraphBuilder.screen].
 *
 * The annotated class **must** also carry `@Serializable` for back-stack state saving.
 *
 * ## Usage
 * ```kotlin
 * @TraverseScreen
 * @ScreenMeta(name = "Home", group = "main")
 * @Serializable
 * data object Home : Destination
 *
 * @TraverseScreen
 * @DeepLink("https://example.com/user/{userId}")
 * @Transition(TransitionPreset.HORIZONTAL_SLIDE)
 * @Serializable
 * data class Profile(val userId: String) : Destination
 * ```
 *
 * When `traverse-ksp-processor` is applied to your project, the annotation processor
 * generates type-safe navigation helpers (`navigateTo*` extensions and `traverseAutoGraph()`).
 *
 * @see TraverseDialog
 * @see TraverseBottomSheet
 * @see TraverseRoot
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class TraverseScreen

