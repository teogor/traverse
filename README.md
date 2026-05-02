# Traverse

> **Type-safe, KMP-first navigation for Compose Multiplatform — built on `androidx.navigation3`.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop-brightgreen)](#)

Traverse is the spiritual successor to [compose-destinations](https://github.com/raamcosta/compose-destinations) (no longer maintained), redesigned from the ground up for **Kotlin Multiplatform** and built on **`androidx.navigation3`** — the next-generation navigation API from Google.

---

## Why Traverse?

| | compose-destinations | Voyager | Decompose | **Traverse** |
|---|---|---|---|---|
| KMP (iOS + Desktop) | ❌ Android only | ✅ | ✅ | ✅ |
| No annotation processing | ❌ KSP required | ✅ | ✅ | ✅ |
| Built on androidx.navigation3 | ❌ | ❌ | ❌ | ✅ |
| Type-safe by default | ✅ | Partial | Partial | ✅ |
| Compose-first | ✅ | ✅ | Partial | ✅ |
| Actively maintained | ❌ | ⚠️ Slow | ✅ | ✅ |

---

## Platforms

| Platform | Status |
|---|---|
| Android | ✅ Supported |
| iOS (arm64 / simulatorArm64) | ✅ Supported |
| Desktop (JVM) | ✅ Supported |
| WasmJs | 🔜 Planned (post-nav3 stable) |

---

## Quick Start

### 1. Add dependency

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("dev.teogor.traverse:traverse-compose:0.1.0")
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

        nested(startDestination = Onboarding) {
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

// Or custom per-destination in the builder:
screen<Settings>(
    enterTransition = { fadeIn() },
    exitTransition  = { fadeOut() },
) { SettingsScreen() }
```

---

## Modules

| Artifact | Purpose |
|---|---|
| `traverse-core` | `Destination` interface, `TraverseNavigator`, back-stack extensions, navigation results |
| `traverse-compose` | `TraverseHost`, `TraverseGraphBuilder`, `LocalTraverseNavigator`, `TraverseTransitionSpec` |
| `traverse-test` | `FakeTraverseNavigator`, assertion helpers for unit tests |

---

## Design Philosophy

1. **Zero codegen** — No KSP, no KAPT. Destinations are plain `@Serializable` Kotlin classes.
2. **KMP-first** — The `traverse-core` module has zero Android or Compose dependencies.
3. **nav3-native** — Built on `androidx.navigation3`; benefits from all upstream improvements.
4. **Thin layer** — Traverse does not reimplement back-stack logic; it wraps nav3's `NavBackStack` with a type-safe, ergonomic API.
5. **Testable** — `TraverseNavigator` is an interface; swap in `FakeTraverseNavigator` in any unit test without a Compose runtime.

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

