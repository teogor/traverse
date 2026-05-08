package dev.teogor.traverse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.teogor.traverse.compose.TraverseHost
import dev.teogor.traverse.compose.deeplink.deepLink
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator
import dev.teogor.traverse.compose.navigator.rememberTraverseNavigator
import dev.teogor.traverse.compose.result.CollectTraverseResultOnce
import dev.teogor.traverse.compose.transition.TraverseTransitionSpec
import dev.teogor.traverse.core.navigator.launchAsNewRoot
import dev.teogor.traverse.demo.dialog.ShowcaseDialogContent
import dev.teogor.traverse.demo.feature.AnimationPreviewScreen
import dev.teogor.traverse.demo.feature.AnimationShowcaseScreen
import dev.teogor.traverse.demo.feature.AnnotationItemDetailScreen
import dev.teogor.traverse.demo.feature.AnnotationsShowcaseScreen
import dev.teogor.traverse.demo.feature.ScreenRegistryDemoScreen
import dev.teogor.traverse.demo.feature.ColorPickerScreen
import dev.teogor.traverse.demo.feature.DeepLinkDemoScreen
import dev.teogor.traverse.demo.feature.DeepLinkTargetScreen
import dev.teogor.traverse.demo.feature.DialogDemoScreen
import dev.teogor.traverse.demo.feature.NestedStepScreen
import dev.teogor.traverse.demo.feature.ResultDemoScreen
import dev.teogor.traverse.demo.feature.SheetDemoScreen
import dev.teogor.traverse.demo.feature.SingleTopScreen
import dev.teogor.traverse.demo.feature.StackControlScreen
import dev.teogor.traverse.demo.feature.StackLevelScreen
import dev.teogor.traverse.demo.feature.TypedArgsScreen
import dev.teogor.traverse.demo.screen.CatalogScreen
import dev.teogor.traverse.demo.screen.SplashScreen
import dev.teogor.traverse.demo.sheet.OptionSheetContent

/**
 * Root composable for the Traverse Explorer demo app.
 *
 * @param pendingDeepLink  Optional URI received from an OS deep-link Intent (Android) or
 *   protocol handler (Desktop). Consumed exactly once when the navigator is ready.
 * @param onDeepLinkConsumed Callback invoked after [pendingDeepLink] has been dispatched
 *   so the caller can clear the pending URI and avoid re-navigation on recomposition.
 */
