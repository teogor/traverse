package dev.teogor.traverse.compose.deeplink

import dev.teogor.traverse.compose.internal.EntrySpec
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Maintains the set of URI pattern → destination registrations compiled from [EntrySpec.deepLinks].
 *
 * Built by [TraverseHost] from the [TraverseGraphBuilder] entries and injected into
 * [DefaultTraverseNavigator] before the first composition.
 *
 * Thread safety: mutated only during the initial composition; afterwards read-only.
 */
internal class TraverseDeepLinkRegistry {

    private class Entry(
        val compiled: TraverseDeepLinkMatcher.CompiledPattern,
        val deepLink: TraverseDeepLink,
        val spec: EntrySpec,
    )

    private val entries = mutableListOf<Entry>()

    /**
     * Registers all deep links declared on [spec].
     * Call once per [EntrySpec] during graph construction.
     */
    fun register(spec: EntrySpec) {
        spec.deepLinks.forEach { dl ->
            entries += Entry(
                compiled = TraverseDeepLinkMatcher.compile(dl.uriPattern),
                deepLink = dl,
                spec = spec,
            )
        }
    }

    /**
     * Looks up [uri] against every registered pattern.
     *
     * @return The reconstructed [Destination] instance if a match is found and the
     *   destination can be deserialized from the extracted params, or `null` otherwise.
     */
    fun resolve(uri: String): Destination? {
        for (entry in entries) {
            val params = TraverseDeepLinkMatcher.match(entry.compiled, uri) ?: continue
            val dest = entry.spec.reconstructFromParams(params)
            if (dest != null) return dest
        }
        return null
    }

    /** `true` when no deep links have been registered — used to skip matching altogether. */
    val isEmpty: Boolean get() = entries.isEmpty()
}

// ─── Lenient JSON instance used only for deep-link destination reconstruction ─────────────
private val deepLinkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Reconstructs a [Destination] from the URI-extracted [params] map using `kotlinx.serialization`.
 *
 * - For `data object` destinations (no params) simply decodes an empty JSON object.
 * - For `data class` destinations each extracted String value is converted to the most
 *   appropriate primitive JSON type (Long, Double, Boolean, or String) before decoding,
 *   so fields typed as `Int`, `Long`, `Boolean`, etc. round-trip correctly.
 *
 * Returns `null` when the serializer is absent or deserialization fails.
 */
@Suppress("UNCHECKED_CAST")
private fun EntrySpec.reconstructFromParams(params: Map<String, String>): Destination? {
    val serializer = this.serializer ?: return null
    return try {
        if (params.isEmpty()) {
            // data object — ObjectSerializer ignores the JSON content.
            deepLinkJson.decodeFromString(serializer as KSerializer<Destination>, "{}")
        } else {
            val jsonObject = buildJsonObject {
                params.forEach { (key, value) ->
                    val jsonPrimitive = when {
                        value.toLongOrNull() != null -> JsonPrimitive(value.toLong())
                        value.toDoubleOrNull() != null -> JsonPrimitive(value.toDouble())
                        value == "true" || value == "false" -> JsonPrimitive(value.toBoolean())
                        else -> JsonPrimitive(value)
                    }
                    put(key, jsonPrimitive)
                }
            }
            deepLinkJson.decodeFromJsonElement(
                serializer as KSerializer<Destination>,
                jsonObject,
            )
        }
    } catch (_: Exception) {
        null
    }
}

