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
| `traverse-compose` | ✅ M3 complete | — (build-verified) |
| `traverse-test` | ✅ M4 complete | 29 passing |
| Demo app | ✅ M5 complete | — (runtime-verified) |
| Deep links | 📋 M6 | — |
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

### 📋 Planned (M6)

| Feature | Description |
|---|---|
| `deepLink<T>(uriPattern)` | DSL registration inside `screen<T>` |
| URI pattern matching | `{paramName}` extracts fields from data class destinations |
| Android: Intent handler | `TraverseDeepLinkHandler` reads Intent URI → pushes matched destination |
| iOS: URL scheme handler | `TraverseDeepLinkHandler.handleUrl(url)` from `AppDelegate` |
| Desktop: CLI / protocol | `TraverseDeepLinkHandler.handleUri(uri)` at startup |
| Web: query-string navigation | Parse initial URL in wasmJs / JS, push matched destination |
| Programmatic | `nav.navigateToDeepLink(uri: String)` |

### 💡 Ideas

- **Type-safe deep link builder** — `DeepLink.build<Profile>(userId = "42")` → `"traverse://app/profile/42"` without string concatenation
- **Deep link testing utilities** — `fake.simulateDeepLink(uri)` assertion helper
- **Deferred deep links** — queue a deep link to navigate after onboarding completes (first-install pattern)

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

The current approach (zero codegen) is the primary path. KSP is an opt-in overlay.

### 💡 Ideas

```kotlin
// With KSP plugin (future):
@TraverseDestination
@Serializable data class Profile(val userId: String) : Destination
// Generates: ProfileDestination.kt with deepLink helpers, route constants
```

- `traverse-ksp-annotations` — `@TraverseDestination`, `@DeepLink(pattern)` annotations
- `traverse-ksp-processor` — generates route constants, deep link helpers, navigation extension functions
- Purely additive — callers using the DSL-only approach are unaffected

---

## Summary Table

| Area | ✅ Done | 📋 Next | 💡 Ideas |
|---|---|---|---|
| Core Navigation | Forward, back, popTo, singleTop, launchAsNewRoot | — | Replace, history cursor |
| Destination Types | screen, dialog, bottomSheet, nested | **Per-screen transitions ✅** | fullScreenDialog, sideSheet, popover, guard |
| Transitions | fade, slide, none, custom, **per-destination ✅** | Predictive back | verticalSlide, scale, shared element |
| Results | setResult, setResultAndNavigateUp, CollectOnce | — | Typed keys, awaitResult, timeout |
| Testing | FakeNavigator + 8 assertion helpers | ComposeTestRule | Lambda assertions, state machine tests |
| Back Gesture | Android ✅, Desktop ✅ (Escape / Alt+Left), others stub | Predictive back (Android 14+), Browser history |
| Deep Links | — | Full M6 feature | Type-safe builder, deferred deep links |
| Tab Navigation | `rememberTraverseNavigator` ✅ | — | Saved-state per tab |
| Saved State | EntrySpec.serializer reserved | Back stack persistence | Platform storage adapters |
| Analytics | — | — | Interceptor, Firebase plugin |
| ViewModel | — | — | Per-entry VM scoping |
| Publication | — | M7: Maven Central + CI + Dokka | BOM, Gradle plugin |
| KSP (opt-in) | — | — | @TraverseDestination, route gen |








