# Traverse — Architecture

> This document lives in `.agent/ARCHITECTURE.md`.
> It records all architectural decisions made for the Traverse library.
> Each decision includes context, options considered, and the rationale for the choice made.
> **Update this file whenever a significant design decision is made or revised.**
> Cross-reference `.agent/MEMORY.md` and `.agent/ROADMAP.md` for current implementation status.

---

## Table of Contents

1. [Navigation Engine — Self-Contained vs nav3](#1-navigation-engine--self-contained-vs-nav3)
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

## 1. Navigation Engine — Self-Contained vs nav3

**Initial plan (session 3):** Build on `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05` (JetBrains KMP fork) as the navigation runtime.

**Revised decision (session 10, 2026-05-02):** The navigation engine is **fully self-contained**. nav3 is NOT a runtime dependency.

### Why the pivot

- nav3 is `1.0.0-alpha05`. Its API changes frequently and it carries transitive Compose dependencies that would bleed into `traverse-core`.
- The core primitives Traverse needs (`SnapshotStateList`, `AnimatedContent`, `Dialog`, `ModalBottomSheet`) are already in Compose Multiplatform stable.
- Implementing the back-stack engine ourselves gives full control over dialog/sheet rendering, transition direction detection, and nested graph key resolution — all of which nav3 does not provide out of the box.
- nav3's "user-owned back stack" philosophy (`SnapshotStateList<NavKey>`) is exactly what Traverse already uses for `List<Destination>`. We aligned architecturally without taking the dependency.

### Current implementation

```
TraverseHost
  └── TraverseAnimatedHost (internal)
        ├── SnapshotStateList<Destination>  ← THE back stack
        ├── DefaultTraverseNavigator         ← mutates the list
        ├── AnimatedContent(currentScreen)   ← screen rendering
        ├── Dialog { }                       ← dialog overlay
        └── ModalBottomSheet { }            ← sheet overlay
```

**nav3 is kept as reference only** — its API design, `SnapshotStateList` philosophy, and `SavedStateConfiguration` approach informed Traverse. `navigation3-ui` remains in `libs.versions.toml` for future reference but is NOT a Gradle dependency of any Traverse module.

**Risk mitigated:** If nav3 stabilises and offers compelling features (e.g. adaptive layout, shared element transitions), Traverse can adopt it internally without changing any public API.

---

## 2. Destination contract

**Decision:** `Destination` is a **plain Kotlin marker interface with zero framework dependencies**. It does NOT extend `NavKey` or any nav3 type. Concrete destinations are `@Serializable` data classes or data objects.

```kotlin
// traverse-core — no nav3 import, no Compose import
public interface Destination

// Callers write:
@Serializable data object Home : Destination
@Serializable data class UserProfile(val userId: String) : Destination
```

**Option A chosen (2026-05-02):** `Destination` does NOT extend `NavKey`.

**Rationale:**
- `traverse-core` must have zero Compose and zero nav3 dependency.
- It is the module depended on by both `traverse-compose` AND `traverse-test`. If it pulled in nav3, traverse-test would require Compose — defeating the purpose of a lightweight test utility.
- If nav3 changes `NavKey`, only `traverse-compose` internals change — callers are unaffected.

**Why `@Serializable` is required (documented, not enforced at compile time):**
- Required for the future saved-state / process-death restoration feature.
- Traverse's back-stack restoration will use `kotlinx.serialization` to persist and restore the `List<Destination>`.
- Not enforced via the type system (would require KSP), but documented clearly and checked at runtime in a future milestone.

**Note on `serializer<T>()` in `TraverseGraphBuilder`:**  
The `screen<T>`, `dialog<T>`, `bottomSheet<T>` registration functions are `inline reified` and originally called `serializer<T>()` eagerly. This caused `SerializationException` at runtime on desktop JVM when the Compose compiler extracted `@Composable` content lambdas into `ComposableSingletons` (where reification is not available).  
**Fix (session 12):** `EntrySpec.serializer` is nullable, defaulting to `null`. The `serializer<T>()` calls are removed from graph registration. The field is reserved for the saved-state milestone where it will be populated in a dedicated `inline reified` context.

---

## 3. Back-stack as source of truth

**Decision:** The canonical navigation state is a `SnapshotStateList<Destination>` owned by `TraverseAnimatedHost`. `DefaultTraverseNavigator` holds a reference to this list and mutates it directly.

**Why `SnapshotStateList`:**
- Compose reacts to changes automatically — no manual `StateFlow` publishing needed.
- The list is the single source of truth; `AnimatedContent`, `BackStackBar`, and any composable reading `LocalTraverseNavigator.current.backStack` all recompose when it changes.
- Trivially testable: `FakeTraverseNavigator` uses a plain `MutableList<Destination>` that behaves identically in unit tests.

`DefaultTraverseNavigator.backStack` is declared as `List<Destination>` in the interface but returns the `SnapshotStateList` directly — so Compose's snapshot observation works through the interface.

---

## 4. Navigator interface design

**Decision:** `TraverseNavigator` is a public interface backed by `DefaultTraverseNavigator`. Injected via `LocalTraverseNavigator`.

```kotlin
// traverse-core — current API
public interface TraverseNavigator {
    public val backStack: List<Destination>
    public val currentDestination: Destination   // default: backStack.last()
    public val canNavigateUp: Boolean             // default: backStack.size > 1
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})
    public fun navigateUp(): Boolean
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean
    public fun <T> setResult(key: String, value: T)
    public fun clearResult(key: String)
    public fun <T> observeResult(key: String): Flow<T>
}
```

Extension functions (top-level `fun TraverseNavigator.*`):

```kotlin
public inline fun <reified T : Destination> TraverseNavigator.navigateAndClearUpTo(destination: Destination)
public inline fun <reified Root : Destination> TraverseNavigator.launchAsNewRoot(destination: Destination)
public inline fun <reified T : Destination> TraverseNavigator.isOnBackStack(): Boolean
public inline fun <reified T : Destination> TraverseNavigator.entriesOf(): List<T>
```

`LocalTraverseNavigator` is a `ProvidableCompositionLocal<TraverseNavigator>` provided by `TraverseHost`.

**Testability:** Tests use `FakeTraverseNavigator` (in `traverse-test`) which records calls and allows assertion without any Compose or Android dependency.

---

## 5. Graph building — DSL vs annotation processing

**Decision:** Pure DSL, zero annotation processing.

```kotlin
TraverseHost(startDestination = Home) {
    screen<Home>           { HomeScreen() }
    screen<UserProfile>    { dest -> UserProfileScreen(dest.userId) }
    dialog<ConfirmDelete>  { dest -> ConfirmDeleteDialog(dest) }
    bottomSheet<TagPicker> { TagPickerSheet() }
    nested(startDestination = OnboardingStep1, graphKey = OnboardingGraph) {
        screen<OnboardingStep1> { … }
        screen<OnboardingStep2> { … }
    }
}
```

**`TraverseGraphBuilder` structure:**
- `@TraverseDsl` (`@DslMarker`) to prevent scope leaks across nested builders.
- `screen<T>`, `dialog<T>`, `bottomSheet<T>` — `inline reified` functions that capture `T::class` at compile time.
- `nested(startDestination, graphKey?, builder)` — creates a sub-scope; all entries are added to the flat registry. The `graphKey` maps to `startDestination` in `nestedGraphKeys: Map<KClass<out Destination>, Destination>`.
- `EntrySpec` — internal descriptor storing `klass`, `type` (SCREEN/DIALOG/BOTTOM_SHEET), and `content`.

**Why no KSP:**
- KSP adds toolchain complexity and slower builds.
- Kotlin `inline reified` gives type safety at registration time without code generation.
- Callers write less code with the DSL than they would with generated route files.

---

## 6. Module boundaries

```
traverse-core/       ← zero framework deps (Kotlin + coroutines + serialization only)
  └─ exports: Destination, TraverseNavigator, NavOptions, TraverseNavigatorExtensions,
              NavigationResultExtensions, TraverseDsl (@DslMarker)
  └─ depends on: kotlinx-coroutines-core, kotlinx-serialization-core

traverse-compose/    ← depends on traverse-core + Compose Multiplatform
  └─ exports: TraverseHost, TraverseGraphBuilder, LocalTraverseNavigator,
              TraverseTransitionSpec, CollectTraverseResultOnce
  └─ internal: DefaultTraverseNavigator, TraverseResultStore, EntrySpec, TraverseBackHandler

traverse-test/       ← depends on traverse-core + kotlin-test + coroutines
  └─ exports: FakeTraverseNavigator, NavigationCall, PopToCall, TraverseAssertions
  └─ intended scope: testImplementation
  └─ no Compose dependency — pure Kotlin tests

demo/                ← depends on traverse-compose
  └─ self-documenting Traverse Explorer showcase app
```

**Strict rule:** `traverse-core` must never import from `traverse-compose`. The dependency graph is strictly linear: `traverse-test → traverse-core`, `traverse-compose → traverse-core`.

---

## 7. Nested navigation

**Decision:** Nested graphs use a **flat entry registry** + a **graph-key redirect map**. There is no separate `NavGraph` or sub-`NavBackStack` object.

```kotlin
nested(startDestination = NestedStep1, graphKey = NestedFlowGraph) {
    screen<NestedStep1> { … }
    screen<NestedStep2> { … }
}
// Internally:
//   entries += [NestedStep1_spec, NestedStep2_spec]   ← added to flat list
//   nestedGraphKeys[NestedFlowGraph::class] = NestedStep1  ← redirect map
```

**Navigation into a nested graph:**
```kotlin
nav.navigate(NestedFlowGraph)
// DefaultTraverseNavigator resolves NestedFlowGraph → NestedStep1 via nestedGraphKeys
// Back stack: [Catalog, NestedStep1]
```

**Exiting a nested graph:**
```kotlin
nav.popTo(Catalog)
// Pops all steps at once
```

**Why flat registry over sub-NavDisplay nesting:**
- Simpler implementation with no requirement for nav3.
- The graph key acts purely as a convenient routing alias.
- All destinations are equal citizens in the back stack — no wrapper or scope object needed.

---

## 8. Navigation results

**Decision:** `MutableSharedFlow`-backed `TraverseResultStore` in `traverse-compose/internal`. Same API on all platforms.

```kotlin
// Producer:
navigator.setResult(RESULT_COLOR, "Red")
navigator.navigateUp()
// OR:
navigator.setResultAndNavigateUp(RESULT_COLOR, "Red")  // extension

// Consumer (composable):
CollectTraverseResultOnce<String>(RESULT_COLOR) { color -> … }
```

`CollectTraverseResultOnce` uses `LaunchedEffect` + `navigator.observeResult<T>` (`Flow`) and calls `navigator.clearResult(key)` immediately after delivery — preventing re-delivery on recomposition.

**`TraverseResultStore`** (internal, `traverse-compose`):
- `pending: MutableMap<String, Any?>` — synchronously readable after set.
- `events: MutableSharedFlow<Pair<String, Any?>>(replay = 1)` — delivers to active collectors.
- `clearResult`: removes from map; if map is empty, resets replay cache.

**Note:** `SavedStateHandle` (Android-only nav2/nav3 pattern) is deliberately NOT used. The `SharedFlow` approach works identically on all 6 platforms.

---

## 9. Transitions

**Decision:** `AnimatedContent` drives screen transitions. `TraverseTransitionSpec` is a value class wrapping four optional transition lambdas.

Per-destination overrides are supported via `screen<T>(enterTransition = …, …)` in `TraverseGraphBuilder`. These are stored in `EntrySpec` and take priority in `TraverseAnimatedHostCore`.

```kotlin
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
        val isPop = !backStack.contains(initialState)
        val targetSpec = graphBuilder.findSpec(targetState)
        val initialSpec = graphBuilder.findSpec(initialState)
        if (isPop) {
            val enter = targetSpec?.popEnterTransition?.invoke()
                ?: initialSpec?.popEnterTransition?.invoke()
                ?: transitions?.popEnterTransition?.invoke()
                ?: fadeIn()
            val exit = initialSpec?.popExitTransition?.invoke()
                ?: targetSpec?.popExitTransition?.invoke()
                ?: transitions?.popExitTransition?.invoke()
                ?: fadeOut()
            enter togetherWith exit
        } else {
            val enter = targetSpec?.enterTransition?.invoke()
                ?: transitions?.enterTransition?.invoke() ?: fadeIn()
            val exit = initialSpec?.exitTransition?.invoke()
                ?: transitions?.exitTransition?.invoke() ?: fadeOut()
            enter togetherWith exit
        }
    },
) { dest -> graphBuilder.findSpec(dest)?.content?.invoke(dest) }
```

**Priority order:** per-entry spec → host `TraverseTransitionSpec` → `fadeIn`/`fadeOut` fallback.

**Pop vs push direction detection:** `initialState ∉ backStack` after the transition starts means the outgoing screen was popped — the reverse animation should play.

**Preset factory methods** on `TraverseTransitionSpec`:
- `fade(durationMillis)` — symmetric crossfade
- `horizontalSlide(durationMillis)` — slide right-to-left on push, reverse on pop
- `none()` — instant cut with no animation
- Callers can provide custom lambdas directly

**Dialogs and bottom sheets** are rendered as Compose overlays (`Dialog {}` / `ModalBottomSheet {}`) above the `AnimatedContent` layer. They do not participate in `AnimatedContent` transitions.

---

## 10. KMP platform strategy

| Platform | Compile target | Back-gesture handling |
|---|---|---|
| Android | `androidTarget` | `BackHandler` from `androidx.activity.compose` |
| iOS (device) | `iosArm64` | no-op `actual` (CMP handles swipe-back via UIKit layer) |
| iOS (simulator) | `iosSimulatorArm64` | no-op `actual` |
| Desktop (JVM) | `jvm` | `Modifier.onPreviewKeyEvent` on root Box — `Escape` + `Alt+Left` |
| Browser (JS) | `js + browser()` | no-op `actual` — browser history API planned |
| Browser (Wasm) | `wasmJs + browser()` | no-op `actual` — browser history API planned |

All 6 targets compile. Back-gesture is handled via two independent mechanisms:

1. **`expect fun TraverseBackHandler(enabled, onBack)`** — standalone composable.
   - `androidMain` — delegates to `androidx.activity.compose.BackHandler`
   - all other platforms — no-op stubs
2. **`internal expect fun Modifier.traverseBackKeyModifier(enabled, onBack)`** — modifier extension
   applied on the root `Box` in `TraverseAnimatedHostCore`.
   - `jvmMain` — `Modifier.onPreviewKeyEvent` intercepts `Escape` / `Alt+Left` before any child
   - all other platforms — returns `this` (no-op)

The two mechanisms are complementary: `TraverseBackHandler` covers system back on Android;
`traverseBackKeyModifier` covers keyboard shortcuts on Desktop.

---

## Resolved open questions

- ✅ `Destination` interface design — Option A: plain marker, no `NavKey` extension
- ✅ `SavedStateHandle` KMP equivalent — `TraverseResultStore` backed by `MutableSharedFlow`
- ✅ `bottomSheet` API — implemented as `ModalBottomSheet` overlay (nav3 not required)
- ✅ nav3 nested back stacks — not used; flat registry + `nestedGraphKeys` map
- ✅ `@TraverseDsl` DslMarker scope — confirmed working; prevents `screen<>` inside `screen<>`
- ⏳ Deep link schema — deferred to M6
- ✅ Desktop/Web back-gesture handling — Desktop Escape/Alt+Left implemented via `traverseBackKeyModifier`; browser history deferred
- ⏳ Saved-state / process-death restoration — deferred; `EntrySpec.serializer` field reserved
