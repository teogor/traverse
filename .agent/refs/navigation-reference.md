# Navigation Libraries — Comprehensive Reference

> Covers the full Android/KMP navigation ecosystem: nav2 (legacy), nav3 (Google Android-only), and nav3 (JetBrains KMP) — the one Traverse is built on.
> **Last verified:** May 2, 2026
>
> Quick index:
> - [Ecosystem Overview](#1-ecosystem-overview)
> - [nav2 — navigation-compose 2.x](#2-nav2--navigation-compose-2x-legacy)
> - [nav3 — JetBrains KMP Edition (Traverse base)](#3-nav3--jetbrains-kmp-edition-use-this-for-traverse)
> - [nav3 — Google Android-only Edition](#4-nav3--google-android-only-edition-do-not-use-in-traverse)
> - [nav2 → nav3 Migration Concepts](#5-nav2--nav3-migration-concepts)
> - [Official Documentation Links](#6-official-documentation-links)

---

## 1. Ecosystem Overview

```
Timeline:  Jetpack Navigation (nav1) → Navigation Compose 2.x (nav2) → Navigation 3 (nav3)

nav1   — Fragment-based, XML nav graph, predates Compose
nav2   — Compose-adapted nav1; JetBrains fork supports KMP (2.9.1 current)
           ↳ Used in Armature: org.jetbrains.androidx.navigation:navigation-compose:2.9.1
nav3   — Redesigned from scratch for Compose; TWO independent implementations:
           ↳ Google original:   androidx.navigation3:navigation3-ui      ← Android-ONLY
           ↳ JetBrains fork:    org.jetbrains.androidx.navigation3:*     ← KMP + Web ★ USE THIS
```

**Traverse uses:** `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05`

**nav3 philosophy (from Android Developers blog):**
- Navigation 3 is more of a *new library* than a new version; it's a fundamental redesign.
- The back stack is now **user-owned** — you create and manage a `SnapshotStateList<NavKey>`; the UI observes it directly.
- Low-level building blocks give more flexibility in customising animation, layout, and behaviour.
- Adaptive layout system allows displaying *multiple destinations simultaneously*.

---

## 2. nav2 — navigation-compose 2.x (legacy)

> Reference only — used in Armature, **not** used in Traverse.

### Key classes (nav2)

| nav2 | Purpose |
|---|---|
| `NavHostController` | Manages back stack, routing, deep links |
| `NavHost` | Composable that renders the current destination |
| `NavGraphBuilder` | DSL for registering destinations |
| `rememberNavController()` | Creates `NavHostController` |
| `NavController.navigate(route)` | Push a destination by route string/object |
| `NavController.navigateUp()` | Pop back stack |
| `NavController.popBackStack()` | Pop with options |
| `NavBackStackEntry.savedStateHandle` | Pass results back between destinations |

### nav2 pattern (for reference only)

```kotlin
// nav2 — DO NOT replicate in Traverse; kept for comparison
val navController = rememberNavController()
NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("profile/{id}") { backStackEntry ->
        ProfileScreen(id = backStackEntry.arguments?.getString("id")!!)
    }
}
navController.navigate("profile/42")
navController.navigateUp()
```

### nav2 limitations (why nav3 was created)

- String-based routes are fragile (runtime crashes on typos).
- `NavGraphBuilder` registration happens once at setup; destinations cannot be dynamically added.
- `NavHostController` is opaque — testing requires Compose test rule.
- Multiple back stacks (bottom nav tabs) required complex workarounds.
- Adaptive layouts (show two destinations side-by-side) not natively supported.

### nav2 KMP support (JetBrains fork)

JetBrains maintains `org.jetbrains.androidx.navigation:navigation-compose` with KMP targets. As of `2.9.1` it supports Android + iOS + Desktop (JVM). **No wasmJs.** This is the last version Armature uses.

---

## 3. nav3 — JetBrains KMP Edition (USE THIS FOR TRAVERSE)

> Source: https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html
> Version: `1.0.0-alpha05`

### Maven coordinates (verified)

```toml
[versions]
nav3 = "1.0.0-alpha05"
compose-adaptive = "1.3.0-alpha02"
compose-lifecycle = "2.10.0-alpha05"
nav3-browser = "0.2.0"

[libraries]
# ✅ Required — navigation3-common added transitively (do NOT also declare navigation3-common)
jetbrains-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "nav3" }
# Optional: adaptive layout (ListDetailPaneScaffold)
jetbrains-adaptive-navigation3 = { module = "org.jetbrains.compose.material3.adaptive:adaptive-navigation3", version.ref = "compose-adaptive" }
# Optional: ViewModel scoping per NavEntry
jetbrains-lifecycle-viewmodel-navigation3 = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "compose-lifecycle" }
# Optional: web browser history (back/forward buttons), proof-of-concept by JetBrains engineer
nav3-browser = { module = "com.github.terrakok:navigation3-browser", version.ref = "nav3-browser" }
```

### Platform support (all platforms ✅)

| Platform | Status |
|---|---|
| Android | ✅ |
| iOS (arm64 / simulatorArm64) | ✅ |
| Desktop (JVM) | ✅ |
| Web (wasmJs) | ✅ |

### Core concepts

#### `NavKey` — the destination marker
```kotlin
// All destinations must implement NavKey + be @Serializable
@Serializable data object Home    : NavKey
@Serializable data object Profile : NavKey
@Serializable data class  UserDetail(val userId: String) : NavKey
```

**For Traverse:** `interface Destination : NavKey` — Traverse's own type extending `NavKey`.

#### Back stack
```kotlin
// KMP-compatible overload — requires SavedStateConfiguration for iOS/Web state saving
val backStack = rememberNavBackStack(savedStateConfig, Home)

// Back stack IS navigation:
backStack.add(UserDetail(userId = "42"))  // navigate forward
backStack.removeLastOrNull()               // navigate back
```

#### `SavedStateConfiguration` — required for non-JVM platforms

On iOS and Web, reflection-based serialization is unavailable. Must use explicit serializers:

```kotlin
val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Home::class, Home.serializer())
            subclass(UserDetail::class, UserDetail.serializer())
        }
    }
}
```

**Traverse auto-builds this** from the `screen<T>` DSL registrations (using `inline reified` + `serializer<T>()`). Callers don't write `SavedStateConfiguration` manually.

#### Three serialization patterns

**Pattern 1 — Sealed interface (single-module, recommended):**
```kotlin
@Serializable sealed interface Route : NavKey
@Serializable data object Home    : Route
@Serializable data class Profile(val id: String) : Route

val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) { subclassesOfSealed<Route>() }
    }
}
```

**Pattern 2 — Multi-module aggregated sealed types:**
```kotlin
// Module A defines: @Serializable sealed interface FeatureA : NavKey
// Module B defines: @Serializable sealed interface FeatureB : NavKey
// App combines: subclassesOfSealed<FeatureA>() + subclassesOfSealed<FeatureB>()
```

**Pattern 3 — Multi-module individual registration:**
```kotlin
val moduleA = SerializersModule { polymorphic(NavKey::class) { subclass(RouteA1::class, ...) } }
val config = SavedStateConfiguration { serializersModule = moduleA + moduleB }
```
For Traverse multi-module: `TraverseHost(serializersModule = combinedModule) { ... }`

#### `NavDisplay`

```kotlin
@Composable
fun NavDisplay(
    backStack: List<NavKey>,
    onBack: () -> Unit,                           // typically backStack.removeLastOrNull()
    transitionSpec: NavTransitionSpec = DefaultNavTransitionSpec,
    predictiveBackSpec: NavPredictiveBackSpec? = null,
    navLocalProviders: List<NavLocalProvider> = emptyList(),
    entryProvider: (key: NavKey) -> NavEntry<*>,  // use entryProvider { } DSL
)
```

#### `entryProvider { }` DSL

```kotlin
entryProvider {
    entry<Home> { HomeContent() }
    entry<UserDetail> { dest -> UserDetailContent(userId = dest.userId) }
}
```

`entry<T>` is `inline reified` — how `TraverseGraphBuilder.screen<T>` works under the hood.

#### Transitions

```kotlin
NavDisplay(
    transitionSpec = object : NavTransitionSpec {
        @Composable
        fun AnimatedContentTransitionScope<NavEntry<*>>.contentTransform(
            from: NavEntry<*>, to: NavEntry<*>
        ): ContentTransform = fadeIn() togetherWith fadeOut()
    },
    ...
)
```

`TraverseTransitionSpec` wraps `NavTransitionSpec` with `fade()`, `horizontalSlide()`, `none()`.

#### ViewModel scoping

```kotlin
entry<Profile>(metadata = navEntryMetadata { /* viewmodel store */ }) { dest ->
    val vm: ProfileViewModel = viewModel()  // scoped to this entry
}
```

### Complete KMP example

```kotlin
@Serializable sealed interface Route : NavKey
@Serializable data object Home    : Route
@Serializable data class Profile(val userId: String) : Route

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) { subclassesOfSealed<Route>() }
    }
}

@Composable
fun App() {
    val backStack = rememberNavBackStack(config, Home)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> { HomeScreen(onGoToProfile = { backStack.add(Profile("42")) }) }
            entry<Profile> { dest -> ProfileScreen(dest.userId) { backStack.removeLast() } }
        }
    )
}
```

### Known limitations (May 2026)

| Feature | Status | Traverse approach |
|---|---|---|
| `dialog` destination type | ⚠️ Verify — likely absent | Wrap in `Dialog { }` composable |
| `bottomSheet` destination type | ⚠️ Verify — likely absent | Wrap in `ModalBottomSheet` |
| Nested navigation (nested NavDisplay) | ⚠️ Not documented for KMP | Research recipes repo before implementing |
| `SavedStateHandle` on iOS/Web | ❌ Android-only | `TraverseResultStore` via `MutableSharedFlow` |
| Web browser history | ✅ via PoC lib | `navigation3-browser:0.2.0` |

---

## 4. nav3 — Google Android-only Edition (DO NOT USE IN TRAVERSE)

> Reference only — shows what compilation errors look like when using the wrong artifact.

```toml
# ❌ Android-only — do NOT use in Traverse / KMP projects
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime" }
androidx-navigation3-ui      = { module = "androidx.navigation3:navigation3-ui" }
```

The Google edition requires `compileSdk = 36`, `minSdk = 23` and targets Android only.
Core API shape is the same as the JetBrains edition, but without `SavedStateConfiguration`
(uses reflection-based serialization, which only works on JVM/Android).

From the migration guide, Android-only migration steps are:
1. Routes implement `NavKey` interface
2. Replace `NavController` with `SnapshotStateList<NavKey>` + management class
3. Move destinations from `NavHost`'s `NavGraph` → `entryProvider { entry<T> { } }`
4. Bottom tabs become separate `SnapshotStateList` instances per tab

---

## 5. nav2 → nav3 Migration Concepts

> Source: https://developer.android.com/guide/navigation/navigation-3/migration-guide
> Note: This guide covers Android-only migration; KMP migration follows the same patterns.

### Conceptual mapping

| nav2 concept | nav3 equivalent |
|---|---|
| Route string/type | `NavKey` implementing class |
| `NavController` | `SnapshotStateList<NavKey>` + navigator class |
| `NavHost` | `NavDisplay` |
| `NavGraphBuilder` + `composable<T> { }` | `entryProvider { entry<T> { } }` |
| `NavHostController.navigate(dest)` | `backStack.add(dest)` |
| `NavHostController.navigateUp()` | `backStack.removeLastOrNull()` |
| `NavHostController.popBackStack(dest)` | `backStack.removeAll { it !is T }` then `backStack.removeLast()` |
| `NavBackStackEntry.savedStateHandle` | nav3 ViewModel scoping (Android) / `TraverseResultStore` (KMP) |
| Nested graphs | Nested `NavDisplay` or flat back stack with sub-lists |
| Multiple back stacks (bottom nav) | One `SnapshotStateList` per tab |
| Deep links | Manual URI parsing → `backStack.add(parsedDestination)` |

### Migration prerequisites (Android-specific)

- `compileSdk = 36` (required for Google nav3 artifact; NOT required for JetBrains fork — verify)
- Routes must be strongly-typed (not string-based)
- Destinations must be Compose functions (no Fragment support)

### What nav3 does NOT support yet

- String-based routes (must use typed `NavKey` subclasses)
- Shared destinations (screens in multiple back stacks simultaneously)
- Fragment destinations
- The full deep link system from nav2

---

## 6. Official Documentation Links

| Resource | URL |
|---|---|
| **JetBrains KMP nav3 guide** (primary reference) | https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html |
| **Android nav3 overview** | https://developer.android.com/guide/navigation/navigation-3 |
| **nav2 → nav3 migration guide** | https://developer.android.com/guide/navigation/navigation-3/migration-guide |
| **nav3 recipes repo** (Android, KMP port exists) | https://github.com/android/navigation-samples |
| **nav3 AOSP source** | https://cs.android.com (search `navigation3`) |
| **JetBrains nav3 version releases** | https://central.sonatype.com/artifact/org.jetbrains.androidx.navigation3/navigation3-ui |
| **nav3 design blog post** | https://android-developers.googleblog.com (search "Navigation 3") |
| **Browser history PoC** | https://github.com/terrakok/navigation3-browser |
| **compose-destinations** (for feature reference) | https://github.com/raamcosta/compose-destinations |

