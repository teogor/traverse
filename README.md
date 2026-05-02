# Traverse

> **Type-safe, KMP-first navigation for Compose Multiplatform — zero codegen, zero external nav runtime.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-brightgreen)](#)

Traverse is the spiritual successor to [compose-destinations](https://github.com/raamcosta/compose-destinations) (no longer maintained), redesigned from the ground up for **Kotlin Multiplatform**. Its navigation engine is **fully self-contained** — no external navigation library runtime is required. The back stack is a plain `SnapshotStateList<Destination>` and transitions are driven by Compose's own `AnimatedContent`.

---

## Why Traverse?

| | compose-destinations | Voyager | Decompose | **Traverse** |
|---|---|---|---|---|
| KMP (iOS + Desktop + Web) | ❌ Android only | ✅ | ✅ | ✅ |
| No annotation processing | ❌ KSP required | ✅ | ✅ | ✅ |
| No external nav runtime | ❌ nav2 | ✅ own stack | ✅ own tree | ✅ self-contained |
| Type-safe by default | ✅ | Partial | Partial | ✅ |
| Deep links (all platforms) | ❌ Android only | ❌ | ❌ | ✅ |
| Compose-first | ✅ | ✅ | Partial | ✅ |
| Actively maintained | ❌ | ⚠️ Slow | ✅ | ✅ |

---

## Platforms

| Platform | Status |
|---|---|
| Android | ✅ Supported |
| iOS (arm64 / simulatorArm64) | ✅ Supported |
| Desktop (JVM) | ✅ Supported |
| Web (JS / WasmJs) | ✅ Supported |

---

## Quick Start

### 1. Add dependency

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("dev.teogor.traverse:traverse-compose:1.0.0-alpha01")
}
```

### 2. Define destinations

No annotations, no code generation — just `@Serializable` data classes:

```kotlin
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable

@Serializable data object Home      : Destination
@Serializable data object Settings  : Destination
@Serializable data class  UserProfile(val userId: String) : Destination
@Serializable data class  ItemDetail(val id: Int, val name: String) : Destination
```

### 3. Set up the host

```kotlin
@Composable
fun App() {
    TraverseHost(startDestination = Home) {
        screen<Home>        { HomeScreen() }
        screen<Settings>    { SettingsScreen() }
        screen<UserProfile> { dest -> UserProfileScreen(dest.userId) }
        screen<ItemDetail>  { dest -> ItemDetailScreen(dest.id, dest.name) }

        dialog<ConfirmDialog>  { dest -> ConfirmDialogScreen(dest) }
        bottomSheet<ShareSheet>{ dest -> ShareSheetScreen(dest) }

        nested(startDestination = OnboardingStep1, graphKey = Onboarding) {
            screen<OnboardingStep1> { Step1Screen() }
            screen<OnboardingStep2> { Step2Screen() }
        }
    }
}
```

### 4. Navigate

```kotlin
@Composable
fun HomeScreen() {
    val navigator = LocalTraverseNavigator.current

    Button(onClick = { navigator.navigate(UserProfile(userId = "42")) }) {
        Text("Open Profile")
    }
}
```

---

## Core API

### Navigator

```kotlin
// Navigate forward
navigator.navigate(Settings)
navigator.navigate(UserProfile(userId = "42"))

// Navigate back
navigator.navigateUp()
navigator.popTo(Home, inclusive = false)

// Back-stack helpers
navigator.navigateAndClearUpTo<Home>(destination)  // pop everything up to Home, then push
navigator.launchAsNewRoot(Home)                    // clear entire stack, start fresh

// Properties
navigator.canNavigateUp  // false when at root

// Results
navigator.setResultAndNavigateUp("key", value)
navigator.setResultAndPopTo("key", value, route = Home)
```

### Results

```kotlin
// Producer (destination that returns a result)
val navigator = LocalTraverseNavigator.current
navigator.setResultAndNavigateUp("selected_color", "red")

// Consumer (destination waiting for the result)
CollectTraverseResultOnce<String>("selected_color") { color ->
    println("User picked: $color")
}
```

### Transitions

```kotlin
TraverseHost(
    startDestination = Home,
    transitions = TraverseTransitionSpec.horizontalSlide(),
) { … }

// Built-in presets
TraverseTransitionSpec.fade(durationMillis = 300)
TraverseTransitionSpec.horizontalSlide(durationMillis = 300)
TraverseTransitionSpec.none()

// Per-destination override in the builder:
screen<Settings>(
    enterTransition = { fadeIn() },
    exitTransition  = { fadeOut() },
) { SettingsScreen() }
```

### Deep Links

Register URI patterns alongside any screen destination. Traverse extracts `{param}` values and reconstructs the destination via `kotlinx.serialization` — no factories needed:

```kotlin
screen<UserProfile>(
    deepLinks = listOf(
        deepLink("https://myapp.example.com/user/{userId}"),
        deepLink("myapp://profile/{userId}"),
    )
) { dest -> UserProfileScreen(dest.userId) }
```

Navigate programmatically from any platform:

```kotlin
// Matches a registered pattern → reconstructs UserProfile(userId="42") → navigates
val handled = navigator.navigateToDeepLink("https://myapp.example.com/user/42")
```

**Android** — handle incoming intents in `MainActivity`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val deepLink = intent?.data?.toString()   // e.g. "myapp://profile/42"
    setContent { App(pendingDeepLink = deepLink) }
}
```

---

## Multiple Back Stacks (Tab Navigation)

Use `rememberTraverseNavigator` to keep per-tab state:

```kotlin
val homeNav   = rememberTraverseNavigator(Home)
val searchNav = rememberTraverseNavigator(Search)

when (selectedTab) {
    Tab.Home   -> TraverseHost(startDestination = Home,   navigator = homeNav)   { … }
    Tab.Search -> TraverseHost(startDestination = Search, navigator = searchNav) { … }
}
```

---

## Modules

| Artifact | Purpose |
|---|---|
| `traverse-core` | `Destination` interface, `TraverseNavigator`, back-stack extensions, navigation results |
| `traverse-compose` | `TraverseHost`, `TraverseGraphBuilder`, transitions, deep links, `LocalTraverseNavigator` |
| `traverse-test` | `FakeTraverseNavigator`, assertion helpers for unit tests |

---

## Design Philosophy

1. **Zero codegen** — No KSP, no KAPT. Destinations are plain `@Serializable` Kotlin classes.
2. **KMP-first** — `traverse-core` has zero Android or Compose dependencies; runs the same on all 6 targets.
3. **Self-contained engine** — Traverse does not depend on any external navigation library at runtime. The back stack is a `SnapshotStateList<Destination>`, transitions use `AnimatedContent`.
4. **Progressive disclosure** — Five lines gets you running. Transitions, dialogs, nested graphs, and deep links are opt-in.
5. **Testable** — `TraverseNavigator` is an interface; swap in `FakeTraverseNavigator` in any unit test without a Compose runtime.

---

## Documentation

| Document | Location | Audience |
|---|---|---|
| Library overview (this file) | `README.md` | Everyone |
| Feature map | `docs/FEATURES.md` | Everyone |
| Library plan | `docs/PLAN.md` | Contributors |
| Contribution guide | `CONTRIBUTING.md` | Human contributors |
| Architecture decisions | `.agent/ARCHITECTURE.md` | Contributors + AI agent |
| Feature roadmap | `.agent/ROADMAP.md` | Contributors + AI agent |
| Agent working memory | `.agent/MEMORY.md` | AI agent only |

---

## License

```
Copyright 2026 Teodor Grigor

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
