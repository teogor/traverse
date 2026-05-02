package dev.teogor.traverse.core

/**
 * Marker interface for all navigation destinations in Traverse.
 *
 * `Destination` is intentionally a plain Kotlin interface with **no framework dependencies**.
 * It does not extend any external navigation type — `traverse-core` has zero framework
 * dependencies, making it fully portable across all platforms and independently testable.
 * `traverse-compose` builds on top of `Destination` without leaking any internal types.
 *
 * ## Requirements
 *
 * Every concrete destination **must** also be annotated with `@Serializable`. This is required
 * for back-stack state saving on non-JVM platforms (iOS, Web). Traverse validates this at
 * runtime when the destination is registered in [TraverseGraphBuilder][dev.teogor.traverse.compose.graph.TraverseGraphBuilder].
 *
 * ## Examples
 *
 * ```kotlin
 * // A destination with no arguments — use data object
 * @Serializable data object Home : Destination
 *
 * // A destination with typed arguments — use data class
 * @Serializable data class UserProfile(val userId: String) : Destination
 *
 * // A nested graph key — also a Destination
 * @Serializable data object OnboardingGraph : Destination
 * ```
 */
public interface Destination
