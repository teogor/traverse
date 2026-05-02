# Traverse — Architecture

> This document lives in `.agent/ARCHITECTURE.md`.
> It records all architectural decisions made for the Traverse library.
> Each decision includes context, options considered, and the rationale for the choice made.
> **Update this file whenever a significant design decision is made or revised.**
> Cross-reference `.agent/MEMORY.md` and `.agent/ROADMAP.md` for current implementation status.

---

## Table of Contents

1. [Foundation — `androidx.navigation3`](#1-foundation--androidxnavigation3)
2. [Destination contract](#2-destination-contract)
3. [Back-stack as source of truth](#3-back-stack-as-source-of-truth)
4. [Navigator interface design](#4-navigator-interface-design)
5. [Graph building — DSL vs annotation processing](#5-graph-building--dsl-vs-annotation-processing)
6. [Module boundaries](#6-module-boundaries)
7. [Nested navigation](#7-nested-navigation)
8. [Navigation results](#8-navigation-results)
9. [Transitions](#9-transitions)
10. [KMP platform strategy](#10-kmp-platform-strategy)

---

## 1. Foundation — `androidx.navigation3` (JetBrains KMP fork)

**Decision:** Build on the JetBrains KMP fork of navigation3 (`org.jetbrains.androidx.navigation3`), version `1.0.0-alpha05`.

**⚠️ Two nav3 implementations exist — use the JetBrains one:**
- `androidx.navigation3:*` — Google original, Android only
- `org.jetbrains.androidx.navigation3:*` — JetBrains KMP fork, supports Android + iOS + Desktop + **Web (wasmJs)** — USE THIS

**Context:**
- nav2 (`navigation-compose` 2.x, JetBrains fork at 2.9.1) is the current stable opt but is in maintenance mode.
- nav3 is fully KMP as of `1.0.0-alpha05` and supports all four platforms including wasmJs.
- nav3 rewrites the back stack as a plain `SnapshotStateList<NavKey>` — no hidden registry, no `NavController`, trivially testable.

**nav3 core concepts:**
```
NavKey              — marker interface; all destinations implement it
NavBackStack        — a SnapshotStateList<NavKey>; manipulating it IS navigation
NavDisplay          — @Composable rendering the current NavEntry
NavEntry<T>         — wraps a NavKey T with its content composable
entryProvider { }   — DSL to register entries by reified type
SavedStateConfiguration — serializer config required for non-JVM platforms
```

**Why nav3 over nav2:**
- KMP-native including Web.
- Back stack is plain Kotlin data — trivially serializable and testable.
- `SavedStateConfiguration` + kotlinx.serialization handles all argument types without `NavType`.
- Google is investing in nav3; nav2 is maintenance-only.

**Risk:** nav3 is still alpha. Mitigation: Traverse wraps it behind a stable public API — callers are insulated from nav3 internal churn.

---

## 2. Destination contract

**Decision:** `Destination` is a **plain Kotlin marker interface with zero framework dependencies**. It does NOT extend `NavKey`. Concrete destinations are `@Serializable` data classes or data objects.

```kotlin
// traverse-core — no nav3 import, no Compose import
public interface Destination

// Callers write:
@Serializable data object Home : Destination
@Serializable data class UserProfile(val userId: String) : Destination
```

**Option A was chosen (2026-05-02):** `Destination` does NOT extend `NavKey`.

**Rationale:**
- `traverse-core` must have zero Compose and zero nav3 dependency — it is the module depended on by both `traverse-compose` AND `traverse-test`. Extending `NavKey` would pull `navigation3-ui` (and transitively, Compose Runtime) into `traverse-core`, contaminating `traverse-test`.
- `traverse-compose`'s `DefaultTraverseNavigator` maps `Destination` → `NavKey` internally via a cast. Since every `Destination` is registered by the `TraverseGraphBuilder` (which controls what enters the back stack), the cast is safe and sealed inside `traverse-compose`.
- If nav3 renames or removes `NavKey`, only `traverse-compose` internals change — callers and `traverse-core` are unaffected.

**Why require `@Serializable`?**
- nav3's `rememberNavBackStack` needs explicit serializers for non-JVM platforms (iOS, Web). See `.agent/refs/navigation-reference.md` → "SavedStateConfiguration".
- `@Serializable` enables Traverse to auto-build the `SavedStateConfiguration` from DSL registrations using `inline reified` + `serializer<T>()`.
- Not enforceable at compile time without KSP, but documented clearly and validated at runtime.

**Auto-built `SavedStateConfiguration`:** `TraverseGraphBuilder.screen<T>` is `inline reified` — Traverse collects `Pair(T::class, serializer<T>())` for every registered destination and builds the `SerializersModule` + `SavedStateConfiguration` internally. Callers never write serialization config for the simple (single-module, sealed or open) case.

**Multi-module projects:** `TraverseHost` accepts an optional `serializersModule: SerializersModule?` parameter that is merged (`+`) with the auto-collected one. See `.agent/refs/navigation-reference.md` → "Three Serialization Patterns".

---

## 3. Back-stack as source of truth

**Decision:** The canonical navigation state is nav3's `NavBackStack` (a `SnapshotStateList<Destination>`), not a Shadow state maintained by Traverse.

**Why:**
- nav3 already handles save/restore, process death, and back gestures via `NavBackStack`.
- Maintaining a parallel state would create sync issues.
- `TraverseNavigator` holds a reference to the `NavBackStack` and dispatches operations against it directly.

**Implication:** `TraverseNavigator.controller` exposes the underlying `NavBackStack` for advanced use. Callers can manipulate it directly if needed, but the recommended path is through the navigator API.

---

## 4. Navigator interface design

**Decision:** `TraverseNavigator` is a public interface backed by `DefaultTraverseNavigator`. Injected via `LocalTraverseNavigator`.

```kotlin
// traverse-core
public interface TraverseNavigator {
    public val backStack: NavBackStack<Destination>
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})
    public fun navigateUp(): Boolean
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean
    public val canNavigateUp: Boolean
}
```

Back-stack extension functions live as top-level `fun TraverseNavigator.*` to keep the interface small:
```kotlin
public inline fun <reified T : Destination> TraverseNavigator.navigateAndClearUpTo(destination: T)
public inline fun <reified Root : Destination> TraverseNavigator.launchAsNewRoot(destination: Destination)
```

`LocalTraverseNavigator` is a `ProvidableCompositionLocal<TraverseNavigator>` provided by `TraverseHost`.

**Testability:** Tests use `FakeTraverseNavigator` (in `traverse-test`) which records calls and allows assertion.

---

## 5. Graph building — DSL vs annotation processing

**Decision:** Pure DSL, zero annotation processing.

```kotlin
TraverseHost(startDestination = Home) {
    screen<Home>        { HomeScreen() }
    screen<UserProfile> { dest -> UserProfileScreen(dest.userId) }
    dialog<ConfirmDialog>    { dest -> ConfirmDialogScreen(dest) }
    bottomSheet<ShareSheet>  { dest -> ShareSheetScreen(dest) }
    nested(startDestination = OnboardingStep1) {
        screen<OnboardingStep1> { … }
        screen<OnboardingStep2> { … }
    }
}
```

**`TraverseGraphBuilder` structure:**
- Annotated `@TraverseDsl` (a `@DslMarker`) to prevent scope leaks.
- `screen<T : Destination>` registers a full-screen composable.
- `dialog<T : Destination>` registers a dialog destination.
- `bottomSheet<T : Destination>` registers a bottom sheet destination.
- `nested(startDestination, builder)` creates a sub-graph scope.

**Why no KSP?**
- KSP is Android/JVM-centric; though it has KMP support, it adds toolchain complexity.
- compose-destinations' main complaint was annotation processing friction (slow builds, kapt/ksp configuration).
- nav3's `entryProvider { entry<T> { } }` already provides the type-safe registration mechanism we need.

**Type safety without codegen:**
- `inline fun <reified T : Destination> screen(...)` uses `reified` to extract `T::class` at call sites.
- nav3's `entry<T>` uses the same `reified` approach — no class literal boilerplate.

---

## 6. Module boundaries

```
traverse-core/
  └── No Compose dependency
  └── No Android dependency
  └── Exports: Destination, TraverseNavigator, NavigationResult helpers
  └── Depends on: kotlinx-serialization-core

traverse-compose/
  └── Depends on traverse-core, Compose Multiplatform, androidx.navigation3
  └── Exports: TraverseHost, TraverseGraphBuilder, LocalTraverseNavigator,
               TraverseTransitionSpec, @TraverseDsl

traverse-test/
  └── Depends on traverse-core
  └── Exports: FakeTraverseNavigator, NavigationAssertion helpers
  └── Scope: testImplementation only

demo/
  └── Depends on traverse-compose
  └── Shows all features with real screens
```

**Rule:** `traverse-core` must never import from `traverse-compose`. The dependency graph is strictly linear: `traverse-test → traverse-core`, `traverse-compose → traverse-core`.

---

## 7. Nested navigation

**Decision:** Nested graphs are represented as a `Destination` object that acts as both the graph key and the nav3 sub-back-stack scope.

```kotlin
@Serializable data object OnboardingGraph : Destination  // graph key

TraverseHost {
    nested(startDestination = OnboardingStep1, graphKey = OnboardingGraph) {
        screen<OnboardingStep1> { … }
        screen<OnboardingStep2> { … }
    }
}

// Navigating INTO the nested graph:
navigator.navigate(OnboardingGraph) // enters at OnboardingStep1

// Navigating OUT:
navigator.popTo(MainGraph, inclusive = false)
```

Internally, nested graphs map to nav3's `NavDisplay` nesting or sub-`NavBackStack` — exact implementation TBD pending nav3 stable nested graph API.

---

## 8. Navigation results

**Decision:** Use nav3's `SavedStateHandle` pattern (if available on the target entry) or a `SharedFlow`-based callback mechanism.

```kotlin
// Producer
navigator.setResultAndNavigateUp(key = "picked_color", value = "red")
navigator.setResultAndPopTo(key = "picked_color", value = "red", destination = Home)

// Consumer
CollectTraverseResultOnce<String>(key = "picked_color") { color ->
    viewModel.applyColor(color)
}
```

`CollectTraverseResultOnce` is a `@Composable` that observes the `savedStateHandle` of the current back-stack entry and calls the callback exactly once when the key is present, then clears it.

**Note on KMP:** `SavedStateHandle` is Android-specific. On iOS/Desktop, Traverse will use an internal `ResultBus` backed by `MutableSharedFlow<Pair<String, Any?>>` scoped to the back-stack entry. The same `CollectTraverseResultOnce` / `setResultAndNavigateUp` API works on all platforms — the underlying mechanism is abstracted behind `TraverseResultStore` in `traverse-core`.

---

## 9. Transitions

**Decision:** Global transitions are configured via `TraverseTransitionSpec` on `TraverseHost`. Per-destination overrides are set inside the `screen<T>` call.

```kotlin
data class TraverseTransitionSpec(
    val enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition?)? = null,
    val exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition?)? = null,
    val popEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition?)? = null,
    val popExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition?)? = null,
) {
    companion object {
        fun fade(durationMillis: Int = 300): TraverseTransitionSpec
        fun horizontalSlide(durationMillis: Int = 300): TraverseTransitionSpec
        fun none(): TraverseTransitionSpec
    }
}
```

Wired into nav3's `NavDisplay` `transitionSpec` parameter (or equivalent in the nav3 Compose layer).

---

## 10. KMP platform strategy

| Platform | Nav3 integration | Back-gesture handling |
|---|---|---|
| Android | `androidx.navigation3` composeui artifact | System back button via `BackHandler` / `PredictiveBackHandler` |
| iOS | JetBrains Compose for iOS layer | Swipe-back gesture via `UINavigationController`-compatible gesture (TBD) |
| Desktop (JVM) | Compose Desktop layer | `Escape` key / Alt+Left hardware shortcuts |

**wasmJs:** Not targeted initially. nav3's wasm support is not yet stable. Add after nav3 1.0.

---

## Open questions / TBDs

- [ ] How does nav3 handle nested back stacks on iOS? Research required.
- [ ] `SavedStateHandle` KMP equivalent — confirm `TraverseResultStore` design.
- [ ] nav3 `bottomSheet` API — confirm it exists in the stable nav3 Compose layer.
- [ ] Deep link schema — decide format (`traverse://host/path?param=value` or pure URI).
- [ ] `@TraverseDsl` DslMarker scope — confirm it prevents nesting `screen<>` inside `dialog<>`.

