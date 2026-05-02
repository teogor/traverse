package dev.teogor.traverse.compose.graph

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.teogor.traverse.compose.deeplink.TraverseDeepLink
import dev.teogor.traverse.compose.internal.EntrySpec
import dev.teogor.traverse.compose.internal.EntryType
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.dsl.TraverseDsl
import kotlinx.serialization.serializer

/**
 * DSL scope for registering navigation destinations inside [TraverseHost].
 *
 * All registration functions are `inline reified` so that Traverse can capture
 * `T::class` at the call site — no code generation needed.
 *
 * ```kotlin
 * TraverseHost(startDestination = Home) {
 *     screen<Home> { HomeScreen() }
 *     screen<UserProfile> { dest -> UserProfileScreen(dest.userId) }
 *     screen<Settings>(
 *         enterTransition = { fadeIn() },
 *         exitTransition  = { fadeOut() },
 *     ) { SettingsScreen() }
 *     dialog<ConfirmDelete> { dest -> ConfirmDeleteDialog(dest.entryId, ...) }
 *     bottomSheet<TagPicker> { TagPickerSheet(...) }
 *     nested(startDestination = OnboardingWelcome, graphKey = Onboarding) {
 *         screen<OnboardingWelcome> { ... }
 *     }
 * }
 * ```
 */
@TraverseDsl
public class TraverseGraphBuilder internal constructor() {

    @PublishedApi
    internal val entries: MutableList<EntrySpec> = mutableListOf()

    // ── Screen ────────────────────────────────────────────────────────────────

    /**
     * Register a full-screen destination of type [T].
     *
     * @param enterTransition    Enter animation when this destination is pushed onto the stack.
     *   Overrides the host-level [TraverseTransitionSpec]. Pass `null` to inherit from host.
     * @param exitTransition     Exit animation when this destination is replaced (another pushes in).
     * @param popEnterTransition Enter animation when returning to this destination via back navigation.
     * @param popExitTransition  Exit animation when this destination is popped off the stack.
     * @param deepLinks          URI patterns that navigate to this destination.
     *   Use [deepLink] to build each entry, e.g. `listOf(deepLink("https://app.example.com/user/{userId}"))`.
     *   Extracted URI params are mapped to the destination constructor fields by name via
     *   `kotlinx.serialization`, so the destination must be `@Serializable`.
     * @param content            Composable content. Receives the strongly-typed destination instance.
     */
    public inline fun <reified T : Destination> screen(
        noinline enterTransition: (() -> EnterTransition)? = null,
        noinline exitTransition: (() -> ExitTransition)? = null,
        noinline popEnterTransition: (() -> EnterTransition)? = null,
        noinline popExitTransition: (() -> ExitTransition)? = null,
        deepLinks: List<TraverseDeepLink> = emptyList(),
        noinline content: @Composable (dest: T) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        entries += EntrySpec(
            klass = T::class,
            serializer = runCatching { serializer<T>() }.getOrNull(),
            type = EntryType.SCREEN,
            content = { dest -> content(dest as T) },
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
            deepLinks = deepLinks,
        )
    }

    // ── Dialog ────────────────────────────────────────────────────────────────

    /**
     * Register a dialog destination of type [T].
     *
     * The content is rendered inside a Compose [Dialog] window, overlaid on top
     * of the current back-stack rendering. Dismiss by calling `navigator.navigateUp()`.
     *
     * Dialogs do not participate in [AnimatedContent] transitions and therefore ignore
     * the transition override parameters.
     *
     * @param content Composable content inside the dialog.
     */
    public inline fun <reified T : Destination> dialog(
        noinline content: @Composable (dest: T) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        entries += EntrySpec(
            klass = T::class,
            type = EntryType.DIALOG,
            content = { dest -> content(dest as T) },
        )
    }

    // ── Bottom Sheet ──────────────────────────────────────────────────────────

    /**
     * Register a bottom sheet destination of type [T].
     *
     * The content is rendered inside a [ModalBottomSheet]. Dismiss (swipe down or
     * tap scrim) automatically calls `navigator.navigateUp()`.
     *
     * Bottom sheets do not participate in [AnimatedContent] transitions and therefore
     * ignore the transition override parameters.
     *
     * @param content Composable content inside the bottom sheet.
     */
    public inline fun <reified T : Destination> bottomSheet(
        noinline content: @Composable (dest: T) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        entries += EntrySpec(
            klass = T::class,
            type = EntryType.BOTTOM_SHEET,
            content = { dest -> content(dest as T) },
        )
    }

    // ── Nested graph ──────────────────────────────────────────────────────────

    /**
     * Register a nested navigation sub-graph.
     *
     * All destinations inside [builder] are added to the flat registry.
     * When [graphKey] is navigated to, it immediately redirects to [startDestination].
     *
     * @param startDestination First destination shown when entering the graph.
     * @param graphKey Optional destination used to navigate into this graph from outside.
     * @param builder DSL lambda for registering nested destinations.
     */
    public fun nested(
        startDestination: Destination,
        graphKey: Destination? = null,
        builder: TraverseGraphBuilder.() -> Unit,
    ) {
        // Collect nested entries into this flat registry
        val nested = TraverseGraphBuilder().also(builder)
        entries.addAll(nested.entries)

        // If a graphKey is provided, register it as a screen that redirects to startDestination
        if (graphKey != null) {
            nestedGraphKeys[graphKey::class] = startDestination
        }
    }

    /** Maps a graph-key destination class to the nested graph's start destination. */
    internal val nestedGraphKeys: MutableMap<kotlin.reflect.KClass<out Destination>, Destination> =
        mutableMapOf()

    /** Returns the first registered [EntrySpec] whose [EntrySpec.klass] matches [dest]'s class. */
    internal fun findSpec(dest: Destination): EntrySpec? =
        entries.firstOrNull { it.klass == dest::class }
}
