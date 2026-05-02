# Traverse — Feature Map

> **Last updated:** 2026-05-02  
> Living document. Update whenever a feature ships or a new idea is captured.
>
> Legend: ✅ Shipped · 🔨 In progress · 📋 Planned · 💡 Idea / Backlog

---

## Current State at a Glance

| Module | Status | Tests |
|---|---|---|
| `traverse-core` | ✅ M2 complete | 15 passing |
| `traverse-compose` | ✅ M3 complete | 13 passing (deep link matcher) |
| `traverse-test` | ✅ M4 complete | 36 passing |
| Demo app | ✅ M5 complete | — (runtime-verified) |
| Deep links | ✅ M6 complete | 13 matcher tests |
| **Annotations** | ✅ **New** — `traverse-annotations` + `traverse-ksp-processor` | — |
| Publication | 📋 M7 | — |

---

## 1. Core Navigation

### ✅ Shipped

| Feature | API | Notes |
|---|---|---|
| Forward navigation | `nav.navigate(Dest)` | Pushes onto `SnapshotStateList` |
| Back navigation | `nav.navigateUp()` | Returns `false` at root |
| `canNavigateUp` | `nav.canNavigateUp` | `backStack.size > 1` |
| Pop to destination | `nav.popTo(Dest)` | Removes entries above target |
| Pop to (inclusive) | `nav.popTo(Dest, inclusive=true)` | Removes target too |
| Navigate and clear up to | `nav.navigateAndClearUpTo<T>(Dest)` | Pop all T entries + push Dest |
| Launch as new root | `nav.launchAsNewRoot<T>(Dest)` | Clears entire stack, pushes Dest |
| Single-top navigation | `nav.navigate(Dest) { launchSingleTop = true }` | No-op if already on top |
| Pop with `popUpTo` | `nav.navigate(Dest) { popUpTo = SomeDest }` | Combined pop + push |
| Current destination | `nav.currentDestination` | `backStack.last()` |
| Back stack read | `nav.backStack` | `List<Destination>`, reactive |
| `isOnBackStack<T>()` | Extension on `TraverseNavigator` | Checks if any T instance exists |
| `entriesOf<T>()` | Extension on `TraverseNavigator` | All T instances in stack order |

### 💡 Ideas

- **`navigateReplace(Dest)`** — pop top, push new (common mobile pattern: swap current screen)
- **`navigateToNewTask(Dest)`** — Android: `Intent.FLAG_ACTIVITY_NEW_TASK` equivalent
- **History cursor** — allow forward navigation after back (browser-style ↔) — complex, post-1.0

---

## 2. Destination Types

### ✅ Shipped

| Type | DSL | Rendering |
|---|---|---|
| Screen | `screen<T> { content }` | `AnimatedContent` |
| Dialog | `dialog<T> { content }` | `Dialog {}` overlay |
| Bottom Sheet | `bottomSheet<T> { content }` | `ModalBottomSheet {}` overlay |
| Nested graph key | `nested(startDest, graphKey = T)` | Routing alias, no visual |

### ✅ Shipped

| Feature | API | Notes |
|---|---|---|
| Per-screen transition overrides | `screen<T>(enterTransition = { slideIn() }, exitTransition = { … })` | Overrides host-level `TraverseTransitionSpec` for one destination; falls back to global if null |

### 💡 Ideas

- **`fullScreenDialog<T>`** — renders the content as a `Dialog` but full-screen (Android `Theme.MaterialComponents.Dialog.Alert` equivalent)
- **`sideSheet<T>`** — right/left drawer variant of bottom sheet
- **`popover<T>`** — anchored tooltip/dropdown destination (desktop-friendly)
- **`customOverlay<T>`** — escape hatch: caller provides the wrapper composable (currently all overlays are Dialog/ModalBottomSheet hardcoded)

---

## 3. Graph Building DSL

### ✅ Shipped

