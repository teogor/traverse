package dev.teogor.traverse.demo

import dev.teogor.traverse.annotations.DeepLink
import dev.teogor.traverse.annotations.ScreenMeta
import dev.teogor.traverse.annotations.Transition
import dev.teogor.traverse.annotations.TransitionPreset
import dev.teogor.traverse.annotations.TraverseBottomSheet
import dev.teogor.traverse.annotations.TraverseDialog
import dev.teogor.traverse.annotations.TraverseRoot
import dev.teogor.traverse.annotations.TraverseScreen
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable

// ── Splash ────────────────────────────────────────────────────────────────────

/** Full-screen app entry point. Uses `launchAsNewRoot` to transition to [Catalog]. */
@TraverseScreen
@ScreenMeta(name = "Splash", group = "root")
@Serializable data object Splash : Destination

// ── Catalog (persistent root) ─────────────────────────────────────────────────

/** Feature catalog — the app's root after splash. Lists all Traverse features. */
@TraverseScreen
@TraverseRoot
@ScreenMeta(name = "Catalog", description = "Feature catalog — the persistent root of the Traverse Explorer.", group = "root")
@Serializable data object Catalog : Destination

// ── Feature: nested() graph ───────────────────────────────────────────────────

/** Graph key destination — navigating here redirects to [NestedStep1]. */
@Serializable data object NestedFlowGraph : Destination

@TraverseScreen
@ScreenMeta(name = "Nested Step 1", group = "nested")
@Transition(TransitionPreset.HORIZONTAL_SLIDE)
@Serializable data object NestedStep1 : Destination

@TraverseScreen
@ScreenMeta(name = "Nested Step 2", group = "nested")
@Transition(TransitionPreset.HORIZONTAL_SLIDE)
@Serializable data object NestedStep2 : Destination

/** Step 3 of 3 — uses `popTo(Catalog)` to exit the nested flow. */
@TraverseScreen
@ScreenMeta(name = "Nested Step 3", group = "nested")
@Transition(TransitionPreset.HORIZONTAL_SLIDE)
@Serializable data object NestedStep3 : Destination

// ── Feature: Typed Arguments ──────────────────────────────────────────────────

/**
 * Strongly-typed destination carrying a runtime argument.
 * Demonstrates `data class` destinations — no route strings needed.
 */
@TraverseScreen
@DeepLink("traverse://demo/feature/{featureId}")
@ScreenMeta(
    name = "Feature Detail",
    description = "Demonstrates typed-argument destinations and deep links.",
    group = "typedArgs",
)
@Transition(TransitionPreset.SLIDE_AND_FADE)
@Serializable data class FeatureDetail(val featureId: String) : Destination

// ── Feature: Navigation Results ───────────────────────────────────────────────

@TraverseScreen
@ScreenMeta(name = "Result Demo", group = "results")
@Serializable data object ResultDemo : Destination

@TraverseScreen
@ScreenMeta(name = "Color Picker", description = "Returns a picked hex color to the caller.", group = "results")
@Transition(TransitionPreset.VERTICAL_SLIDE)
@Serializable data object ColorPicker : Destination

// ── Feature: dialog<T> ────────────────────────────────────────────────────────

@TraverseScreen
@ScreenMeta(name = "Dialog Demo", group = "overlays")
@Serializable data object DialogDemo : Destination

/** Dialog destination — rendered as a composable `Dialog` overlay. */
@TraverseDialog(dismissOnClickOutside = false)
@ScreenMeta(name = "Showcase Dialog", description = "Confirmation dialog that returns a boolean result.", group = "overlays")
@Serializable data class ShowcaseDialog(val message: String) : Destination

// ── Feature: bottomSheet<T> ───────────────────────────────────────────────────

@TraverseScreen
@ScreenMeta(name = "Sheet Demo", group = "overlays")
@Serializable data object SheetDemo : Destination

@TraverseBottomSheet
@ScreenMeta(name = "Option Sheet", description = "ModalBottomSheet that returns a selected option.", group = "overlays")
@Serializable data object OptionSheet : Destination

// ── Feature: popTo / Stack Control ────────────────────────────────────────────

@TraverseScreen
@ScreenMeta(name = "Stack Control", group = "stack")
@Serializable data object StackControl : Destination

@TraverseScreen
@ScreenMeta(name = "Stack Level A", group = "stack")
@Serializable data object StackLevelA : Destination

@TraverseScreen
@ScreenMeta(name = "Stack Level B", group = "stack")
@Serializable data object StackLevelB : Destination

