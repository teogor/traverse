package dev.teogor.traverse.compose.internal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
 *
 * The four optional transition lambdas override the host-level [TraverseTransitionSpec]
 * for this destination only. `null` means "fall back to the host spec".
 */
@PublishedApi
internal class EntrySpec(
    val klass: KClass<out Destination>,
    /**
     * Serializer for this destination type — reserved for the saved-state milestone.
     * Null until that milestone is implemented.
     */
    val serializer: KSerializer<out Destination>? = null,
    val type: EntryType,
    val content: @Composable (Destination) -> Unit,
    /** Per-destination enter transition override (push). Null = use host spec. */
    val enterTransition: (() -> EnterTransition)? = null,
    /** Per-destination exit transition override (push, outgoing). Null = use host spec. */
    val exitTransition: (() -> ExitTransition)? = null,
    /** Per-destination enter transition override (pop, returning). Null = use host spec. */
    val popEnterTransition: (() -> EnterTransition)? = null,
    /** Per-destination exit transition override (pop, outgoing). Null = use host spec. */
    val popExitTransition: (() -> ExitTransition)? = null,
)
