package dev.teogor.traverse.compose.internal

import androidx.compose.runtime.Composable
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@PublishedApi
internal enum class EntryType { SCREEN, DIALOG, BOTTOM_SHEET }

/**
 * Internal descriptor for a single registered destination.
 *
 * [content] is stored as `@Composable (Destination) -> Unit` — the cast from
 * `(T) -> Unit` is done at registration time in [TraverseGraphBuilder] so that
 * dispatch at render time is always type-erased and safe.
 */
@PublishedApi
internal class EntrySpec(
    val klass: KClass<out Destination>,
    val serializer: KSerializer<out Destination>,
    val type: EntryType,
    val content: @Composable (Destination) -> Unit,
)