```kotlin
TraverseHost(startDestination = Home) {
    screen<Home>           { HomeScreen() }
    screen<Profile>        { dest -> ProfileScreen(dest.userId) }
    dialog<Confirm>        { dest -> ConfirmDialog(dest.message) }
    bottomSheet<Picker>    { PickerSheet(…) }
    nested(startDestination = Step1, graphKey = OnboardingGraph) {
        screen<Step1> { … }
        screen<Step2> { … }
    }
}
```

### 💡 Ideas

- **Lazy graph registration** — register destinations after `TraverseHost` is composed (dynamic feature modules, OTA updates)
- **Route groups** — an alias for a set of destinations that all get the same transition or auth guard
- **`guard<T>(predicate) { content }`** — conditional rendering; if predicate fails, redirect to a fallback destination (auth gate pattern)
- **`overrideTransition<T>(spec)`** — per-destination transition override on an existing registration without re-registering

---

## 4. Transitions

### ✅ Shipped

| Feature | API | Notes |
|---|---|---|
| Fade | `TraverseTransitionSpec.fade(durationMillis = 300)` | |
| Horizontal slide | `TraverseTransitionSpec.horizontalSlide(durationMillis = 300)` | |
| None (instant cut) | `TraverseTransitionSpec.none()` | |
| Custom | Provide `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition` lambdas | |
| Per-destination override | `screen<T>(enterTransition = { … }, exitTransition = { … }, …)` | Overrides host-level spec for one screen; all 4 directions supported |

All transitions are direction-aware: push shows enter/exit, back shows popEnter/popExit automatically.

### 📋 Planned

| Feature | Description | Milestone |
|---|---|---|
| Predictive back (Android 14+) | `PredictiveBackHandler` gesture interpolation | Post-M5 |

### 💡 Ideas

- **`verticalSlide`** — slide up/down (modal-style push)
- **`scale`** — zoom in/out transition (iOS-inspired)
- **`sharedElement`** — shared element transitions between destinations (major feature, depends on Compose SharedTransitionLayout)
- **Directional awareness** — automatic push-vs-pop flip based on destination "depth" in a declared hierarchy rather than back-stack membership

---

## 5. Navigation Results

### ✅ Shipped

| Feature | API |
|---|---|
| Set result + navigate up | `nav.setResultAndNavigateUp(key, value)` |
| Set result + pop to | `nav.setResultAndPopTo(key, value, dest, inclusive)` |
| Collect once (composable) | `CollectTraverseResultOnce<T>(key) { value -> … }` |
| Raw set/clear/observe | `nav.setResult()`, `nav.clearResult()`, `nav.observeResult()` |

All backed by `MutableSharedFlow(replay = 1)` — works identically on all 6 platforms.

### 💡 Ideas

- **Typed result keys** — `val RESULT_COLOR = traverseResultKey<String>("color")` — prevents typos and type mismatches at compile time
- **Result timeout** — auto-clear result if not collected within N seconds
- **Multiple pending results** — currently one result per key; allow queuing multiple values on the same key (e.g., multi-selection pickers)
- **`awaitResult<T>(key)`** — suspend function that returns the result (for ViewModel use)

---

## 6. Nested Graphs

### ✅ Shipped

- `nested(startDestination, graphKey?, builder)` — flat entry registry + graph-key redirect map
- Multiple nested graphs in one `TraverseHost`
- Nested graphs inside nested graphs (unlimited depth)
- Exit via `nav.popTo(root)` — removes all nested destinations at once

### 💡 Ideas

- **Named nested graphs** — expose the graph structure to tooling / analytics
- **Graph-scoped ViewModel** — a `ViewModel` that lives while any destination in a nested graph is on the stack and is cleared when the graph is fully popped
- **Tab navigation (multiple independent back stacks)** — `rememberTraverseNavigator()` per tab, each with its own `SnapshotStateList`; tab state preserved on switch

---

## 7. Testing (`traverse-test`)

### ✅ Shipped

