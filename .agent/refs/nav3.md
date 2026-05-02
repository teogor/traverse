# androidx.navigation3 — API Reference

> **Last verified:** May 2026  
> **Stability:** Experimental / Alpha — API will change. Always verify official docs at:  
> https://developer.android.com/jetpack/androidx/releases/navigation3  
> https://developer.android.com/guide/navigation/navigation3  
>
> ⚠️ **IMPORTANT:** This is a completely different library from `navigation-compose 2.x`.  
> Do NOT confuse `NavController` (nav2) with `NavBackStack` (nav3).  
> Do NOT confuse `NavHost` (nav2) with `NavDisplay` (nav3).

---

## Maven Coordinates

```toml
# gradle/libs.versions.toml
[versions]
navigation3 = "1.0.0-alpha04"   # check for latest alpha at the link above

[libraries]
# Core runtime (KMP-compatible)
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }

# Compose UI layer (KMP-compatible)
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }

# Optional: adaptive layout support
androidx-navigation3-adaptive = { module = "androidx.navigation3:navigation3-adaptive", version.ref = "navigation3" }

# Optional: ViewModel scoping per entry
androidx-navigation3-viewmodel = { module = "androidx.navigation3:navigation3-viewmodel", version.ref = "navigation3" }
```

**KMP support status (as of May 2026):**
- `navigation3-runtime` — ✅ KMP (Android + JVM + iOS)
- `navigation3-ui` — ✅ KMP (Compose Multiplatform)
- `navigation3-adaptive` — ⚠️ Android-only, skip for now
- `navigation3-viewmodel` — ⚠️ Check current status before using

---

## Core Concepts

### The back stack IS the navigation state

In nav3, the back stack is a plain `SnapshotStateList<T>`. There is no `NavController`, no route registry, no `NavGraph`. The list itself is the source of truth — adding to or removing from it IS navigation.

```kotlin
// The entire navigation state:
val backStack = remember { mutableStateListOf<Any>(HomeDestination) }

// Navigate forward:
backStack.add(UserProfile(userId = "42"))

// Navigate back:
backStack.removeLastOrNull()

// Pop to a specific destination:
while (backStack.last() !is HomeDestination) {
    backStack.removeLast()
}
```

### Key Classes

---

#### `NavBackStack<T>` / `MutableStateList`

nav3 does not have a special `NavBackStack` class — it is literally a `SnapshotStateList<T>` from Compose.  
`rememberNavBackStack(initial)` creates one backed by `rememberSaveable`:

```kotlin
// Creates a SnapshotStateList<T> that survives recomposition + process death
val backStack = rememberNavBackStack(HomeDestination)

// Type: SnapshotStateList<T> — just a list, manipulate directly
backStack.add(SomeDestination)
backStack.removeLast()
backStack.removeAll { it is OnboardingStep1 }
```

> ⚠️ VERIFY: `rememberNavBackStack` may require destinations to be `@Serializable` for state saving to work. Confirm with the current alpha docs.

---

#### `NavDisplay`

The composable that renders the back stack. It shows one entry at a time (or multiple in adaptive layouts).

```kotlin
@Composable
fun NavDisplay(
    backStack: List<*>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    transitionSpec: NavTransitionSpec = DefaultNavTransitionSpec,
    predictiveBackSpec: NavPredictiveBackSpec? = null,
    navLocalProviders: List<NavLocalProvider> = emptyList(),
    entryProvider: (key: Any) -> NavEntry<*>,
)
```

- `backStack` — the list; `NavDisplay` reads the **last** entry to decide what to render
- `onBack` — called when the system back gesture/button fires; typically `backStack.removeLastOrNull()`
- `transitionSpec` — enter/exit animations
- `navLocalProviders` — `CompositionLocal` values scoped per entry (e.g., ViewModel stores)
- `entryProvider` — a function from a destination key → `NavEntry`. Use the `entryProvider { }` DSL

---

#### `NavEntry<T>`

Represents a single item in the back stack along with its content factory.

```kotlin
data class NavEntry<T : Any>(
    val key: T,
    val metadata: NavEntryMetadata = EmptyNavEntryMetadata,
    val content: @Composable (T) -> Unit,
)
```

- `key` — the destination object (your `@Serializable` data class)
- `metadata` — optional extras (e.g., ViewModel store owner for nav3-viewmodel)
- `content` — the composable content to render for this entry

---

#### `entryProvider { }` DSL

The recommended way to build the `entryProvider` function for `NavDisplay`:

```kotlin
val ep = entryProvider {
    // Register by reified type — content receives the typed destination
    entry<HomeDestination> {
        HomeScreen()  // 'it' is HomeDestination
    }

    entry<UserProfile> { dest ->
        UserProfileScreen(userId = dest.userId)
    }

    entry<ItemDetail> { dest ->
        ItemDetailScreen(id = dest.id, name = dest.name)
    }
}
```

`entry<T>` is `inline fun <reified T>` — uses `T::class` internally. This is exactly how Traverse's `TraverseGraphBuilder.screen<T>` will work under the hood.

