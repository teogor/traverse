package dev.teogor.traverse.annotations

/**
 * Associates one or more deep-link URI patterns with a navigation destination.
 *
 * Repeatable — apply multiple times to register multiple URI patterns for the same destination.
 * Parameters in the URI pattern are denoted with `{paramName}` and are matched to the
 * destination's constructor fields by name via `kotlinx.serialization`.
 *
 * ## Usage
 * ```kotlin
 * @TraverseScreen
 * @DeepLink("https://example.com/user/{userId}")
 * @DeepLink("app://profile/{userId}")
 * @Serializable
 * data class Profile(val userId: String) : Destination
 *
 * // Single pattern
 * @TraverseScreen
 * @DeepLink("traverse://demo/target/{id}")
 * @Serializable
 * data class DeepLinkTarget(val id: String) : Destination
 * ```
 *
 * When `traverse-ksp-processor` is applied, the processor generates a `{Class}Route.kt` file
 * containing the deep link list and, for data-class destinations, a type-safe deep link builder
 * function.
 *
 * @param pattern URI pattern where `{param}` placeholders match destination constructor fields.
 *   Supports both path segments (`/user/{id}`) and query parameters (`?tab={tab}`).
 *
 * @see TraverseScreen
 * @see TraverseDialog
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Repeatable
public annotation class DeepLink(val pattern: String)

