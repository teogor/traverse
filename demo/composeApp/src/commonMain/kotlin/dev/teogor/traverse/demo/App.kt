package dev.teogor.traverse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.teogor.traverse.compose.TraverseHost
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator
import dev.teogor.traverse.compose.result.CollectTraverseResultOnce
import dev.teogor.traverse.compose.transition.TraverseTransitionSpec
import dev.teogor.traverse.core.navigator.launchAsNewRoot
import dev.teogor.traverse.demo.dialog.ShowcaseDialogContent
import dev.teogor.traverse.demo.feature.ColorPickerScreen
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

@Composable
fun App() {
    MaterialTheme {
        TraverseHost(
            startDestination = Splash,
            transitions = TraverseTransitionSpec.horizontalSlide(),
        ) {

            // ── Splash ────────────────────────────────────────────────────────
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
            screen<FeatureDetail> { dest ->
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
        }
    }
}