| Feature | API |
|---|---|
| `FakeTraverseNavigator` | Drop-in test double, full `TraverseNavigator` implementation |
| Real back-stack simulation | Same `popUpTo` / `launchSingleTop` logic as production |
| Navigation call recording | `fake.navigateCalls`, `fake.popToCalls`, `fake.navigateUpCount` |
| Result store | Full `setResult` / `clearResult` / `observeResult` in tests |
| `reset()` | Clears history between test cases |
| `assertNavigatedTo<T>()` | At least one navigate call to type T |
| `assertLastNavigatedTo<T>()` | Most recent navigate call was to type T |
| `assertCurrentDestination<T>()` | Top of stack is type T |
| `assertNavigatedUp(times)` | `navigateUp()` called exactly N times |
| `assertPoppedTo<T>(inclusive)` | `popTo(T, inclusive)` was called |
| `assertResultSet(key, value)` | `setResult(key, value)` was called |
| `assertBackStack(vararg)` | Exact back-stack equality |
| `assertNoNavigation()` | Zero navigate/navigateUp/popTo calls |

### 📋 Planned

| Feature | Description |
|---|---|
| `TraverseComposeTestRule` | Compose UI test rule that wraps `TraverseHost` with a `FakeTraverseNavigator` injected — lets you write `composeRule.navigate(…)` and assert the rendered screen |

### 💡 Ideas

- **`assertNavigatedTo<T> { dest -> … }`** — lambda overload to assert the destination's type-safe fields (e.g. `assertNavigatedTo<Profile> { it.userId == "42" }`)
- **Navigation state machine tests** — declarative test helper that follows a sequence of navigations and asserts each resulting back stack

---

## 8. Back Gesture Handling

### ✅ Shipped

| Platform | Status |
|---|---|
| Android | ✅ `BackHandler` from `androidx.activity.compose` |
| iOS | ✅ no-op (Compose Multiplatform handles swipe-back via UIKit) |
| Desktop (JVM) | ✅ `Escape` / `Alt+Left` via `Modifier.onPreviewKeyEvent` on root Box |
| Browser (JS) | ✅ no-op stub |
| Browser (Wasm) | ✅ no-op stub |

### 📋 Planned

| Platform | Feature | Notes |
|---|---|---|
| Android | Predictive back animation (Android 14+) | `PredictiveBackHandler` with progress interpolation |
| Browser (JS/Wasm) | Browser history API | `history.pushState` + `popstate` event listener |

---

## 9. Deep Links

### ✅ Shipped (M6)

| Feature | API | Notes |
|---|---|---|
| DSL registration | `screen<T>(deepLinks = listOf(deepLink("pattern/{param}")))` | In `TraverseGraphBuilder.screen` |
| URI pattern matching | `{paramName}` in path and query segments | Regex-based, KMP-safe positional groups |
| Destination reconstruction | `@Serializable` + `kotlinx-serialization-json` | String→Long/Double/Boolean coercion |
| Programmatic navigation | `navigator.navigateToDeepLink(uri): Boolean` | Returns `false` when no pattern matches |
| Android: Intent handler | `MainActivity.onCreate` reads `intent.data` | URI passed to `App(pendingDeepLink=...)` |
| Multi-scheme support | `traverse://`, `https://`, any custom scheme | One destination can have multiple patterns |
| Test support | `fake.deepLinkCalls`, `assertDeepLinkNavigatedTo(uri)` | `FakeTraverseNavigator` records calls |
| Multiple patterns per destination | `deepLinks = listOf(deepLink("a"), deepLink("b"))` | All registered in `TraverseDeepLinkRegistry` |
| Query parameter extraction | Params after `?` merged with path params | Available to serialization reconstruction |

**Registered demo URIs:**
- `traverse://demo/target/{id}` → `DeepLinkTarget(id)`
- `https://traverse.teogor.dev/target/{id}` → `DeepLinkTarget(id)`
- `traverse://demo/feature/{featureId}` → `FeatureDetail(featureId)`

### 💡 Ideas

