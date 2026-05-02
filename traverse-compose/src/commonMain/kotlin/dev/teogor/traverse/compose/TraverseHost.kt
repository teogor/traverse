package dev.teogor.traverse.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import dev.teogor.traverse.compose.backgesture.TraverseBackHandler
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
 *   `startDestination` is shown automatically.
 * @param modifier Applied to the [AnimatedContent] hosting the active screen.
 * @param navigator Override the navigator (useful for tests / previews).
 * @param transitions Default enter/exit animations between screens.
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

    if (navigator != null) {
        // Test / injection mode — render current destination without animation
        CompositionLocalProvider(LocalTraverseNavigator provides navigator) {
            TraverseBackHandler(enabled = navigator.canNavigateUp) { navigator.navigateUp() }
            val dest = navigator.currentDestination
            graphBuilder.findSpec(dest)?.content?.invoke(dest)
        }
    } else {
        TraverseAnimatedHost(
            startDestination = startDestination,
            modifier = modifier,
            transitions = transitions,
            graphBuilder = graphBuilder,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TraverseAnimatedHost(
    startDestination: Destination,
    modifier: Modifier,
    transitions: TraverseTransitionSpec?,
    graphBuilder: TraverseGraphBuilder,
) {
    // Resolve graph-key → actual start destination for nested graphs
    val resolvedStart = graphBuilder.nestedGraphKeys[startDestination::class] ?: startDestination

    val backStack = remember { mutableStateListOf(resolvedStart) }
    val navigator = remember(backStack) {
        DefaultTraverseNavigator(backStack, graphBuilder.nestedGraphKeys)
    }

    CompositionLocalProvider(LocalTraverseNavigator provides navigator) {
        TraverseBackHandler(enabled = navigator.canNavigateUp) { navigator.navigateUp() }

        val topDest = backStack.lastOrNull() ?: return@CompositionLocalProvider
        val topSpec = graphBuilder.findSpec(topDest)
        val isOverlay = topSpec?.type == EntryType.DIALOG || topSpec?.type == EntryType.BOTTOM_SHEET

        // The screen that drives AnimatedContent (skip overlay entries when looking)
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
                    // Pop detection: the outgoing destination is no longer on the stack
                    val isPop = !backStack.contains(initialState)
                    if (isPop) {
                        val enter = transitions?.popEnterTransition?.invoke() ?: fadeIn()
                        val exit = transitions?.popExitTransition?.invoke() ?: fadeOut()
                        enter togetherWith exit
                    } else {
                        val enter = transitions?.enterTransition?.invoke() ?: fadeIn()
                        val exit = transitions?.exitTransition?.invoke() ?: fadeOut()
                        enter togetherWith exit
                    }
                },
                contentKey = { it },
                label = "TraverseHost",
            ) { dest ->
                graphBuilder.findSpec(dest)?.content?.invoke(dest)
            }
        }

        // Render dialog / bottom sheet overlays above AnimatedContent
        if (isOverlay && topSpec != null) {
            when (topSpec.type) {
                EntryType.DIALOG -> {
                    Dialog(onDismissRequest = { navigator.navigateUp() }) {
                        topSpec.content(topDest)
                    }
                }
                EntryType.BOTTOM_SHEET -> {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { navigator.navigateUp() },
                        sheetState = sheetState,
                    ) {
                        topSpec.content(topDest)
                    }
                }
                EntryType.SCREEN -> Unit // never reached when isOverlay is true
            }
        }
    }
}


