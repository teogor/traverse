package dev.teogor.traverse.annotations

/**
 * Attaches human-readable metadata to a navigation destination.
 *
 * This annotation is optional and purely informational — it has no effect on how Traverse
 * routes navigation. Its values are available to:
 * - The `traverse-ksp-processor`, which embeds them in a generated `ScreenInfo` registry.
 * - Analytics / debugging tools, which can query the registry at runtime.
 * - IDE tooling and documentation generators.
 *
 * ## Usage
 * ```kotlin
 * @TraverseScreen
 * @TraverseRoot
 * @ScreenMeta(
 *     name = "Home",
 *     description = "App entry point — lists all Traverse features.",
 *     group = "main",
 * )
 * @Serializable
 * data object Home : Destination
 *
 * @TraverseScreen
 * @ScreenMeta(name = "Profile", group = "account")
 * @Serializable
 * data class Profile(val userId: String) : Destination
 * ```
 *
 * @param name  Short human-readable display name for this destination (e.g. `"Home"`, `"Profile"`).
 *   Defaults to `""` — the processor falls back to the class simple name when blank.
 * @param description Longer description of this destination's purpose. Shown in debug overlays,
 *   auto-generated docs, and analytics dashboards. Defaults to `""`.
 * @param group Logical grouping key (e.g. `"onboarding"`, `"account"`, `"main"`). Useful for
 *   filtering in Debug Screens panels and analytics funnels. Defaults to `""`.
 *
 * @see TraverseScreen
 * @see ScreenInfo
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class ScreenMeta(
    val name: String = "",
    val description: String = "",
    val group: String = "",
)