---

#### `NavLocalProvider`

Provides `CompositionLocal` values scoped to a specific back-stack entry (not globally).

```kotlin
// Example: providing a ViewModel store owner per entry
val providers = listOf(
    rememberViewModelStoreNavLocalProvider()  // from navigation3-viewmodel
)

NavDisplay(
    backStack = backStack,
    navLocalProviders = providers,
    ...
)
```

Traverse wraps this internally — callers do not interact with `NavLocalProvider` directly.

---

### Transitions

nav3 uses `NavTransitionSpec` for animations:

```kotlin
// Default — no animation
NavDisplay(transitionSpec = DefaultNavTransitionSpec, ...)

// Custom
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

> ⚠️ VERIFY: The exact `NavTransitionSpec` API — it may have changed since alpha03.  
> `TraverseTransitionSpec` wraps this. Build the wrapper after confirming the nav3 API.

---

### Back Handling

```kotlin
// System back button / gesture
NavDisplay(
    onBack = { backStack.removeLastOrNull() },
    ...
)

// Predictive back (Android 14+)
NavDisplay(
    predictiveBackSpec = NavPredictiveBackSpec { progress ->
        // progress: Float 0..1, use to animate back preview
    },
    ...
)
```

---

### Saving / Restoring State (Process Death)

```kotlin
// rememberNavBackStack uses rememberSaveable internally.
// Requires destinations to be @Serializable (via kotlinx.serialization).
val backStack = rememberNavBackStack(HomeDestination)
```

This is why `Destination : @Serializable` is a **hard requirement** in Traverse.

---

### ViewModel Scoping (nav3-viewmodel)

```kotlin
// Each NavEntry can have its own ViewModel store:
entry<Profile>(
    metadata = navEntryMetadata {
        viewModelStore = ViewModelStore()
    }
) { dest -> ProfileScreen() }

// Access inside the entry:
val vm: ProfileViewModel = viewModel()  // scoped to this entry
```

Traverse will wrap this transparently — `screen<T>` entries automatically get a ViewModel store.

> ⚠️ VERIFY: The exact `navEntryMetadata` API before implementing ViewModel scoping in Traverse.

---

### Nested Navigation

nav3's approach to nested navigation is still evolving. Two approaches exist:

**Approach A — Nested `NavDisplay`:**
```kotlin
// A destination can contain another NavDisplay internally
entry<NestedGraphRoot> {
    val nestedBackStack = rememberNavBackStack(NestedStep1)
    NavDisplay(
        backStack = nestedBackStack,
        onBack = { if (nestedBackStack.size > 1) nestedBackStack.removeLast() else backStack.removeLast() },
        entryProvider = entryProvider {
            entry<NestedStep1> { Step1Screen() }
            entry<NestedStep2> { Step2Screen() }
        }
    )
}
```

**Approach B — Flat back stack with filtering:**
Keep a single back stack and filter by sub-graph membership in the `entryProvider`.

> ⚠️ CONFIRM: Which approach nav3 recommends. This decision affects `TraverseGraphBuilder.nested()` implementation significantly.

---

### Known Limitations / Gotchas (as of May 2026)

1. **No built-in deep link handling** — nav3 does not yet have a `deepLink` feature. It must be implemented manually (parse URI → push to back stack). Traverse will add this in Milestone 6.
2. **No `bottomSheet` destination type** — nav3 does not provide a `bottomSheet` entry type. Traverse must implement it with a `ModalBottomSheet` wrapped around a `NavEntry`.
3. **`dialog` destination** — Check if nav3 has a dialog `NavEntry` type. If not, implement the same way as bottomSheet.
4. **`SavedStateHandle` — not available on iOS/Desktop** — nav3's ViewModel scoping is Android-centric. Traverse's `NavigationResult` must use its own `TraverseResultStore` backed by `MutableSharedFlow` on non-Android platforms.
5. **`rememberNavBackStack` key serialization** — the destination type must be `@Serializable` for process death save/restore to work. Traverse enforces this via the `Destination` interface documentation but cannot enforce it at compile time (no KSP).

---

## Full Example (nav3 without Traverse, for reference)

```kotlin
@Serializable data object Home
@Serializable data class Profile(val userId: String)
@Serializable data class Details(val itemId: Int)

@Composable
fun App() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onGoToProfile = { backStack.add(Profile(userId = "42")) }
                )
            }
            entry<Profile> { dest ->
                ProfileScreen(
                    userId = dest.userId,
                    onGoToDetails = { id -> backStack.add(Details(itemId = id)) },
                    onBack = { backStack.removeLast() }
                )
            }
            entry<Details> { dest ->
                DetailsScreen(
                    itemId = dest.itemId,
                    onBack = { backStack.removeLast() }
                )
            }
        }
    )
}
```

**What Traverse adds on top of this:**  
Type-safe `TraverseNavigator` abstraction, injectable via `LocalTraverseNavigator`, supports `navigateUp()`, `popTo()`, `launchAsNewRoot()`, navigation results, transitions DSL, testability via `FakeTraverseNavigator`.