@Composable
fun App(
    pendingDeepLink: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    MaterialTheme {
        // External navigator — lets us handle deep links via LaunchedEffect
        // before the navigator is injected into TraverseHost.
        val navigator = rememberTraverseNavigator(Splash)

        // Dispatch an incoming OS deep link once the navigator (and its registry) is ready.
        // LaunchedEffect runs after the first successful composition, so TraverseHost
        // will have already wired deepLinkRegistry onto the navigator by then.
        LaunchedEffect(pendingDeepLink) {
            val uri = pendingDeepLink ?: return@LaunchedEffect
            navigator.navigateToDeepLink(uri)
            onDeepLinkConsumed()
        }

        TraverseHost(
            startDestination = Splash,
            navigator = navigator,
            transitions = TraverseTransitionSpec.none(),
        ) {
            screen<Splash> {
                val nav = LocalTraverseNavigator.current
                SplashScreen(onEnter = { nav.launchAsNewRoot<Splash>(Catalog) })
            }

            // ── Catalog ───────────────────────────────────────────────────────
            screen<Catalog> {
                val nav = LocalTraverseNavigator.current
                CatalogScreen(
                    onNestedGraph = { nav.navigate(NestedFlowGraph) },
                    onTypedArgs = { nav.navigate(FeatureDetail("kmp")) },
                    onResults = { nav.navigate(ResultDemo) },
                    onDialog = { nav.navigate(DialogDemo) },
                    onSheet = { nav.navigate(SheetDemo) },
                    onStackControl = { nav.navigate(StackControl) },
                    onSingleTop = { nav.navigate(SingleTopDemo) },
                    onDeepLinks = { nav.navigate(DeepLinkDemo) },
                    onAnimations = { nav.navigate(AnimationShowcase) },
                    onAnnotations = { nav.navigate(AnnotationsDemo) },
                    onScreenRegistry = { nav.navigate(ScreenRegistryDemo) },
                )
            }

            // ── Feature: nested() graph ───────────────────────────────────────
            nested(startDestination = NestedStep1, graphKey = NestedFlowGraph) {
                screen<NestedStep1> {
                    val nav = LocalTraverseNavigator.current
                    NestedStepScreen(
                        step = 1,
                        totalSteps = 3,
                        description = "You navigated to NestedFlowGraph. Traverse automatically " +
                            "redirected to NestedStep1 (the graph's startDestination).",
                        onNext = { nav.navigate(NestedStep2) },
                    )
                }
                screen<NestedStep2> {
                    val nav = LocalTraverseNavigator.current
                    NestedStepScreen(
                        step = 2,
                        totalSteps = 3,
                        description = "All nested destinations share the same flat entry registry. " +
                            "The graph key is simply a routing alias — no separate NavGraph objects.",
                        onNext = { nav.navigate(NestedStep3) },
                        onBack = { nav.navigateUp() },
                    )
                }
                screen<NestedStep3> {
                    val nav = LocalTraverseNavigator.current
                    NestedStepScreen(
                        step = 3,
                        totalSteps = 3,
                        description = "Call popTo(Catalog) to exit the entire nested flow in one step, " +
                            "clearing all intermediate destinations.",
                        onNext = { nav.popTo(Catalog) },
                        onBack = { nav.navigateUp() },
                        nextLabel = "Done — Return to Catalog",
                    )
                }
            }

            // ── Feature: Typed Arguments ──────────────────────────────────────
            screen<FeatureDetail>(
                deepLinks = listOf(
                    deepLink("traverse://demo/feature/{featureId}"),
                ),
            ) { dest ->
                val nav = LocalTraverseNavigator.current
                TypedArgsScreen(
                    featureId = dest.featureId,
                    onNavigateWithDifferentId = { id -> nav.navigate(FeatureDetail(id)) },
                )
            }

            // ── Feature: Navigation Results ───────────────────────────────────
            screen<ResultDemo> {
                val nav = LocalTraverseNavigator.current
                var pickedColor by remember { mutableStateOf<String?>(null) }
                CollectTraverseResultOnce<String>(RESULT_COLOR) { pickedColor = it }
                ResultDemoScreen(
                    pickedColor = pickedColor,
                    onPickColor = { nav.navigate(ColorPicker) },
                )
            }
            screen<ColorPicker> {
                val nav = LocalTraverseNavigator.current
                ColorPickerScreen(
                    onColorPicked = { color ->
                        nav.setResult(RESULT_COLOR, color)
                        nav.navigateUp()
                    },
                )
            }

            // ── Feature: dialog<T> ────────────────────────────────────────────
            screen<DialogDemo> {
                val nav = LocalTraverseNavigator.current
                var lastResult by remember { mutableStateOf<Boolean?>(null) }
                CollectTraverseResultOnce<Boolean>(RESULT_DIALOG_CONFIRMED) { lastResult = it }
                DialogDemoScreen(
                    lastResult = lastResult,
                    onOpenDialog = { nav.navigate(ShowcaseDialog("Ready to confirm?")) },
                )
            }
            dialog<ShowcaseDialog> { dest ->
                val nav = LocalTraverseNavigator.current
                ShowcaseDialogContent(
                    message = dest.message,
                    onConfirm = {
                        nav.setResult(RESULT_DIALOG_CONFIRMED, true)
                        nav.navigateUp()
                    },
                    onDismiss = {
                        nav.setResult(RESULT_DIALOG_CONFIRMED, false)
                        nav.navigateUp()
                    },
                )
            }

            // ── Feature: bottomSheet<T> ───────────────────────────────────────
            screen<SheetDemo> {
                val nav = LocalTraverseNavigator.current
                var pickedOption by remember { mutableStateOf<String?>(null) }
                CollectTraverseResultOnce<String>(RESULT_OPTION) { pickedOption = it }
                SheetDemoScreen(
                    pickedOption = pickedOption,
                    onOpenSheet = { nav.navigate(OptionSheet) },
                )
            }
            bottomSheet<OptionSheet> {
                val nav = LocalTraverseNavigator.current
                OptionSheetContent(
                    onOptionSelected = { option ->
                        nav.setResult(RESULT_OPTION, option)
                        nav.navigateUp()
                    },
                    onDismiss = { nav.navigateUp() },
                )
            }

            // ── Feature: Stack Control ────────────────────────────────────────
            screen<StackControl> {
                val nav = LocalTraverseNavigator.current
                StackControlScreen(
                    onNavigateToA = { nav.navigate(StackLevelA) },
                )
            }
            screen<StackLevelA> {
                val nav = LocalTraverseNavigator.current
                StackLevelScreen(
                    level = "A",
                    onGoDeeper = { nav.navigate(StackLevelB) },
                    onPopToStackControl = { nav.popTo(StackControl) },
                    onPopToCatalog = { nav.popTo(Catalog) },
                )
            }
            screen<StackLevelB> {
                val nav = LocalTraverseNavigator.current
                StackLevelScreen(
                    level = "B",
                    onGoDeeper = { nav.navigate(StackLevelC) },
                    onPopToStackControl = { nav.popTo(StackControl) },
                    onPopToCatalog = { nav.popTo(Catalog) },
                )
            }
            screen<StackLevelC> {
                val nav = LocalTraverseNavigator.current
                StackLevelScreen(
                    level = "C",
                    onGoDeeper = null,
                    onPopToStackControl = { nav.popTo(StackControl) },
                    onPopToCatalog = { nav.popTo(Catalog) },
                )
            }

            // ── Feature: launchSingleTop ──────────────────────────────────────
            screen<SingleTopDemo> {
                val nav = LocalTraverseNavigator.current
                SingleTopScreen(
                    stackSize = nav.backStack.size,
                    onNavigateNormal = { nav.navigate(SingleTopDemo) },
                    onNavigateSingleTop = {
                        nav.navigate(SingleTopDemo) { launchSingleTop = true }
                    },
                )
            }

            // ── Feature: Deep Links ───────────────────────────────────────────
            screen<DeepLinkDemo> {
                val nav = LocalTraverseNavigator.current
                DeepLinkDemoScreen(
                    onNavigateToUri = { uri -> nav.navigateToDeepLink(uri) },
                    onNavigateUp = { nav.navigateUp() },
                )
            }
            screen<DeepLinkTarget>(
                deepLinks = listOf(
                    deepLink("traverse://demo/target/{id}"),
                    deepLink("https://traverse.teogor.dev/target/{id}"),
                ),
            ) { dest ->
                val nav = LocalTraverseNavigator.current
                DeepLinkTargetScreen(
                    id = dest.id,
                    onNavigateUp = { nav.navigateUp() },
                )
            }

            // ── Feature: Transitions ──────────────────────────────────────────
            screen<AnimationShowcase> {
                val nav = LocalTraverseNavigator.current
                AnimationShowcaseScreen(
                    onFade = { nav.navigate(AnimPreviewFade) },
                    onHorizontalSlide = { nav.navigate(AnimPreviewHorizontalSlide) },
                    onVerticalSlide = { nav.navigate(AnimPreviewVerticalSlide) },
                    onSlideAndFade = { nav.navigate(AnimPreviewSlideAndFade) },
                    onScaleAndFade = { nav.navigate(AnimPreviewScaleAndFade) },
                    onElevate = { nav.navigate(AnimPreviewElevate) },
                    onNone = { nav.navigate(AnimPreviewNone) },
                )
            }
            screen<AnimPreviewFade>(transitionSpec = TraverseTransitionSpec.fade()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Fade",
                    apiCall = "fade()",
                    pushDescription = "New screen fades in over the current screen.",
                    popDescription = "Previous screen fades back in.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewHorizontalSlide>(transitionSpec = TraverseTransitionSpec.horizontalSlide()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Horizontal Slide",
                    apiCall = "horizontalSlide()",
                    pushDescription = "New screen slides in from the right; current exits to the left.",
                    popDescription = "Current screen slides out to the right; previous enters from the left.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewVerticalSlide>(transitionSpec = TraverseTransitionSpec.verticalSlide()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Vertical Slide",
                    apiCall = "verticalSlide()",
                    pushDescription = "New screen slides up from the bottom; current scales back slightly.",
                    popDescription = "Pop screen slides back down; previous scales up into view.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewSlideAndFade>(transitionSpec = TraverseTransitionSpec.slideAndFade()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Slide and Fade",
                    apiCall = "slideAndFade()",
                    pushDescription = "Slides in from the right 20% of width + fades in — Material Shared Axis X.",
                    popDescription = "Slides back to the right 20% of width + fades out.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewScaleAndFade>(transitionSpec = TraverseTransitionSpec.scaleAndFade()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Scale and Fade",
                    apiCall = "scaleAndFade()",
                    pushDescription = "Grows from 92% scale + fades in — zoom into destination.",
                    popDescription = "Shrinks back to 92% scale + fades out — zoom back to origin.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewElevate>(transitionSpec = TraverseTransitionSpec.elevate()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "Elevate",
                    apiCall = "elevate()",
                    pushDescription = "Rises from 94% scale with a slight upward drift + fade in.",
                    popDescription = "Drifts back down with scale-down + fade out.",
                    onBack = { nav.navigateUp() },
                )
            }
            screen<AnimPreviewNone>(transitionSpec = TraverseTransitionSpec.none()) {
                val nav = LocalTraverseNavigator.current
                AnimationPreviewScreen(
                    name = "None",
                    apiCall = "none()",
                    pushDescription = "Instant cut — no enter animation whatsoever.",
                    popDescription = "Instant cut — no pop animation whatsoever.",
                    onBack = { nav.navigateUp() },
                )
            }

            // ── Feature: Annotations + KSP ────────────────────────────────────
            screen<AnnotationsDemo>(transitionSpec = TraverseTransitionSpec.slideAndFade()) {
                val nav = LocalTraverseNavigator.current
                AnnotationsShowcaseScreen(
                    onNavigateToItemDetail = { itemId -> nav.navigate(AnnotationItemDetail(itemId)) },
                    onBrowseRegistry = { nav.navigate(ScreenRegistryDemo) },
                    onNavigateUp = { nav.navigateUp() },
                )
            }
            screen<AnnotationItemDetail>(
                transitionSpec = TraverseTransitionSpec.slideAndFade(),
                deepLinks = listOf(
                    deepLink("traverse://demo/annotations/item/{itemId}"),
                ),
            ) { dest ->
                val nav = LocalTraverseNavigator.current
                AnnotationItemDetailScreen(
                    itemId = dest.itemId,
                    onNavigateUp = { nav.navigateUp() },
                )
            }
            screen<ScreenRegistryDemo>(transitionSpec = TraverseTransitionSpec.slideAndFade()) {
                val nav = LocalTraverseNavigator.current
                ScreenRegistryDemoScreen(
                    onNavigateUp = { nav.navigateUp() },
                )
            }
        }
    }
}
