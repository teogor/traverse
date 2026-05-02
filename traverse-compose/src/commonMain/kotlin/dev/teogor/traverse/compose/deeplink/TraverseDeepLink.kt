package dev.teogor.traverse.compose.deeplink

/**
 * Represents a URI pattern that maps to a navigation destination.
 *
 * Register deep links on a destination via [TraverseGraphBuilder.screen]:
 * ```kotlin
 * screen<UserProfile>(
 *     deepLinks = listOf(
 *         deepLink("https://example.com/user/{userId}"),
 *         deepLink("myapp://profile/{userId}"),
 *     )
 * ) { dest -> UserProfileScreen(dest.userId) }
 * ```
 *
 * ## URI pattern syntax
 *
 * Use `{paramName}` as a placeholder for any path segment:
 * - `https://example.com/user/{userId}` — matches `https://example.com/user/42`,
 *   extracting `userId = "42"`.
 * - `traverse://app/item/{id}/detail` — mandatory middle segment.
 * - `myapp://search?q={query}` — query parameter extraction.
 *
 * Extracted parameter names must match the constructor parameter names of the destination
 * `data class`. The values are automatically converted from String to the field's type
 * (Int, Long, Double, Boolean, String) using the destination's `@Serializable` serializer.
 *
 * @param uriPattern   URI with `{param}` placeholders.
 * @param action       Optional action string (Android intent action matching, ignored on other platforms).
 * @param mimeType     Optional MIME type (Android intent data type matching, ignored on other platforms).
 */
public data class TraverseDeepLink(
    val uriPattern: String,
    val action: String? = null,
    val mimeType: String? = null,
)

/**
 * Creates a [TraverseDeepLink] for [uriPattern].
 *
 * ```kotlin
 * screen<Profile>(
 *     deepLinks = listOf(
 *         deepLink("https://app.example.com/profile/{userId}"),
 *     )
 * ) { dest -> ProfileScreen(dest.userId) }
 * ```
 *
 * @param uriPattern URI with `{paramName}` placeholders.
 * @param action     Optional intent action (Android only, ignored elsewhere).
 * @param mimeType   Optional intent MIME type (Android only, ignored elsewhere).
 */
public fun deepLink(
    uriPattern: String,
    action: String? = null,
    mimeType: String? = null,
): TraverseDeepLink = TraverseDeepLink(uriPattern, action, mimeType)

