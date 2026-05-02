package dev.teogor.traverse.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import dev.teogor.traverse.compose.backgesture.LocalPredictiveBackProgress
import dev.teogor.traverse.compose.backgesture.TraverseBackHandler
import dev.teogor.traverse.compose.backgesture.traverseBackKeyModifier
import dev.teogor.traverse.compose.deeplink.TraverseDeepLinkRegistry
import dev.teogor.traverse.compose.graph.TraverseGraphBuilder
import dev.teogor.traverse.compose.internal.EntryType
import dev.teogor.traverse.compose.navigator.DefaultTraverseNavigator
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator
import dev.teogor.traverse.compose.transition.TraverseTransitionSpec
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.TraverseNavigator

/**
 * Root composable for Traverse navigation.
 *
 * Maintains a [SnapshotStateList][androidx.compose.runtime.snapshots.SnapshotStateList] of
 * [Destination]s as the back stack, renders the active screen inside [AnimatedContent],
 * and presents dialogs / bottom sheets as composable overlays on top.
 *
 * All navigation-library types are fully internal — callers only interact with Traverse APIs.
 *
 * @param startDestination The first destination shown. If it is a nested-graph key, the graph's
 *   `startDestination` is shown automatically. Ignored when [navigator] is a
 *   [DefaultTraverseNavigator] created by [rememberTraverseNavigator] (the back stack is already
 *   seeded at that point).
 * @param modifier Applied to the root container.
 * @param navigator Override the navigator. Pass a [rememberTraverseNavigator] result for tab
 *   navigation, or a `FakeTraverseNavigator` for unit tests / previews.
 * @param transitions Default enter/exit animations between screens. Per-destination overrides
 *   registered via `screen<T>(enterTransition = …)` take precedence over this value.
 * @param builder Register all destinations via [TraverseGraphBuilder].
 */
@Composable
public fun TraverseHost(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navigator: TraverseNavigator? = null,
    transitions: TraverseTransitionSpec? = null,
    builder: TraverseGraphBuilder.() -> Unit,
) {
    val graphBuilder = remember { TraverseGraphBuilder().also(builder) }
    val deepLinkRegistry = remember(graphBuilder) {
        TraverseDeepLinkRegistry().also { reg ->
            graphBuilder.entries.forEach { reg.register(it) }
        }
    }

    when {
        // ── External DefaultTraverseNavigator (rememberTraverseNavigator) ──────
        // Full animated host, using the caller-owned back stack. Nested graph keys are wired
        // here so the navigator can resolve them at navigate() time.
        navigator is DefaultTraverseNavigator -> {
            navigator.nestedGraphKeys = graphBuilder.nestedGraphKeys
            navigator.deepLinkRegistry = deepLinkRegistry
            TraverseAnimatedHostCore(
                backStack = navigator.snapshotBackStack,
                navigator = navigator,
                modifier = modifier,
                transitions = transitions,
                graphBuilder = graphBuilder,
            )
        }

        // ── External TraverseNavigator (test / preview mode) ──────────────────
        // Simple render without animation; used by FakeTraverseNavigator in unit tests.
        navigator != null -> {
            CompositionLocalProvider(LocalTraverseNavigator provides navigator) {
                TraverseBackHandler(
                    enabled = navigator.canNavigateUp,
                    backStackSize = navigator.backStack.size,
                    onProgress = {},        // no animation in test/preview mode
                    onBack = { navigator.navigateUp() },
                )
                val dest = navigator.currentDestination
                graphBuilder.findSpec(dest)?.content?.invoke(dest)
            }
        }

        // ── Default path — host owns the back stack ───────────────────────────
        else -> {
            val resolvedStart = graphBuilder.nestedGraphKeys[startDestination::class] ?: startDestination
            val backStack = remember { mutableStateListOf(resolvedStart) }
            val internalNavigator = remember(backStack) {
                DefaultTraverseNavigator(backStack, graphBuilder.nestedGraphKeys)
            }
            internalNavigator.deepLinkRegistry = deepLinkRegistry
            TraverseAnimatedHostCore(
                backStack = backStack,
                navigator = internalNavigator,
                modifier = modifier,
                transitions = transitions,
                graphBuilder = graphBuilder,
            )
        }
    }
}

