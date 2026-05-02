# Traverse — Complete Library Plan

> **Version:** 0.1 (Planning phase)  
> **Author:** Teodor Grigor (`dev.teogor`)  
> **Date:** 2026-05-02  
> **Status:** Living document — update as decisions are made.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Design Philosophy](#3-design-philosophy)
4. [Competitive Landscape](#4-competitive-landscape)
5. [Feature Inventory](#5-feature-inventory)
6. [Module Architecture](#6-module-architecture)
7. [Public API Surface](#7-public-api-surface)
8. [Internal Components](#8-internal-components)
9. [Platform Matrix](#9-platform-matrix)
10. [Dependency Map](#10-dependency-map)
11. [Milestone Breakdown](#11-milestone-breakdown)
12. [Testing Strategy](#12-testing-strategy)
13. [Publication Strategy](#13-publication-strategy)
14. [Risk Register](#14-risk-register)
15. [Glossary](#15-glossary)

---

## 1. Executive Summary

**Traverse** is a Kotlin Multiplatform navigation library for Compose Multiplatform. It is the spiritual successor to [compose-destinations](https://github.com/raamcosta/compose-destinations), which is no longer actively maintained.

Traverse wraps [JetBrains' KMP fork of navigation3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) (`org.jetbrains.androidx.navigation3`) behind a stable, ergonomic, zero-codegen DSL API that works identically on **Android, iOS, Desktop (JVM), and Web (wasmJs)**.

The library is intentionally an abstraction layer — callers write against Traverse's stable types (`Destination`, `TraverseNavigator`, `TraverseHost`) and never touch nav3's internal types (`NavKey`, `NavDisplay`, `NavBackStack`) directly. This insulates callers from nav3's alpha churn while nav3 stabilises.

---

## 2. Problem Statement

### 2.1 The compose-destinations vacuum

`compose-destinations` solved the biggest pain point in Jetpack Navigation Compose: type-safe, boilerplate-free destination management. It was the de-facto standard. It is now unmaintained and Android-only.

### 2.2 nav3 is the right foundation but has rough edges

`androidx.navigation3` (Google) / `org.jetbrains.androidx.navigation3` (JetBrains KMP):
- Back stack is a plain `SnapshotStateList<NavKey>` — clean and testable.
- Requires manual `SavedStateConfiguration` boilerplate for iOS/Web state saving.
- Has no DSL for dialogs, bottom sheets, nested graphs.
- Has no built-in navigator interface — callers manipulate the list directly.
- Has no typed result-passing mechanism on non-Android platforms.
- Has no transition presets.

### 2.3 What Traverse solves

| Pain point | Traverse solution |
|---|---|
| No KMP navigation library with full feature parity | First-class KMP: Android + iOS + Desktop + Web |
| compose-destinations requires KSP | Zero codegen — pure `@Serializable` DSL |
| nav3 `SavedStateConfiguration` boilerplate | Auto-built from DSL registrations |
| nav3 has no typed result passing on iOS/Web | `TraverseResultStore` backed by `MutableSharedFlow` |
| nav3 has no dialog/bottom-sheet support | `dialog<T>` / `bottomSheet<T>` DSL entries |
| nav3 has no navigator abstraction (testable) | `TraverseNavigator` interface + `FakeTraverseNavigator` |
| nav3 transitions are verbose | `TraverseTransitionSpec.fade()` / `.horizontalSlide()` |
| No deep-link system for KMP | `deepLink<T>(pattern)` in DSL, platform handlers |

---

## 3. Design Philosophy

### 3.1 Zero boilerplate for the common case

The simplest possible Traverse app:

```kotlin
@Serializable data object Home : Destination
@Serializable data class Profile(val userId: String) : Destination

@Composable
fun App() {
    TraverseHost(startDestination = Home) {
        screen<Home> { HomeScreen() }
        screen<Profile> { dest -> ProfileScreen(dest.userId) }
    }
}
```

Nothing generated, nothing registered, nothing configured. It just works.

### 3.2 Stable public API over unstable foundation

nav3 is `1.0.0-alpha05`. It will change. Callers should never need to update their code because nav3 changed. All nav3 types are `internal` — only Traverse's types are public.

### 3.3 Progressive disclosure

- Happy path: `TraverseHost { screen<T> { } }` — 5 lines.
- Medium: Add transitions, dialogs, nested graphs — 20 lines.
- Advanced: Multi-module serialization, custom navigator, deep links — opt-in complexity.

### 3.4 Kotlin first, no annotation processing

No KSP. No KAPT. No code generation. Kotlin's `inline reified`, `@Serializable`, and `sealed interface` give us everything codegen used to provide, without the toolchain tax.

### 3.5 KMP by default, not by afterthought

Every API decision is made with all four platforms in mind. Platform-specific behaviour (Android back button, iOS swipe-back, Desktop keyboard shortcuts) is handled via platform-specific `expect/actual` or by wrapping existing Compose Multiplatform utilities.

---

## 4. Competitive Landscape

| Library | KMP | Codegen | nav3 | Maintained |
|---|---|---|---|---|
| **Traverse** | ✅ Android+iOS+Desktop+Web | ❌ None | ✅ JetBrains fork | ✅ |
| compose-destinations 2.x | ❌ Android only | ✅ KSP | ❌ nav2 | ❌ Abandoned |
| Voyager | ✅ | ❌ None | ❌ Own stack | ⚠️ Low activity |
| Decompose | ✅ | ❌ None | ❌ Own tree | ✅ |
| Appyx | ⚠️ Partial | ❌ None | ❌ Own model | ⚠️ |
| navigation-compose (JetBrains) | ✅ | ❌ | ❌ nav2 fork | ✅ Maintenance |

**Traverse fills the gap:** KMP + zero codegen + nav3 + maintained.

---

## 5. Feature Inventory

Every feature listed here has a `[Milestone N]` tag showing when it ships. Features without a milestone are post-1.0 backlog.

### 5.1 Core Navigation

| Feature | Description | Milestone |
|---|---|---|
| Forward navigation | Push a destination to back stack | M2 |
| Back navigation | Pop the top entry from back stack | M2 |
| Navigate up | Pop with `canNavigateUp` guard | M2 |
| Pop-to destination (exclusive) | Remove all entries above `T` | M2 |
| Pop-to destination (inclusive) | Remove `T` and everything above it | M2 |
| Single-top navigation | Don't duplicate if already top | M2 |
| Navigate and clear up to | `popTo<T>` then push new destination | M2 |
| Launch as new root | Clear entire back stack, push new root | M2 |
| `canNavigateUp` property | `true` when back stack has > 1 entry | M2 |
| Back stack read access | `navigator.backStack` as read-only `List<Destination>` | M2 |
| Current destination | `navigator.currentDestination` derived from `backStack.last()` | M2 |

### 5.2 Destination Types

| Feature | Description | Milestone |
|---|---|---|
| Screen destination | Full-screen Composable entry | M3 |
| Dialog destination | Destination rendered inside `Dialog { }` | M3 |
| Bottom sheet destination | Destination rendered inside `ModalBottomSheet` | M3 |
| Nested graph | Sub-scope with its own `startDestination` | M3 |
| Graph key destination | `@Serializable data object FooGraph : Destination` as graph identifier | M3 |

### 5.3 Graph Building DSL

| Feature | Description | Milestone |
|---|---|---|
| `screen<T>` | Register a full-screen destination | M3 |
| `dialog<T>` | Register a dialog destination | M3 |
| `bottomSheet<T>` | Register a bottom sheet destination | M3 |
| `nested(startDest, graphKey?, builder)` | Register a nested sub-graph | M3 |
| Per-screen enter transition override | `screen<T>(enterTransition = { slideInHorizontally() })` | M3 |
| Per-screen exit transition override | `screen<T>(exitTransition = { slideOutHorizontally() })` | M3 |
| Per-screen pop-enter transition override | `screen<T>(popEnterTransition = { ... })` | M3 |
| Per-screen pop-exit transition override | `screen<T>(popExitTransition = { ... })` | M3 |
| Deep link registration per screen | `screen<T>(deepLinks = listOf(...))` | M6 |

### 5.4 Transitions

| Feature | Description | Milestone |
|---|---|---|
| Global transition spec | `TraverseHost(transitions = TraverseTransitionSpec.fade())` | M3 |
| Fade preset | `TraverseTransitionSpec.fade(durationMillis)` | M3 |
| HorizontalSlide preset | `TraverseTransitionSpec.horizontalSlide(durationMillis)` | M3 |
| None preset | `TraverseTransitionSpec.none()` — instant cut | M3 |
| Custom transition | Provide your own `AnimatedContentTransitionScope` lambdas | M3 |
| Predictive back animation (Android 14+) | `PredictiveBackHandler` integration | M5 |
| Shared element transitions | Requires nav3 API — post-1.0 | Backlog |

### 5.5 Navigation Results

| Feature | Description | Milestone |
|---|---|---|
| `setResultAndNavigateUp(key, value)` | Producer — set result then pop | M2 |
| `setResultAndPopTo(key, value, dest, inclusive)` | Producer — set result then pop-to | M2 |
| `CollectTraverseResultOnce<T>(key, onResult)` | Consumer composable — fires once, then clears | M2 |
| Android: `SavedStateHandle`-backed store | Use nav3 ViewModel layer on Android | M3 |
| iOS/Desktop/Web: `SharedFlow`-backed store | `TraverseResultStore` backed by `MutableSharedFlow` | M3 |
| Typed result keys (v2) | `val MyKey = traverseResultKey<String>("my_key")` | Backlog |

### 5.6 Serialization & State Saving

| Feature | Description | Milestone |
|---|---|---|
| Auto-build `SavedStateConfiguration` | Collect `T::class + serializer<T>()` from DSL registrations | M3 |
| Multi-module serialization | `TraverseHost(serializersModule = combinedModule)` | M3 |
| Explicit serializers module param | Optional `serializersModule: SerializersModule?` on `TraverseHost` | M3 |
| Sealed interface support | `subclassesOfSealed<T>()` pattern | M3 |

### 5.7 ViewModel Integration

| Feature | Description | Milestone |
|---|---|---|
| ViewModel scoped to nav entry | `viewModel()` inside `screen<T>` content (via `lifecycle-viewmodel-navigation3`) | M3 |
| ViewModel scoped to nested graph | VM survives within graph, cleared when graph popped | M3 |
| Custom `ViewModelStoreOwner` | Pass per-entry metadata to nav3 | M4 |

### 5.8 Back Gesture Handling

| Feature | Description | Milestone |
|---|---|---|
| Android system back button | `BackHandler` / `PredictiveBackHandler` integration in `TraverseHost` | M3 |
| iOS swipe-back gesture | Compose Multiplatform handles via UIKitView layer — verify | M3 |
| Desktop keyboard back shortcut | `Escape` / `Alt+Left` event listener in `TraverseHost` | M3 |
| Web browser back button | `navigation3-browser` PoC integration | M6 |

### 5.9 Deep Links

| Feature | Description | Milestone |
|---|---|---|
| DSL registration | `deepLink<T>("https://example.com/path/{param}")` | M6 |
| URI pattern matching | `{paramName}` extracts String fields from destination data class | M6 |
| Android: Intent → destination | `TraverseDeepLinkHandler` reads Intent URI, matches pattern, pushes destination | M6 |
| iOS: URL scheme handler | `TraverseDeepLinkHandler.handleUrl(url)` called from `AppDelegate` | M6 |
| Desktop: CLI argument / protocol | `TraverseDeepLinkHandler.handleUri(uri)` at app startup | M6 |
| Web: query-string navigation | Read initial URL in wasmJs, push matched destination | M6 |
| `navigateToDeepLink(uri)` programmatic | Navigate to any deep link URI from code | M6 |

### 5.10 Multiple Back Stacks (Tab Navigation)

| Feature | Description | Milestone |
|---|---|---|
| `rememberTraverseNavigator()` | Create an independent navigator for a tab | M7 |
| Tab state preservation | Each tab's back stack preserved when switching | M7 |
| `currentDestination` in `NavigationBar` | `navigator.backStack.last() is HomeDestination` | M7 |
| `launchSingleTop` for tab roots | Prevent stacking root tab destinations | M7 |

### 5.11 Testing Support

| Feature | Description | Milestone |
|---|---|---|
| `FakeTraverseNavigator` | Records all navigate/navigateUp/popTo calls | M4 |
| `assertNavigatedTo<T>(args?)` | Assert a specific `navigate<T>` call was made | M4 |
| `assertNavigatedUp()` | Assert `navigateUp()` was called | M4 |
| `assertPoppedTo<T>(inclusive)` | Assert `popTo<T>` was called with correct params | M4 |
| `assertResultSet(key, value)` | Assert result was set with correct key+value | M4 |
| `assertNoNavigation()` | Assert no navigation occurred | M4 |
| `TraverseTestRule` (Compose) | Compose test rule wrapping `TraverseHost` | M4 |
| `currentDestination` observable | `navigator.currentDestination` for assertion in UI tests | M4 |

### 5.12 Composition Locals & Hooks

| Feature | Description | Milestone |
|---|---|---|
| `LocalTraverseNavigator` | `CompositionLocal<TraverseNavigator>` provided by `TraverseHost` | M3 |
| `LocalTraverseBackStack` | `CompositionLocal<List<Destination>>` (derived) | M3 |
| `rememberTraverseNavigator()` | Hook to access navigator from any composable | M3 |

### 5.13 Analytics / Interceptors (post-1.0)

| Feature | Description | Milestone |
|---|---|---|
| Navigation interceptor | `TraverseNavigationInterceptor` — intercept all nav events | Backlog |
| Analytics plugin | Pre-build integration with Firebase Analytics / custom | Backlog |
| Navigation event logging | Debug-mode event log | Backlog |

---

## 6. Module Architecture

```
traverse/
├── traverse-core/              KMP — zero Compose dependency
│   ├── Destination.kt          Public interface Destination : NavKey
│   ├── navigator/
│   │   ├── TraverseNavigator.kt        Public interface
│   │   ├── TraverseNavigatorExtensions.kt  Extension functions
│   │   └── NavOptions.kt               NavOptions data class for navigate()
│   ├── result/
│   │   ├── TraverseResultStore.kt      expect/actual — SharedFlow on all platforms
│   │   ├── NavigationResultExtensions.kt  setResultAndNavigateUp, setResultAndPopTo
│   │   └── CollectTraverseResultOnce.kt    @Composable consumer (NOTE: in compose module)
│   └── dsl/
│       └── TraverseDsl.kt              @DslMarker annotation
│
├── traverse-compose/           KMP — depends on traverse-core + Compose + nav3
│   ├── TraverseHost.kt         @Composable entry point
│   ├── graph/
│   │   ├── TraverseGraphBuilder.kt     DSL class (@TraverseDsl)
│   │   ├── ScreenEntry.kt              Internal — wraps screen content
│   │   ├── DialogEntry.kt              Internal — wraps dialog content
│   │   └── BottomSheetEntry.kt         Internal — wraps bottom sheet content
│   ├── navigator/
│   │   ├── DefaultTraverseNavigator.kt Internal — NavBackStack adapter
│   │   └── LocalTraverseNavigator.kt   CompositionLocal<TraverseNavigator>
│   ├── transition/
│   │   ├── TraverseTransitionSpec.kt   Public data class + presets
│   │   └── TraverseNavTransitionSpec.kt Internal — NavTransitionSpec adapter
│   ├── result/
│   │   └── CollectTraverseResultOnce.kt @Composable — moved here from core
│   ├── deeplink/                       Added in M6
│   │   ├── TraverseDeepLink.kt         deepLink() DSL function
│   │   └── TraverseDeepLinkMatcher.kt  URI pattern matcher
│   └── backgesture/
│       ├── TraverseBackHandler.kt      expect/actual back-gesture handling
│       ├── androidMain: AndroidBackHandler.kt
│       ├── iosMain: IosBackHandler.kt
│       ├── jvmMain: DesktopBackHandler.kt
│       └── wasmJsMain: WebBackHandler.kt
│
├── traverse-test/              KMP — testImplementation only
│   ├── FakeTraverseNavigator.kt
│   ├── NavigationAssertions.kt
│   └── TraverseTestRule.kt     (Compose UI testing)
│
├── demo/
│   └── composeApp/             Runnable Compose MP demo app
│       └── src/
│           └── commonMain/
│               ├── App.kt      TraverseHost wiring
│               ├── screen/     Feature screens (Home, Profile, Settings, etc.)
│               ├── dialog/     Dialog destination screens
│               └── onboarding/ Nested graph demo
│
└── build-logic/
    ├── TraverseKmpLibraryPlugin.kt
    ├── TraverseComposePlugin.kt
    └── TraverseKmpApplicationPlugin.kt
```

### 6.1 Dependency Rules (strict, enforced by module structure)

```
traverse-test  ──────────────────────▶  traverse-core
traverse-compose  ──────────────────▶  traverse-core
demo/composeApp  ───────────────────▶  traverse-compose
```

- `traverse-core` imports: `kotlinx-serialization-core`, `org.jetbrains.androidx.navigation3:navigation3-ui` (for `NavKey` type only, no Compose)
- `traverse-compose` imports: `traverse-core`, Compose Multiplatform, `navigation3-ui`, optional `lifecycle-viewmodel-navigation3`
- `traverse-test` imports: `traverse-core`, `kotlin-test`
- **Never:** `traverse-core` imports anything from `traverse-compose`

---

## 7. Public API Surface

> Every item here has `public` modifier (enforced by `explicitApi()`). Items marked `internal` are implementation details.

### 7.1 `traverse-core` — Public types

#### `Destination` (interface)

```kotlin
package dev.teogor.traverse.core

/**
 * Marker interface for all navigation destinations in Traverse.
 *
 * All implementations must also be `@Serializable` for state saving on non-JVM platforms.
 *
 * Example:
 * ```kotlin
 * @Serializable data object Home : Destination
 * @Serializable data class Profile(val userId: String) : Destination
 * ```
 */
public interface Destination : NavKey
```

#### `TraverseNavigator` (interface)

```kotlin
package dev.teogor.traverse.core.navigator

public interface TraverseNavigator {

    /** The current back stack. The last element is the active destination. */
    public val backStack: List<Destination>

    /** The currently active destination (last entry on the back stack). */
    public val currentDestination: Destination

    /** Whether navigateUp() has any effect (back stack has more than one entry). */
    public val canNavigateUp: Boolean

    /**
     * Navigate to [destination], optionally configuring single-top behavior and
     * restore-state via [builder].
     */
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})

    /**
     * Pop the top entry from the back stack.
     * @return true if navigation occurred, false if the back stack had only one entry.
     */
    public fun navigateUp(): Boolean

    /**
     * Pop all entries above [destination].
     * @param inclusive if true, [destination] itself is also removed.
     * @return true if [destination] was found on the stack and entries were removed.
     */
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean
}
```

#### `TraverseNavigatorExtensions` (top-level extension functions)

```kotlin
package dev.teogor.traverse.core.navigator

/**
 * Pop all entries up to and including any instance of [T], then navigate to [destination].
 * Useful for "go home and then navigate to X" flows.
 */
public inline fun <reified T : Destination> TraverseNavigator.navigateAndClearUpTo(
    destination: Destination,
)

/**
 * Clear the entire back stack and start fresh with [destination] as the only entry.
 * @param Root the type used as the conceptual root — used only for documentation/intent signalling.
 */
public inline fun <reified Root : Destination> TraverseNavigator.launchAsNewRoot(
    destination: Destination,
)

/**
 * Whether [destination] (by type) is currently anywhere on the back stack.
 */
public inline fun <reified T : Destination> TraverseNavigator.isOnBackStack(): Boolean

/**
 * Returns all entries on the back stack that match type [T].
 */
public inline fun <reified T : Destination> TraverseNavigator.entriesOf(): List<T>
```

#### `NavOptions` (data class)

```kotlin
package dev.teogor.traverse.core.navigator

public data class NavOptions(
    /**
     * If true and [destination] is already the top of the back stack, the navigation
     * is a no-op (destination is not pushed twice).
     */
    public var launchSingleTop: Boolean = false,

    /**
     * If set, pop all entries up to and including this destination before navigating.
     * Combined with [popUpToInclusive].
     */
    public var popUpTo: Destination? = null,

    /** Whether [popUpTo] destination itself is removed. Only meaningful when [popUpTo] is set. */
    public var popUpToInclusive: Boolean = false,

    /**
     * If true and the destination being navigated to previously had saved state via
     * [popUpTo], that state is restored.
     */
    public var restoreState: Boolean = false,
)
```

#### `TraverseResultStore` (expect/actual)

```kotlin
package dev.teogor.traverse.core.result

/**
 * Platform-abstracted result passing mechanism.
 *
 * - Android: backed by nav3 ViewModel layer's SavedStateHandle.
 * - iOS / Desktop / Web: backed by MutableSharedFlow<Pair<String, Any?>>.
 *
 * Internal — access via extension functions setResultAndNavigateUp / setResultAndPopTo.
 */
internal expect class TraverseResultStore {
    fun <T> setResult(key: String, value: T)
    fun <T> getResult(key: String): T?
    fun clearResult(key: String)
    fun <T> observeResult(key: String): Flow<T>
}
```

#### `NavigationResultExtensions` (top-level)

```kotlin
package dev.teogor.traverse.core.result

/**
 * Set a result with [key] and [value], then navigate up one level.
 */
public fun <T> TraverseNavigator.setResultAndNavigateUp(key: String, value: T)

/**
 * Set a result with [key] and [value], then pop all entries up to [destination].
 */
public fun <T> TraverseNavigator.setResultAndPopTo(
    key: String,
    value: T,
    destination: Destination,
    inclusive: Boolean = false,
)
```

#### `TraverseDsl` (DslMarker)

```kotlin
package dev.teogor.traverse.core.dsl

@DslMarker
public annotation class TraverseDsl
```

---

### 7.2 `traverse-compose` — Public types

#### `TraverseHost` (top-level composable)

```kotlin
package dev.teogor.traverse.compose

/**
 * The root composable for Traverse navigation. Renders the currently active destination
 * from the back stack and provides [TraverseNavigator] via [LocalTraverseNavigator].
 *
 * @param startDestination The first destination shown when the [TraverseHost] is composed.
 * @param modifier Modifier applied to the outer layout.
 * @param navigator Optional external navigator — use to inject a [FakeTraverseNavigator] in tests,
 *   or a custom implementation. When null, a [DefaultTraverseNavigator] is created internally.
 * @param transitions Global transition spec applied to all screen destinations.
 *   Individual screens can override via per-screen params in [TraverseGraphBuilder.screen].
 * @param serializersModule Optional additional [SerializersModule] for multi-module projects.
 *   Merged with the module auto-built from DSL registrations.
 * @param builder DSL lambda for registering all destinations.
 */
@Composable
public fun TraverseHost(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navigator: TraverseNavigator? = null,
    transitions: TraverseTransitionSpec? = null,
    serializersModule: SerializersModule? = null,
    builder: TraverseGraphBuilder.() -> Unit,
)
```

#### `TraverseGraphBuilder` (DSL class)

```kotlin
package dev.teogor.traverse.compose.graph

@TraverseDsl
public class TraverseGraphBuilder {

    /**
     * Register a full-screen destination of type [T].
     *
     * @param enterTransition Override for enter transition. `null` inherits global spec.
     * @param exitTransition Override for exit transition. `null` inherits global spec.
     * @param popEnterTransition Override for pop-enter transition. `null` inherits global spec.
     * @param popExitTransition Override for pop-exit transition. `null` inherits global spec.
     * @param deepLinks Deep links associated with this destination (see Milestone 6).
     * @param content The composable content. Receives [dest] as the strongly-typed instance.
     */
    public inline fun <reified T : Destination> screen(
        enterTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> EnterTransition?)? = null,
        exitTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> ExitTransition?)? = null,
        popEnterTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> EnterTransition?)? = null,
        popExitTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> ExitTransition?)? = null,
        deepLinks: List<TraverseDeepLink> = emptyList(),
        content: @Composable (dest: T) -> Unit,
    )

    /**
     * Register a dialog destination of type [T].
     * Rendered inside a Compose [Dialog].
     *
     * @param properties [DialogProperties] controlling dialog behaviour.
     * @param content The composable content inside the dialog window.
     */
    public inline fun <reified T : Destination> dialog(
        properties: DialogProperties = DialogProperties(),
        content: @Composable (dest: T) -> Unit,
    )

    /**
     * Register a bottom sheet destination of type [T].
     * Rendered inside a [ModalBottomSheet].
     *
     * Note: nav3 does not have native bottom sheet entry support as of alpha05.
     * This wraps back-stack manipulation + ModalBottomSheet visibility state.
     *
     * @param content The composable content inside the bottom sheet.
     */
    public inline fun <reified T : Destination> bottomSheet(
        content: @Composable (dest: T) -> Unit,
    )

    /**
     * Register a nested navigation sub-graph.
     *
     * Navigating to [graphKey] enters the graph at [startDestination].
     * Popping past [startDestination] pops back to the parent graph.
     *
     * @param startDestination First destination shown when entering the nested graph.
     * @param graphKey Optional destination used to navigate INTO this graph from outside.
     *   If null, the graph can only be entered by directly navigating to [startDestination].
     * @param builder DSL lambda for registering nested graph destinations.
     */
    public fun nested(
        startDestination: Destination,
        graphKey: Destination? = null,
        builder: TraverseGraphBuilder.() -> Unit,
    )
}
```

#### `TraverseTransitionSpec` (public data class)

```kotlin
package dev.teogor.traverse.compose.transition

/**
 * Configures animated transitions between navigation destinations.
 *
 * All four lambdas default to `null`, meaning the nav3 default transition is used.
 *
 * Use the companion presets for common cases:
 * - [TraverseTransitionSpec.fade]
 * - [TraverseTransitionSpec.horizontalSlide]
 * - [TraverseTransitionSpec.none]
 */
public data class TraverseTransitionSpec(
    val enterTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> EnterTransition?)? = null,
    val exitTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> ExitTransition?)? = null,
    val popEnterTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> EnterTransition?)? = null,
    val popExitTransition: (AnimatedContentTransitionScope<NavEntry<*>>.() -> ExitTransition?)? = null,
) {
    public companion object {
        /** Cross-fade transition (default duration: 300ms). */
        public fun fade(durationMillis: Int = 300): TraverseTransitionSpec

        /** Horizontal slide — push from right, pop from left (default: 300ms). */
        public fun horizontalSlide(durationMillis: Int = 300): TraverseTransitionSpec

        /** No animation — instant cut between destinations. */
        public fun none(): TraverseTransitionSpec
    }
}
```

#### `LocalTraverseNavigator` (CompositionLocal)

```kotlin
package dev.teogor.traverse.compose.navigator

/**
 * Provides the [TraverseNavigator] for the current [TraverseHost] scope.
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val navigator = LocalTraverseNavigator.current
 *     Button(onClick = { navigator.navigate(Profile(userId = "42")) }) { ... }
 * }
 * ```
 *
 * Throws [IllegalStateException] if accessed outside a [TraverseHost].
 */
public val LocalTraverseNavigator: ProvidableCompositionLocal<TraverseNavigator>
```

#### `CollectTraverseResultOnce` (composable)

```kotlin
package dev.teogor.traverse.compose.result

/**
 * Collects a navigation result exactly once.
 *
 * This composable observes the result store for the current back-stack entry.
 * When a result with [key] arrives, [onResult] is called ONCE and the result is cleared
 * to prevent re-triggering on recomposition.
 *
 * Producer side: [TraverseNavigator.setResultAndNavigateUp] or [setResultAndPopTo].
 *
 * @param T The expected type of the result value.
 * @param key The result key — must match the key used on the producer side.
 * @param onResult Called exactly once with the result value.
 */
@Composable
public inline fun <reified T> CollectTraverseResultOnce(
    key: String,
    crossinline onResult: (T) -> Unit,
)
```

#### `TraverseDeepLink` (M6)

```kotlin
package dev.teogor.traverse.compose.deeplink

/**
 * Represents a deep link URI pattern for a navigation destination.
 *
 * Usage in DSL:
 * ```kotlin
 * screen<UserProfile>(
 *     deepLinks = listOf(deepLink("https://example.com/user/{userId}"))
 * ) { dest -> UserProfileScreen(dest.userId) }
 * ```
 */
public data class TraverseDeepLink(
    val uriPattern: String,
    val action: String? = null,
    val mimeType: String? = null,
)

/** Creates a [TraverseDeepLink] for a URI pattern. */
public fun deepLink(
    uriPattern: String,
    action: String? = null,
    mimeType: String? = null,
): TraverseDeepLink
```

---

### 7.3 `traverse-test` — Public types

#### `FakeTraverseNavigator`

```kotlin
package dev.teogor.traverse.test

/**
 * A test double for [TraverseNavigator].
 *
 * Records all navigation calls made to it. Use with
 * assertion extensions to verify navigation behaviour in unit tests.
 *
 * Usage:
 * ```kotlin
 * val navigator = FakeTraverseNavigator(startDestination = Home)
 * // ... pass to composable under test ...
 * navigator.assertNavigatedTo<Profile>()
 * ```
 */
public class FakeTraverseNavigator(
    startDestination: Destination,
) : TraverseNavigator {

    /** All recorded navigation events in order. */
    public val navigationHistory: List<NavigationEvent>

    // NavigationEvent is a sealed class — see below
}

public sealed class NavigationEvent {
    public data class NavigateTo(val destination: Destination, val options: NavOptions) : NavigationEvent()
    public data object NavigateUp : NavigationEvent()
    public data class PopTo(val destination: Destination, val inclusive: Boolean) : NavigationEvent()
    public data class SetResult(val key: String, val value: Any?) : NavigationEvent()
}
```

#### Navigation assertion extensions

```kotlin
package dev.teogor.traverse.test

/** Assert that navigator.navigate<T> was called with a destination of type T. */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertNavigatedTo(
    args: T? = null,
)

/** Assert that navigateUp() was called at least once. */
public fun FakeTraverseNavigator.assertNavigatedUp()

/** Assert that popTo<T>(inclusive) was called. */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertPoppedTo(
    inclusive: Boolean = false,
)

/** Assert that setResult was called with matching key and value. */
public fun <T> FakeTraverseNavigator.assertResultSet(key: String, value: T)

/** Assert that no navigation event was recorded. */
public fun FakeTraverseNavigator.assertNoNavigation()
```

---

## 8. Internal Components

These are `internal` and not part of the public API. They are described here for implementation planning only.

### 8.1 `DefaultTraverseNavigator`

```kotlin
internal class DefaultTraverseNavigator(
    private val navBackStack: NavBackStack<Destination>,
) : TraverseNavigator {
    // navigate() adds to navBackStack with single-top / popUpTo logic
    // navigateUp() calls navBackStack.removeLastOrNull()
    // popTo() filters navBackStack to remove entries above target
}
```

### 8.2 `TraverseBackStackManager`

Internal object that owns the `NavBackStack<Destination>` created by `rememberNavBackStack(savedStateConfig, startDestination)`. Responsible for:
- Creating the initial `SavedStateConfiguration` from all registered DSL entries
- Providing the `NavBackStack` to `DefaultTraverseNavigator`
- Providing the entry provider to `NavDisplay`

### 8.3 `TraverseDslProcessor`

Internal class created inside `TraverseHost`. Collects all `screen<T>`, `dialog<T>`, `bottomSheet<T>`, `nested(...)` registrations from the `TraverseGraphBuilder` lambda, and outputs:
- `entryProvider` for `NavDisplay`
- `savedStateConfig` built from all `T::class + serializer<T>()` pairs
- `deepLinkRegistry` for URI matching (M6)

### 8.4 `TraverseNavTransitionSpec`

Internal adapter that wraps `TraverseTransitionSpec` values into nav3's `NavTransitionSpec` interface:
```kotlin
internal class TraverseNavTransitionSpec(
    private val spec: TraverseTransitionSpec,
) : NavTransitionSpec { ... }
```

### 8.5 `TraverseResultStoreHolder`

Internal singleton (or `CompositionLocal`) that maps `NavKey` (back-stack entry key) → `TraverseResultStore`. Enables result passing between arbitrary destinations, not just parent→child.

### 8.6 Platform back-gesture handlers (`expect/actual`)

```
commonMain: expect fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit)
androidMain: actual using BackHandler / PredictiveBackHandler
iosMain:     actual — no-op (iOS handles swipe natively via UINavigationController layer)
jvmMain:     actual — KeyEvent filter for Escape / Alt+Left
wasmJsMain:  actual — browser popstate event listener
```

### 8.7 `DialogEntryRenderer` / `BottomSheetEntryRenderer`

Internal composables invoked when the active nav entry is a dialog or bottom sheet destination. They:
- Wrap the registered content in `Dialog { }` / `ModalBottomSheet { }`
- Dismiss by calling `navigator.navigateUp()` on `onDismissRequest`

---

## 9. Platform Matrix

| Feature | Android | iOS | Desktop (JVM) | wasmJs |
|---|---|---|---|---|
| Forward navigation | ✅ | ✅ | ✅ | ✅ |
| Back navigation | ✅ | ✅ | ✅ | ✅ |
| System back gesture | ✅ `BackHandler` | ✅ native swipe | ✅ Esc key | ✅ browser back |
| Predictive back animation | ✅ (API 34+) | ❌ N/A | ❌ N/A | ❌ N/A |
| State saving (process death) | ✅ `rememberNavBackStack` | ✅ | ✅ | ✅ |
| Navigation results | ✅ `SharedFlow` | ✅ `SharedFlow` | ✅ `SharedFlow` | ✅ `SharedFlow` |
| Dialog destinations | ✅ | ✅ | ✅ | ✅ |
| Bottom sheet destinations | ✅ | ✅ | ✅ | ✅ |
| ViewModel scoping | ✅ via nav3-viewmodel | ⚠️ verify | ✅ | ⚠️ verify |
| Deep links | ✅ `IntentFilter` | ✅ URL scheme | ✅ CLI / protocol | ✅ query string |
| Browser history | N/A | N/A | N/A | ✅ `navigation3-browser` |
| Multiple back stacks | ✅ | ✅ | ✅ | ✅ |

---

## 10. Dependency Map

### 10.1 Production dependencies

```toml
# traverse-core
kotlinx-serialization-core = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1"
jetbrains-navigation3-ui   = "org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05"

# traverse-compose (adds to above)
compose-runtime  (transitive via Compose MP)
compose-ui       (transitive via Compose MP)
compose-animation
compose-material3  (for ModalBottomSheet, Dialog wrapping)

# Optional — ViewModel scoping per entry
jetbrains-lifecycle-viewmodel-navigation3
  = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0-alpha05"

# Optional — Web browser history (M6, Web only)
nav3-browser = "com.github.terrakok:navigation3-browser:0.2.0"
```

### 10.2 Test dependencies

```toml
kotlin-test          = "org.jetbrains.kotlin:kotlin-test"
compose-test-junit4  (Android UI tests)
compose-test-desktop (JVM UI tests)
```

### 10.3 Build-logic dependencies (classpath only)

```toml
android-gradle-plugin
jetbrains-kotlin-gradle-plugin
jetbrains-kotlin-compose-gradle-plugin
jetbrains-compose-gradle-plugin
```

---

## 11. Milestone Breakdown

> Each milestone must build green (`./gradlew build`) before being closed.

---

### M0 — Scaffold ✅ DONE

*Goal: Fully documented repo with build system and zero source files.*

All items complete. See `.agent/ROADMAP.md`.

---

### M1 — Gradle Skeleton ✅ DONE

*Goal: All modules compile an empty commonMain.*

All items complete. See `.agent/ROADMAP.md`.

---

### M2 — `traverse-core` Source Files

*Goal: The entire `traverse-core` API compiles and is unit-testable.*

**Files to create:**

| File | Package | Contents |
|---|---|---|
| `Destination.kt` | `dev.teogor.traverse.core` | `interface Destination : NavKey` |
| `TraverseNavigator.kt` | `dev.teogor.traverse.core.navigator` | Interface contract |
| `TraverseNavigatorExtensions.kt` | `dev.teogor.traverse.core.navigator` | `navigateAndClearUpTo`, `launchAsNewRoot`, `isOnBackStack`, `entriesOf` |
| `NavOptions.kt` | `dev.teogor.traverse.core.navigator` | `data class NavOptions` |
| `TraverseResultStore.kt` | `dev.teogor.traverse.core.result` | `expect class TraverseResultStore` |
| `TraverseResultStoreImpl.kt` | `dev.teogor.traverse.core.result` (actual per platform) | `actual class TraverseResultStore` using `MutableSharedFlow` |
| `NavigationResultExtensions.kt` | `dev.teogor.traverse.core.result` | `setResultAndNavigateUp`, `setResultAndPopTo` |
| `TraverseDsl.kt` | `dev.teogor.traverse.core.dsl` | `@DslMarker annotation class TraverseDsl` |

**Unit tests to write:**

| Test class | What it tests |
|---|---|
| `TraverseNavigatorExtensionsTest` | `navigateAndClearUpTo`, `launchAsNewRoot`, `isOnBackStack`, `entriesOf` with a `FakeTraverseNavigator`-style test double |
| `NavOptionsTest` | Default values, copy semantics |
| `TraverseResultStoreTest` | `setResult`, `getResult`, `clearResult`, `observeResult` — platform-independent tests via `commonTest` |

**Acceptance criteria:**
- `./gradlew :traverse-core:compileKotlinJvm` ✅
- `./gradlew :traverse-core:compileKotlinAndroid` ✅  
- `./gradlew :traverse-core:testJvm` ✅

---

### M3 — `traverse-compose` Source Files

*Goal: `TraverseHost` renders destinations; full DSL works; tests pass.*

**Files to create:**

| File | Package | Contents |
|---|---|---|
| `TraverseHost.kt` | `dev.teogor.traverse.compose` | `@Composable fun TraverseHost(...)` |
| `TraverseGraphBuilder.kt` | `dev.teogor.traverse.compose.graph` | DSL class |
| `ScreenEntry.kt` | `dev.teogor.traverse.compose.graph` | Internal data holder for screen registrations |
| `DialogEntry.kt` | `dev.teogor.traverse.compose.graph` | Internal data holder for dialog registrations |
| `BottomSheetEntry.kt` | `dev.teogor.traverse.compose.graph` | Internal data holder for bottom sheet registrations |
| `DefaultTraverseNavigator.kt` | `dev.teogor.traverse.compose.navigator` | `internal class DefaultTraverseNavigator(NavBackStack)` |
| `LocalTraverseNavigator.kt` | `dev.teogor.traverse.compose.navigator` | `val LocalTraverseNavigator` |
| `TraverseTransitionSpec.kt` | `dev.teogor.traverse.compose.transition` | Public data class + presets |
| `TraverseNavTransitionSpec.kt` | `dev.teogor.traverse.compose.transition` | Internal `NavTransitionSpec` adapter |
| `CollectTraverseResultOnce.kt` | `dev.teogor.traverse.compose.result` | `@Composable inline fun <reified T>` |
| `TraverseBackHandler.kt` | `dev.teogor.traverse.compose.backgesture` | `expect fun` |
| `AndroidBackHandler.kt` | `androidMain` | `actual fun` using `BackHandler` |
| `IosBackHandler.kt` | `iosMain` | `actual fun` — no-op (native swipe handled by Compose iOS) |
| `DesktopBackHandler.kt` | `jvmMain` | `actual fun` — `KeyEvent(Escape)` listener |
| `WebBackHandler.kt` | `wasmJsMain` | `actual fun` — browser `popstate` listener |
| `TraverseBackStackManager.kt` | internal | Builds `SavedStateConfiguration`, owns `NavBackStack` |
| `TraverseDslProcessor.kt` | internal | Processes `TraverseGraphBuilder` registrations |

**Key implementation notes per file:**

**`TraverseHost.kt`** — Internal flow:
1. Create `TraverseGraphBuilder`, execute `builder` lambda against it.
2. `TraverseDslProcessor` extracts all entry registrations → builds `entryProvider` + collects serializers.
3. Auto-build `SavedStateConfiguration` from collected serializers + optional `serializersModule` param.
4. `rememberNavBackStack(savedStateConfig, startDestination)` → `NavBackStack`.
5. Create `DefaultTraverseNavigator(navBackStack)` or use the provided `navigator`.
6. Provide `LocalTraverseNavigator` via `CompositionLocalProvider`.
7. Call `NavDisplay(backStack, onBack = { navigator.navigateUp() }, transitionSpec = ..., entryProvider = ...)`.
8. Attach `TraverseBackHandler`.

**`DefaultTraverseNavigator.kt`:**
- `navigate(destination, builder)`: Respect `NavOptions.launchSingleTop`, `popUpTo`, `popUpToInclusive`, `restoreState`. Manipulate `navBackStack` directly.
- `navigateUp()`: `navBackStack.removeLastOrNull() != null`.
- `popTo(destination, inclusive)`: Find index of last matching destination, remove entries from that index (inclusive/exclusive) to end.

**`TraverseGraphBuilder.kt`** — `screen<T>` is `inline reified` to capture `T::class` and `serializer<T>()`. Stores as `ScreenEntry(klass = T::class, serializer = serializer<T>(), ...)`.

**`CollectTraverseResultOnce.kt`:**
```kotlin
@Composable
public inline fun <reified T> CollectTraverseResultOnce(key: String, crossinline onResult: (T) -> Unit) {
    val navigator = LocalTraverseNavigator.current
    val resultStore = // get store for current back-stack entry
    LaunchedEffect(key) {
        resultStore.observeResult<T>(key).collect { value ->
            onResult(value)
            resultStore.clearResult(key)
        }
    }
}
```

**Integration tests to write:**

| Test | Scenario |
|---|---|
| `TraverseHostNavigationTest` | navigate forward, back, up |
| `TraverseHostSingleTopTest` | `launchSingleTop = true` does not duplicate entry |
| `TraverseHostPopToTest` | `popTo(dest, inclusive = false/true)` |
| `TraverseHostTransitionTest` | Transitions are applied |
| `TraverseHostDialogTest` | Dialog shown/dismissed correctly |
| `TraverseHostResultTest` | `setResultAndNavigateUp` + `CollectTraverseResultOnce` |
| `TraverseHostNestedGraphTest` | Enter/exit nested graph |

**Acceptance criteria:**
- `./gradlew :traverse-compose:compileKotlinJvm` ✅
- `./gradlew :traverse-compose:compileKotlinAndroid` ✅
- Demo app compiles and runs: `./gradlew :demo:composeApp:assembleDebug` ✅

---

### M4 — `traverse-test` Module

*Goal: Test utilities for Traverse-based projects.*

**Files to create:**

| File | Package | Contents |
|---|---|---|
| `FakeTraverseNavigator.kt` | `dev.teogor.traverse.test` | Records all calls |
| `NavigationEvent.kt` | `dev.teogor.traverse.test` | Sealed class of navigation events |
| `NavigationAssertions.kt` | `dev.teogor.traverse.test` | Extension functions on `FakeTraverseNavigator` |
| `TraverseTestRule.kt` | `dev.teogor.traverse.test` | JUnit rule wrapping `TraverseHost` for Compose UI tests |

**`FakeTraverseNavigator` contract:**
- Starts with `startDestination` as single back stack entry.
- All `navigate()` calls add to `navigationHistory` AND actually manipulate internal back stack.
- `navigateUp()` actually pops the internal back stack.
- `navigationHistory` is public and can be iterated for assertions.

**`TraverseTestRule`:**
```kotlin
public class TraverseTestRule(
    public val startDestination: Destination,
    public val builder: TraverseGraphBuilder.() -> Unit,
) : TestRule {
    public val navigator: FakeTraverseNavigator
    // Sets up a ComposeTestRule with TraverseHost(navigator = fakeNavigator)
}
```

**Acceptance criteria:**
- `./gradlew :traverse-test:compileKotlinJvm` ✅
- `FakeTraverseNavigator` used in traverse-compose tests ✅

---

### M5 — Demo App (Full Feature Coverage)

*Goal: Runnable demo app showcasing every library feature across all platforms.*

**Demo screens to build:**

| Screen | Destination type | Feature demonstrated |
|---|---|---|
| `HomeScreen` | `data object Home` | Start destination, navigate forward |
| `ProfileScreen` | `data class Profile(val userId: String)` | Typed args, navigate up |
| `SettingsScreen` | `data object Settings` | `popTo`, `navigateAndClearUpTo` |
| `ColorPickerScreen` | `data object ColorPicker` | `setResultAndNavigateUp` (producer) |
| `FeedScreen` | `data object Feed` | `CollectTraverseResultOnce` (consumer) |
| `ConfirmDeleteDialog` | `data class ConfirmDelete(val itemId: String)` | `dialog<T>` |
| `ShareSheetScreen` | `data object ShareSheet` | `bottomSheet<T>` |
| `OnboardingGraph` | `data object OnboardingGraph` | Nested graph entry |
| `OnboardingStep1` | `data object OnboardingStep1` | Nested graph screen |
| `OnboardingStep2` | `data object OnboardingStep2` | Nested graph screen |
| `TabsScreen` | `data object Tabs` | Multiple back stacks / bottom nav |
| `TransitionsGallery` | `data object TransitionsGallery` | All transition presets side by side |

**Demo app navigation structure:**
```
Home
├── → Profile (navigate with userId arg)
│     └── → Settings (popTo Home + navigateAndClearUpTo)
├── → Feed (with ColorPicker result flow)
│     └── → ColorPicker (setResultAndNavigateUp)
├── → ConfirmDeleteDialog (dialog<>)
├── → ShareSheet (bottomSheet<>)
├── → OnboardingGraph (nested)
│     ├── OnboardingStep1 → OnboardingStep2
│     └── OnboardingStep2 → popTo(Home, inclusive = true) via launchAsNewRoot
└── → Tabs (bottom nav with multiple back stacks)
      ├── Tab A: own TraverseNavigator
      └── Tab B: own TraverseNavigator
```

**Platforms to verify:**
- Android emulator / device
- iOS Simulator
- JVM Desktop (`./gradlew :demo:composeApp:runJvm`)
- wasmJs browser (`./gradlew :demo:composeApp:wasmJsBrowserRun`)

---

### M6 — Deep Links

*Goal: URI-based navigation for all four platforms.*

**Files to create (in traverse-compose/deeplink):**

| File | Contents |
|---|---|
| `TraverseDeepLink.kt` | `data class TraverseDeepLink(uriPattern: String, ...)` + `deepLink(...)` function |
| `TraverseDeepLinkMatcher.kt` | Parse URI pattern `{param}`, extract values, build destination instance |
| `TraverseDeepLinkHandler.kt` | `expect class TraverseDeepLinkHandler` |
| `AndroidDeepLinkHandler.kt` | `actual` — parse `Intent.data`, match, push to back stack |
| `IosDeepLinkHandler.kt` | `actual` — parse URL from AppDelegate callback |
| `DesktopDeepLinkHandler.kt` | `actual` — parse URI from CLI argument or system protocol handler |
| `WebDeepLinkHandler.kt` | `actual` — read initial `window.location`, parse as deep link |

**URI pattern matching spec:**
- Pattern: `https://example.com/user/{userId}/posts/{postId}`
- Extracts `userId` and `postId` as Strings.
- Maps to constructor parameters of the destination `data class` by name.
- Supports optional parameters: `{param?}` → nullable field.
- Supports encoded chars automatically via `URIDecoder`.

**DSL integration:**
```kotlin
screen<UserProfile>(
    deepLinks = listOf(
        deepLink("https://example.com/user/{userId}"),
        deepLink("myapp://profile/{userId}"),
    )
) { dest -> UserProfileScreen(dest.userId) }
```

**Android manifest integration:**
Traverse generates `<intent-filter>` entries or provides a utility for doing so at runtime (investigate AndroidManifest merge approach vs runtime registration).

**Demo additions:** Add `DeepLinkTestScreen` demonstrating deep link navigation on each platform.

---

### M7 — Multiple Back Stacks (Tab Navigation)

*Goal: Proper bottom-nav support with per-tab back stack preservation.*

**New API:**

```kotlin
/**
 * Creates an independent [TraverseNavigator] for a navigation tab.
 * The back stack is preserved when switching between tabs.
 *
 * @param startDestination The tab's root destination.
 * @param key Unique key for state saving — use a stable value per tab.
 */
@Composable
public fun rememberTraverseNavigator(
    startDestination: Destination,
    key: String,
): TraverseNavigator
```

**Usage pattern (not built into Traverse — just documented):**
```kotlin
val homeNavigator = rememberTraverseNavigator(HomeTab, key = "tab_home")
val searchNavigator = rememberTraverseNavigator(SearchTab, key = "tab_search")
val activeNavigator = if (selectedTab == 0) homeNavigator else searchNavigator

Scaffold(
    bottomBar = {
        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                ...
            )
        }
    }
) {
    TraverseHost(
        startDestination = HomeTab,
        navigator = activeNavigator,
    ) { /* ... */ }
}
```

---

### M8 — Publication

*Goal: Library available on Maven Central.*

**Steps:**

| Task | Detail |
|---|---|
| Maven publishing config | `maven-publish` + Sonatype OSSRH credentials in `traverse-core/build.gradle.kts` and `traverse-compose/build.gradle.kts` |
| Group ID | `dev.teogor.traverse` |
| Artifact IDs | `traverse-core`, `traverse-compose`, `traverse-test` |
| Version scheme | `0.1.0-alpha01` → `0.1.0-rc01` → `0.1.0` |
| POM metadata | `<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`, `<scm>` |
| GitHub Actions CI | Build + test on every PR (`ubuntu-latest`, `macos-latest` for iOS) |
| Binary compat validator | `kotlinx-binary-compatibility-validator` — `apiCheck` task on every build |
| Dokka | Generate API docs, publish to GitHub Pages |
| README | Full getting-started guide, comparison table, migration guide from compose-destinations |
| `0.1.0-alpha01` release | Tag, deploy to Maven Central |

---

## 12. Testing Strategy

### 12.1 Test pyramid

```
Unit tests (traverse-core commonTest)     ← Fast, pure Kotlin, no Compose
  │
Integration tests (traverse-compose)      ← Compose test rule, single module
  │
UI tests (demo/composeApp)                ← Full app on Android + Desktop
```

### 12.2 Unit test targets

- `TraverseNavigatorExtensions` — pure logic, use `FakeTraverseNavigator`
- `NavOptions` — data class, default values
- `TraverseResultStore` — SharedFlow behaviour
- `TraverseDeepLinkMatcher` — URI parsing table tests (M6)

### 12.3 Integration test targets (Compose UI tests)

- `TraverseHostNavigationTest` — navigate, navigateUp, popTo, launchSingleTop
- `TraverseHostDialogTest` — dialog shown / dismissed
- `TraverseHostResultTest` — result passing round trip
- `TraverseHostNestedGraphTest` — enter / exit nested graph, back stack depth
- `TraverseHostTransitionTest` — transition applied (visual test if possible)

### 12.4 CI matrix

| Job | Platform | Trigger |
|---|---|---|
| `build-jvm` | JVM (Ubuntu) | Every PR + main |
| `build-android` | Android (Ubuntu) | Every PR + main |
| `build-ios` | iOS (macOS) | Every PR + main |
| `build-wasm` | wasmJs (Ubuntu) | Every PR + main |
| `api-check` | All | Every PR (bcompat validator) |
| `publish-snapshot` | All | Push to `main` |
| `publish-release` | All | Git tag `v*` |

---

## 13. Publication Strategy

### 13.1 Version policy

```
0.x.y-alphaN  — active development, API may change
0.x.y-betaN   — feature-complete, API stabilising
0.x.y-rcN     — release candidate, only bug fixes
0.x.y         — stable
```

First public milestone: `0.1.0-alpha01` after M3 (core + compose working).

### 13.2 API stability policy

- `traverse-core` reaches API stability at `0.1.0-rc01`.
- `traverse-compose` reaches API stability at `0.1.0-rc01`.
- `traverse-test` is `@ExperimentalTraverseTestApi` until `0.2.0`.
- Deep-link API is `@ExperimentalTraverseApi` until `0.2.0`.

### 13.3 Deprecation policy

Once stable (0.1.0): deprecated APIs are supported for at minimum 2 minor versions before removal.

---

## 14. Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| nav3 API breaks in alpha06+ | High | High | All nav3 types are `internal` — only `traverse-compose` internals change, public API unchanged |
| nav3 nested graph API missing / unstable | Medium | Medium | Implement with flat back stack + sub-list approach as fallback |
| `SavedStateHandle` not available on iOS/Web | High (confirmed) | Medium | `TraverseResultStore` via `MutableSharedFlow` — already designed for this |
| ViewModel scoping on iOS/wasmJs unverified | Medium | Low | Optional dependency, clearly documented, tested manually before M3 close |
| nav3 `dialog` / `bottomSheet` absent | High (likely absent) | Low | Wrap `Dialog {}` / `ModalBottomSheet` ourselves — already planned |
| Binary compatibility breakage | Low | High | `kotlinx-binary-compatibility-validator` on every CI run |
| Web browser history (wasmJs) PoC lib unmaintained | Medium | Low | In-scope for M6 only; fallback = no history support on Web |

---

## 15. Glossary

| Term | Definition |
|---|---|
| **Destination** | Traverse's public marker interface. Extends `NavKey`. All screen/dialog/sheet keys implement this. |
| **NavKey** | nav3's internal marker interface. Never used directly by Traverse callers. |
| **NavBackStack** | nav3's `SnapshotStateList<NavKey>` — the canonical source of back-stack state. |
| **NavDisplay** | nav3's root `@Composable` that renders the active entry. Wrapped by `TraverseHost`. |
| **entryProvider** | nav3's DSL for mapping `NavKey` instances to composable content. |
| **SavedStateConfiguration** | nav3 config object holding explicit serializers for non-JVM state saving. |
| **TraverseNavigator** | Traverse's public navigator interface — wraps back stack manipulation. |
| **DefaultTraverseNavigator** | Internal `TraverseNavigator` implementation backed by a `NavBackStack`. |
| **FakeTraverseNavigator** | Test double — records calls, enables assertions without Compose test rule. |
| **TraverseHost** | The root `@Composable` that owns the back stack and provides the navigator. |
| **TraverseGraphBuilder** | The DSL class inside `TraverseHost { }`. Registers all destination types. |
| **TraverseTransitionSpec** | Data class for animated transition configuration. |
| **TraverseResultStore** | KMP-compatible result store. SharedFlow-based on all platforms. |
| **CollectTraverseResultOnce** | Composable that observes results and callbacks exactly once per result. |
| **TraverseDeepLink** | Data class representing a URI pattern for a destination. |
| **@TraverseDsl** | `@DslMarker` — prevents scope leaks in nested DSL lambdas. |
| **explicitApi()** | Kotlin compiler flag requiring `public` on all public declarations. |
| **GraphKey** | A `Destination` used as the identifier for a nested graph (e.g. `OnboardingGraph`). |
| **SingleTop** | Navigation option preventing duplicate stack entries of the same destination class. |
| **M0–M8** | Milestone numbers as defined in Section 11. |