- **Type-safe deep link builder** — `DeepLink.build<Profile>(userId = "42")` → URI without string concatenation
- **Deep link testing utilities** — `fake.simulateDeepLink(uri)` with actual registry matching
- **Deferred deep links** — queue a deep link to navigate after onboarding (first-install pattern)
- **iOS URL scheme handler** — `TraverseDeepLinkHandler.handleUrl(url)` called from `AppDelegate`
- **Desktop protocol handler** — register OS-level URI scheme, call `navigateToDeepLink` at startup
- **Web: initial URL routing** — parse `window.location` on wasmJs startup and navigate

---

## 10. Multiple Back Stacks (Tab Navigation)

### ✅ Shipped

```kotlin
// Create one navigator per tab — each owns its own back stack.
val homeNav   = rememberTraverseNavigator(Home)
val searchNav = rememberTraverseNavigator(Search)
val profileNav = rememberTraverseNavigator(Profile)

NavigationBar {
    NavigationBarItem(…, onClick = { selectedTab = Tab.Home })
    NavigationBarItem(…, onClick = { selectedTab = Tab.Search })
    NavigationBarItem(…, onClick = { selectedTab = Tab.Profile })
}

when (selectedTab) {
    Tab.Home    -> TraverseHost(startDestination = Home,    navigator = homeNav)    { … }
    Tab.Search  -> TraverseHost(startDestination = Search,  navigator = searchNav)  { … }
    Tab.Profile -> TraverseHost(startDestination = Profile, navigator = profileNav) { … }
}
```

