package dev.teogor.traverse.demo

import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable

// ── Splash ────────────────────────────────────────────────────────────────────

/** Full-screen app entry point. Uses `launchAsNewRoot` to transition to [Catalog]. */
@Serializable data object Splash : Destination

// ── Catalog (persistent root) ─────────────────────────────────────────────────

/** Feature catalog — the app's root after splash. Lists all Traverse features. */
@Serializable data object Catalog : Destination

// ── Feature: nested() graph ───────────────────────────────────────────────────

/** Graph key destination — navigating here redirects to [NestedStep1]. */
@Serializable data object NestedFlowGraph : Destination

@Serializable data object NestedStep1 : Destination
@Serializable data object NestedStep2 : Destination

/** Step 3 of 3 — uses `popTo(Catalog)` to exit the nested flow. */
@Serializable data object NestedStep3 : Destination

// ── Feature: Typed Arguments ──────────────────────────────────────────────────

/**
 * Strongly-typed destination carrying a runtime argument.
 * Demonstrates `data class` destinations — no route strings needed.
 */
@Serializable data class FeatureDetail(val featureId: String) : Destination

// ── Feature: Navigation Results ───────────────────────────────────────────────

@Serializable data object ResultDemo : Destination
@Serializable data object ColorPicker : Destination

// ── Feature: dialog<T> ────────────────────────────────────────────────────────

@Serializable data object DialogDemo : Destination

/** Dialog destination — rendered as a composable `Dialog` overlay. */
@Serializable data class ShowcaseDialog(val message: String) : Destination

// ── Feature: bottomSheet<T> ───────────────────────────────────────────────────

@Serializable data object SheetDemo : Destination
@Serializable data object OptionSheet : Destination

// ── Feature: popTo / Stack Control ────────────────────────────────────────────

@Serializable data object StackControl : Destination
@Serializable data object StackLevelA : Destination
@Serializable data object StackLevelB : Destination
@Serializable data object StackLevelC : Destination

// ── Feature: launchSingleTop ──────────────────────────────────────────────────

@Serializable data object SingleTopDemo : Destination
