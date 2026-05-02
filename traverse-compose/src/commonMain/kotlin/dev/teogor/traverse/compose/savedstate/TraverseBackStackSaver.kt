package dev.teogor.traverse.compose.savedstate

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import dev.teogor.traverse.compose.graph.TraverseGraphBuilder
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

/**
 * Builds a [Saver] that persists a `SnapshotStateList<Destination>` to a JSON `String`.
 *
 * Serialization uses a `polymorphic` [SerializersModule] populated from the `KSerializer`
 * instances that were captured during [TraverseGraphBuilder] registration (via the `reified T`
 * inline `screen {}` / `dialog {}` / `bottomSheet {}` blocks).
 *
 * ### Graceful degradation
 * - If a destination type was registered without a serializer (e.g. a non-`@Serializable`
 *   class), it is silently skipped; that destination cannot be persisted and will be lost
 *   on process death / configuration change.
 * - If the back-stack JSON cannot be decoded (e.g. after a schema change during an app update),
 *   `restore` returns `null` so the caller falls back to the initial value.
 *
 * @param graphBuilder The builder whose entries carry per-type serializers.
 * @return A `Saver` mapping `SnapshotStateList<Destination>` ↔ `String`, or `null` if no
 *   entry has a serializer (making `rememberSaveable` fall back to in-memory state).
 */
internal fun buildBackStackSaver(
    graphBuilder: TraverseGraphBuilder,
): Saver<SnapshotStateList<Destination>, String>? {
    // Collect (KClass, KSerializer) pairs for all registered @Serializable destinations.
    val serializers: List<Pair<KClass<Destination>, KSerializer<Destination>>> =
        graphBuilder.entries.mapNotNull { spec ->
            @Suppress("UNCHECKED_CAST")
            val ser = spec.serializer as? KSerializer<Destination> ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            val klass = spec.klass as KClass<Destination>
            klass to ser
        }

    // Nothing to serialize — skip the saver entirely.
    if (serializers.isEmpty()) return null

    val module = SerializersModule {
        polymorphic(Destination::class) {
            serializers.forEach { (klass, ser) ->
                @Suppress("UNCHECKED_CAST")
                subclass(klass, ser)
            }
        }
    }

    val json = Json {
        serializersModule = module
        ignoreUnknownKeys = true
        // Use class discriminator so the polymorphic JSON includes a "type" field.
        classDiscriminator = "type"
    }

    val listSerializer = ListSerializer(PolymorphicSerializer(Destination::class))

    return Saver(
        save = { list ->
            try {
                json.encodeToString(listSerializer, list.toList())
            } catch (_: Exception) {
                // Serialization failed (e.g. a destination has no serializer).
                null
            }
        },
        restore = { str ->
            try {
                json.decodeFromString(listSerializer, str).toMutableStateList()
            } catch (_: Exception) {
                // Deserialization failed (e.g. schema changed between app versions).
                null
            }
        },
    )
}

