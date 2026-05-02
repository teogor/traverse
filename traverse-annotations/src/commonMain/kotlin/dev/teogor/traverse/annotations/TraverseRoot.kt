package dev.teogor.traverse.annotations

/**
 * Marks a destination class as the **start destination** of a navigation graph or nested sub-graph.
 *
 * Annotating a destination with `@TraverseRoot` communicates — to both developers and to the
 * `traverse-ksp-processor` — that this destination is the root of a navigation scope. The KSP
 * processor uses this information to:
 * - Validate that exactly one root exists per graph scope.
 * - Use the annotated destination as the `startDestination` argument of `TraverseHost` in the
 *   generated `traverseAutoGraph()` helper.
 *
 * ## Usage
 * ```kotlin
 * // Root of the whole app
 * @TraverseScreen
 * @TraverseRoot
 * @Serializable
 * data object Home : Destination
 *
 * // Root of a nested onboarding flow — associate it with a graph key
 * @TraverseScreen
 * @TraverseRoot(graphKey = OnboardingGraph::class)
 * @Serializable
 * data object OnboardingWelcome : Destination
 * ```
 *
 * @param graphKey Optional reference to the graph-key destination that this root belongs to.
 *   When omitted, this destination is treated as the top-level app root.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class TraverseRoot(
    val graphKey: kotlin.reflect.KClass<*> = Unit::class,  // Unit::class = "not set" sentinel
)