@TraverseScreen
@ScreenMeta(name = "Stack Level C", group = "stack")
@Serializable data object StackLevelC : Destination

// ── Feature: launchSingleTop ──────────────────────────────────────────────────

@TraverseScreen
@ScreenMeta(name = "Single Top Demo", description = "Prevents duplicate destinations at the top of the back stack.", group = "singleTop")
@Serializable data object SingleTopDemo : Destination

// ── Feature: Deep Links ───────────────────────────────────────────────────────

/** Entry screen for the deep link demo. */
@TraverseScreen
@ScreenMeta(name = "Deep Link Demo", group = "deepLinks")
@Serializable data object DeepLinkDemo : Destination

/**
 * A destination reached via deep link.
 *
 * Registered patterns in the demo:
 *   - `traverse://demo/target/{id}`
 *   - `https://traverse.teogor.dev/target/{id}`
 */
@TraverseScreen
@DeepLink("traverse://demo/target/{id}")
@DeepLink("https://traverse.teogor.dev/target/{id}")
@ScreenMeta(name = "Deep Link Target", description = "Destination reached via deep link URI.", group = "deepLinks")
@Serializable data class DeepLinkTarget(val id: String) : Destination

// ── Feature: Custom Transitions ───────────────────────────────────────────────

/** Animation showcase — lists all built-in transition presets. */
@TraverseScreen
@ScreenMeta(name = "Animation Showcase", description = "Gallery of all built-in transition presets.", group = "transitions")
@Serializable data object AnimationShowcase : Destination

/**
 * One destination object per animation preset so each can be registered with its own
 * `transitionSpec` and transitions are visible when navigating both forward and back.
 */
@TraverseScreen
@ScreenMeta(name = "Fade Preview", group = "transitions")
@Transition(TransitionPreset.FADE)
@Serializable data object AnimPreviewFade : Destination

@TraverseScreen
@ScreenMeta(name = "Horizontal Slide Preview", group = "transitions")
@Transition(TransitionPreset.HORIZONTAL_SLIDE)
@Serializable data object AnimPreviewHorizontalSlide : Destination

@TraverseScreen
@ScreenMeta(name = "Vertical Slide Preview", group = "transitions")
@Transition(TransitionPreset.VERTICAL_SLIDE)
@Serializable data object AnimPreviewVerticalSlide : Destination

@TraverseScreen
@ScreenMeta(name = "Slide and Fade Preview", group = "transitions")
@Transition(TransitionPreset.SLIDE_AND_FADE)
@Serializable data object AnimPreviewSlideAndFade : Destination

@TraverseScreen
@ScreenMeta(name = "Scale and Fade Preview", group = "transitions")
@Transition(TransitionPreset.SCALE_AND_FADE)
@Serializable data object AnimPreviewScaleAndFade : Destination

@TraverseScreen
@ScreenMeta(name = "Elevate Preview", group = "transitions")
@Transition(TransitionPreset.ELEVATE)
@Serializable data object AnimPreviewElevate : Destination

@TraverseScreen
@ScreenMeta(name = "None Preview", group = "transitions")
@Transition(TransitionPreset.NONE)
@Serializable data object AnimPreviewNone : Destination

// ── Feature: Annotations ─────────────────────────────────────────────────────

/** Annotations showcase — demonstrates the @Traverse* annotation system and KSP output. */
@TraverseScreen
@ScreenMeta(
    name = "Annotations Demo",
    description = "Showcases @TraverseScreen, @DeepLink, @Transition, @ScreenMeta and the KSP-generated helpers.",
    group = "annotations",
)
@Transition(TransitionPreset.SLIDE_AND_FADE)
@Serializable data object AnnotationsDemo : Destination

/** Interactive ScreenRegistry browser — filter, search, and inspect all registered destinations. */
@TraverseScreen
@ScreenMeta(
    name = "Screen Registry",
    description = "Live view of ScreenRegistry: filter by group, type, search by name — all 30 destinations visible.",
    group = "annotations",
)
@Transition(TransitionPreset.SLIDE_AND_FADE)
@Serializable data object ScreenRegistryDemo : Destination

/** A sample KSP-annotated destination used as a live example inside the Annotations Demo. */
@TraverseScreen
@DeepLink("traverse://demo/annotations/item/{itemId}")
@ScreenMeta(
    name = "Annotations Item Detail",
    description = "Reached via type-safe navigateToAnnotationItemDetail(itemId) — generated by KSP.",
    group = "annotations",
)
@Transition(TransitionPreset.SLIDE_AND_FADE)
@Serializable data class AnnotationItemDetail(val itemId: String) : Destination