`rememberTraverseNavigator(startDestination)` returns a `TraverseNavigator` backed by a
`SnapshotStateList`. When passed to `TraverseHost` via the `navigator` parameter, the full
animated navigation engine is used and nested-graph resolution is wired automatically.
Tab state (each tab's independent back stack) is preserved across tab switches.

### 💡 Ideas

- **Saved-state per tab** — persist each tab's back stack across process death / config changes

---

### 💡 Ideas / Future Feature

Currently the back stack lives only in memory. On Android process death the back stack is lost. Future work:

- Restore `EntrySpec.serializer` usage — currently `null`, reserved for this feature
- Collect `serializer<T>()` from `screen<T>` registrations in a dedicated inline reified context (not inside composable lambdas to avoid the `ComposableSingletons` serialization bug)
- Serialize `List<Destination>` to JSON via `SerializersModule` + `PolymorphicSerializer`
- Persist to `SavedStateHandle` on Android, `NSUserDefaults` / file storage on iOS, `localStorage` on Web
- `rememberTraverseNavigator()` restores state on recompose after process death

---

## 12. Analytics / Interceptors

### 💡 Ideas / Future Feature

```kotlin
TraverseHost(
    interceptor = TraverseNavigationInterceptor { event ->
        analytics.track("nav_${event.destination::class.simpleName}")
    }
)
```

- `TraverseNavigationInterceptor` — intercept all `navigate`, `navigateUp`, `popTo` calls before they execute
- Built-in logging interceptor for debug builds
- Firebase Analytics pre-built integration
- Block navigation conditionally (for auth gating without a `guard<T>` DSL)

---

## 13. ViewModel Integration

### 💡 Ideas / Future Feature

Nav3's `lifecycle-viewmodel-navigation3` provides ViewModel scoping per back-stack entry. Since we don't use nav3 runtime, ViewModel scoping currently follows standard Compose `viewModel()` semantics (scoped to the Activity/Fragment/Screen, not to the nav entry).

Future options:
- Integrate `lifecycle-viewmodel-navigation3` as an optional add-on module (`traverse-viewmodel`)
- Implement custom `ViewModelStoreOwner` per `TraverseHost` entry
- `rememberDestinationViewModel<VM>(dest)` — provides a VM scoped to the destination's lifetime on the back stack

---

## 14. Publication (M7)

### 📋 Planned

| Task | Notes |
|---|---|
| Maven publishing | `dev.teogor.traverse:traverse-core:1.0.0-alpha01` etc. |
| `gradle-maven-publish-plugin` setup | `vanniktech` or custom publication config |
| GitHub Actions CI | Build all 6 targets + run tests on every PR |
| Binary compatibility validator | `kotlinx-binary-compatibility-validator` — prevents accidental API breaks |
| Dokka HTML docs site | Auto-generated from KDoc — host on GitHub Pages |
| `README.md` badges | Build status, Maven Central version, KMP targets |
| `CHANGELOG.md` | First entry: `1.0.0-alpha01` |

### 💡 Ideas

- **BOM (Bill of Materials) artifact** — `dev.teogor.traverse:traverse-bom:1.0.0-alpha01` for easy version alignment
- **Version catalog TOML** — publish a `libs.versions.toml` snippet for the GitHub readme
- **Gradle plugin** — optional `traverse-plugin` that validates `@Serializable` at build time without KSP (Kotlin IR transform)

---

## 15. KSP / Annotation Processing (Optional, Post-1.0)

## 15. Annotations + KSP (`traverse-annotations` + `traverse-ksp-processor`)

### ✅ Shipped

#### `traverse-annotations` (KMP, zero external deps)

| Annotation | Target | Purpose |
|---|---|---|
| `@TraverseScreen` | `CLASS` | Marks a full-screen destination |
| `@TraverseDialog(dismissOnBackPress, dismissOnClickOutside)` | `CLASS` | Marks a dialog destination |
| `@TraverseBottomSheet(skipPartiallyExpanded)` | `CLASS` | Marks a bottom sheet destination |
| `@TraverseRoot(graphKey)` | `CLASS` | Marks start destination of a graph |
| `@DeepLink(pattern)` | `CLASS`, repeatable | Associates a URI pattern |
| `@Transition(preset, durationMillis)` | `CLASS` | Per-screen transition preset |
| `@ScreenMeta(name, description, group)` | `CLASS` | Human-readable metadata |

```kotlin
@TraverseScreen
@TraverseRoot
@ScreenMeta(name = "Home", description = "App root", group = "main")
@Serializable data object Home : Destination

@TraverseScreen
@DeepLink("https://example.com/user/{userId}")
@DeepLink("app://profile/{userId}")
@Transition(TransitionPreset.HORIZONTAL_SLIDE)
@ScreenMeta(name = "Profile", group = "account")
@Serializable data class Profile(val userId: String) : Destination

@TraverseDialog(dismissOnClickOutside = false)
@Serializable data class ConfirmDelete(val itemId: String) : Destination

@TraverseBottomSheet
@Serializable data object TagPicker : Destination
```

**`TransitionPreset` enum:** `FADE`, `HORIZONTAL_SLIDE`, `VERTICAL_SLIDE`, `SLIDE_AND_FADE`, `SCALE_AND_FADE`, `ELEVATE`, `NONE`

**`ScreenRegistry` + `ScreenInfo`:** in-process metadata registry. Populate with the generated `initTraverseScreenRegistry()` call, or manually via `ScreenRegistry.register(ScreenInfo(...))`.

```kotlin
// After calling initTraverseScreenRegistry():
val allScreens = ScreenRegistry.screens
val accountGroup = ScreenRegistry.byGroup("account")
val withDeepLinks = ScreenRegistry.all.filter { it.deepLinkPatterns.isNotEmpty() }
```

#### `traverse-ksp-processor` (JVM-only, KSP 2.3.7)

Add to your project: `ksp(project(":traverse-ksp-processor"))` (or the Maven artifact).

For KMP projects processing `commonMain` annotations:

```kotlin
// build.gradle.kts
dependencies {
    add("kspCommonMainMetadata", "dev.teogor.traverse:traverse-ksp-processor:x.y.z")
}
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
}
kotlin.sourceSets.getByName("commonMain") {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
```

**Generated files per destination (example for `Profile(val userId: String)`):**

| File | Contents |
|---|---|
| `ProfileRoute.kt` | `object ProfileRoute { val deepLinks: List<TraverseDeepLink>; fun buildDeepLink(userId: String): String }` |
| `ProfileNavigatorExtensions.kt` | `fun TraverseNavigator.navigateToProfile(userId: String, builder: NavOptions.() -> Unit = {})` |
| `TraverseAutoGraph.kt` | `fun TraverseGraphBuilder.traverseAutoGraph(homeContent: ..., profileContent: ...) { screen<Home>(...) screen<Profile>(...) ... }` |
| `TraverseScreenRegistry.kt` | `fun initTraverseScreenRegistry() { ScreenRegistry.register(ScreenInfo(...)) ... }` |

**Before KSP (manual DSL):**
```kotlin
TraverseHost(startDestination = Home) {
    screen<Home> { HomeScreen() }
    screen<Profile>(
        deepLinks = listOf(deepLink("https://example.com/user/{userId}")),
        transitionSpec = TraverseTransitionSpec.horizontalSlide(300),
    ) { dest -> ProfileScreen(dest.userId) }
    dialog<ConfirmDelete>(content = { dest -> ConfirmDeleteDialog(dest) })
}
```

**After KSP (generated auto-graph):**
```kotlin
// Call at startup
initTraverseScreenRegistry()

// TraverseHost registration — just provide content lambdas
TraverseHost(startDestination = Home) {
    traverseAutoGraph(
        homeContent    = { HomeScreen() },
        profileContent = { dest -> ProfileScreen(dest.userId) },
        confirmDeleteContent = { dest -> ConfirmDeleteDialog(dest) },
        tagPickerContent = { TagPickerSheet() },
    )
}

// Navigation — type-safe, no manual Profile(userId = ...) construction
navigator.navigateToProfile(userId = "42")
```

### 💡 Ideas / Future

- **Validation at build time**: KSP processor emits a `KSPLogger.error()` if `@TraverseScreen` is missing `@Serializable`
- **Multi-module `traverseAutoGraph`**: per-module registration functions (`registerProfileModule()`) that compose at the app level
- **Debug overlay**: a composable panel that reads `ScreenRegistry` and shows all registered destinations — useful during development
- **`@guard<T>(predicate)`**: annotation-driven conditional navigation (auth gates)
- **Shared element annotation**: `@SharedElement(tag)` to auto-wire shared element transitions between two `@TraverseScreen` destinations

---

## Summary Table

| Area | ✅ Done | 📋 Next | 💡 Ideas |
|---|---|---|---|
| Core Navigation | Forward, back, popTo, singleTop, launchAsNewRoot | — | Replace, history cursor |
| Destination Types | screen, dialog, bottomSheet, nested | — | fullScreenDialog, sideSheet, popover, guard |
| Transitions | fade, slide, none, custom, per-destination | Predictive back | verticalSlide, scale, shared element |
| Results | setResult, setResultAndNavigateUp, CollectOnce | — | Typed keys, awaitResult, timeout |
| Testing | FakeNavigator + 8 assertion helpers + deepLink assertions | ComposeTestRule | Lambda assertions, state machine tests |
| Back Gesture | Android ✅, Desktop ✅ (Escape / Alt+Left), others stub | Predictive back (Android 14+), Browser history |
| Deep Links | ✅ DSL + matcher + registry + `navigateToDeepLink` + Android intent | iOS/Desktop/Web OS integration | Type-safe builder, deferred deep links |
| Tab Navigation | `rememberTraverseNavigator` ✅ | — | Saved-state per tab |
| Saved State | EntrySpec.serializer populated (now used by deep links) | Back stack persistence | Platform storage adapters |
| Analytics | — | — | Interceptor, Firebase plugin |
| ViewModel | — | — | Per-entry VM scoping |
| **Annotations + KSP** | ✅ `traverse-annotations` (7 annotations, ScreenRegistry) + `traverse-ksp-processor` (Route, navigate extensions, traverseAutoGraph, ScreenRegistry init) | Build-time `@Serializable` validation | Multi-module auto-graph, debug overlay, @guard |
| Publication | — | M7: Maven Central + CI + Dokka | BOM, Gradle plugin |








