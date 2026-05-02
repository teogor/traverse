package dev.teogor.traverse.compose.deeplink

/**
 * Compiles URI patterns with `{param}` placeholders into regex-based matchers and
 * extracts named parameter values from concrete URIs.
 *
 * All KMP targets are supported — uses positional capture groups rather than named groups
 * to remain compatible with Kotlin/JS and Kotlin/Native.
 *
 * Examples:
 * - Pattern `https://example.com/user/{userId}` + URI `https://example.com/user/42`
 *   → `{userId: "42"}`
 * - Pattern `myapp://item/{id}/details` + URI `myapp://item/abc/details`
 *   → `{id: "abc"}`
 * - Pattern `myapp://search?q={query}` + URI `myapp://search?q=kotlin`
 *   → `{query: "kotlin"}`
 */
internal object TraverseDeepLinkMatcher {

    private val PLACEHOLDER_REGEX = Regex("""\{([^/?#}]+)\}""")

    /**
     * A compiled pattern ready for repeated matching.
     *
     * @param regex      Match regex derived from [uriPattern] with placeholders converted to `([^/?#]+)`.
     * @param paramNames Ordered list of placeholder names — aligns with capture-group indices.
     */
    internal data class CompiledPattern(
        val regex: Regex,
        val paramNames: List<String>,
    )

    /**
     * Converts [uriPattern] to a [CompiledPattern].
     *
     * Special regex characters in the literal parts of the pattern are escaped.
     * `{param}` placeholders become `([^/?#]+)` capture groups.
     * An optional trailing query string / fragment is allowed via `(?:[?#].*)?$`.
     */
    internal fun compile(uriPattern: String): CompiledPattern {
        val paramNames = mutableListOf<String>()
        val regexSource = buildString {
            append("^")
            var lastEnd = 0
            PLACEHOLDER_REGEX.findAll(uriPattern).forEach { match ->
                // Escape the literal text before this placeholder.
                append(Regex.escape(uriPattern.substring(lastEnd, match.range.first)))
                paramNames += match.groupValues[1]
                // Each placeholder becomes a positional capture group.
                append("([^/?#]+)")
                lastEnd = match.range.last + 1
            }
            // Escape any remaining literal text after the last placeholder.
            append(Regex.escape(uriPattern.substring(lastEnd)))
            // Allow optional query string or fragment to trail the matched path.
            append("(?:[?#].*)?$")
        }
        return CompiledPattern(Regex(regexSource), paramNames)
    }

    /**
     * Attempts to match [uri] against [compiled].
     *
     * @return A map of `paramName → value` if the URI matches, or `null` if it does not.
     *   Query parameters beyond those in the pattern are also extracted and merged in.
     */
    internal fun match(compiled: CompiledPattern, uri: String): Map<String, String>? {
        // Strip fragment before matching.
        val uriNoFragment = uri.substringBefore("#")
        val mr = compiled.regex.find(uriNoFragment) ?: return null

        val params = mutableMapOf<String, String>()

        // Extract path placeholder values from positional capture groups.
        compiled.paramNames.forEachIndexed { i, name ->
            val value = mr.groupValues.getOrNull(i + 1)
            if (!value.isNullOrEmpty()) params[name] = value
        }

        // Also extract query parameters so patterns like `{q}` in the query part work,
        // and extra query params (not in the pattern) are available if the destination
        // has matching field names.
        if (uriNoFragment.contains('?')) {
            uriNoFragment.substringAfter('?').split('&').forEach { pair ->
                val eq = pair.indexOf('=')
                if (eq > 0) params[pair.substring(0, eq)] = pair.substring(eq + 1)
            }
        }

        return params
    }
}