/**
 * Shared animated rendering core used by both the default and external-navigator paths.
 *
 * Wraps all content in a root [Box] that applies [traverseBackKeyModifier] — on Desktop this
 * intercepts `Escape` / `Alt+Left` before any child composable, giving keyboard back-navigation
 * without focus gymnastics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TraverseAnimatedHostCore(
    backStack: SnapshotStateList<Destination>,
    navigator: DefaultTraverseNavigator,
    modifier: Modifier,
    transitions: TraverseTransitionSpec?,
    graphBuilder: TraverseGraphBuilder,
) {
    // Root Box — on Desktop, onPreviewKeyEvent intercepts Escape/Alt+Left here.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .traverseBackKeyModifier(
                enabled = navigator.canNavigateUp,
                onBack = { navigator.navigateUp() },
            ),
    ) {
        // Track the predictive-back swipe progress (0.0 → 1.0) so screens can animate.
        // On non-Android platforms this value stays at 0f (onProgress is never called).
        var backProgress by remember { mutableFloatStateOf(0f) }

        CompositionLocalProvider(
            LocalTraverseNavigator provides navigator,
            LocalPredictiveBackProgress provides backProgress,
        ) {
            // Android back-handler (no-op on other platforms — key modifier handles those).
            TraverseBackHandler(
                enabled = navigator.canNavigateUp,
                backStackSize = backStack.size,
                onProgress = { backProgress = it },
                onBack = { navigator.navigateUp() },
            )

            val topDest = backStack.lastOrNull() ?: return@CompositionLocalProvider
            val topSpec = graphBuilder.findSpec(topDest)
            val isOverlay = topSpec?.type == EntryType.DIALOG || topSpec?.type == EntryType.BOTTOM_SHEET

            // The screen that drives AnimatedContent (skip overlay entries when looking).
            val screenDest: Destination? = if (isOverlay) {
                backStack.lastOrNull { graphBuilder.findSpec(it)?.type == EntryType.SCREEN }
            } else {
                topDest.takeIf { topSpec?.type == EntryType.SCREEN }
            }

            screenDest?.let { currentScreen ->
                AnimatedContent(
                    targetState = currentScreen,
                    modifier = modifier,
                    transitionSpec = {
                        // Pop detection: the outgoing destination is no longer on the stack.
                        val isPop = !backStack.contains(initialState)
                        val targetSpec = graphBuilder.findSpec(targetState)
                        val initialSpec = graphBuilder.findSpec(initialState)

                        if (isPop) {
                            // popEnter: prefer the incoming (target) spec, fallback to outgoing, then global.
                            val enter = targetSpec?.popEnterTransition?.invoke()
                                ?: initialSpec?.popEnterTransition?.invoke()
                                ?: transitions?.popEnterTransition?.invoke()
                                ?: fadeIn()
                            // popExit: prefer the outgoing (initial) spec, fallback to incoming, then global.
                            val exit = initialSpec?.popExitTransition?.invoke()
                                ?: targetSpec?.popExitTransition?.invoke()
                                ?: transitions?.popExitTransition?.invoke()
                                ?: fadeOut()
                            enter togetherWith exit
                        } else {
                            // enter: prefer the incoming (target) spec, then global.
                            val enter = targetSpec?.enterTransition?.invoke()
                                ?: transitions?.enterTransition?.invoke()
                                ?: fadeIn()
                            // exit: prefer the outgoing (initial) spec, then global.
                            val exit = initialSpec?.exitTransition?.invoke()
                                ?: transitions?.exitTransition?.invoke()
                                ?: fadeOut()
                            enter togetherWith exit
                        }
                    },
                    contentKey = { it },
                    label = "TraverseHost",
                ) { dest ->
                    graphBuilder.findSpec(dest)?.content?.invoke(dest)
                }
            }

            // Render dialog / bottom sheet overlays above AnimatedContent.
            val overlaySpec = topSpec.takeIf { isOverlay }
            if (overlaySpec != null) {
                when (overlaySpec.type) {
                    EntryType.DIALOG -> {
                        Dialog(onDismissRequest = { navigator.navigateUp() }) {
                            overlaySpec.content(topDest)
                        }
                    }
                    EntryType.BOTTOM_SHEET -> {
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ModalBottomSheet(
                            onDismissRequest = { navigator.navigateUp() },
                            sheetState = sheetState,
                        ) {
                            overlaySpec.content(topDest)
                        }
                    }
                    EntryType.SCREEN -> Unit // unreachable: isOverlay is false for SCREEN
                }
            }
        }
    }
}
