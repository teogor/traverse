# compose-destinations — Feature Inventory

> **Source:** https://github.com/raamcosta/compose-destinations  
> **Status:** No longer actively maintained (as of 2025). Last stable: 2.x  
> **Purpose of this document:** Catalogue every feature compose-destinations had so Traverse can replicate the good parts and improve or drop the rest.

---

## Summary verdict per feature

| Feature | compose-destinations | Traverse verdict |
|---|---|---|
| Annotation processing (`@Destination`) | KSP required | ❌ Drop — use `@Serializable` DSL instead |
| Type-safe destinations | ✅ (via codegen) | ✅ Keep — via `@Serializable` data classes |
| Typed navigation arguments | ✅ (via codegen) | ✅ Keep — baked into data class fields |
| `DestinationsNavigator` wrapper | ✅ | ✅ Keep → `TraverseNavigator` |
| `DestinationsNavHost` | ✅ | ✅ Keep → `TraverseHost` |
| Nested nav graphs (`@NavGraph`) | ✅ (annotation) | ✅ Keep → `nested { }` in DSL |
| Navigation results (`ResultBackNavigator`) | ✅ | ✅ Keep → `setResultAndNavigateUp` |
| Dialog destinations | ✅ | ✅ Keep → `dialog<T>` in builder |
| Bottom sheet destinations | ✅ | ✅ Keep → `bottomSheet<T>` (pending nav3) |
| Transitions (enter/exit) | ✅ Global + per-destination | ✅ Keep — `TraverseTransitionSpec` |
| Deep links (`@DeepLink`) | ✅ (annotation) | ✅ Keep → `deepLink<T>(pattern)` in DSL |
| `@ActivityDestination` | ✅ Android-only | ❌ Drop — Android-specific, not KMP |
| Custom `NavType` | ✅ | ❌ Drop — `@Serializable` makes this unnecessary |
| Bottom nav integration | ✅ (`DestinationsNavHost` with bottom nav) | ✅ Keep → provide `TraverseNavigator` flow |
| ViewModel scoping per destination | ⚠️ Manual | ✅ Improve — automatic via nav3-viewmodel |
| KMP (iOS / Desktop) | ❌ Android only | ✅ Improve — first-class KMP |
| Code generation | ✅ KSP | ❌ Drop — all DSL, zero codegen |
| `NavArgs` data holders | ✅ | ❌ Drop — `data class` fields replace this |
| `BuildableRoute` (programmatic deep links) | ✅ | 🔜 Consider in v2 |
| Dependency injection integration | ✅ (Hilt, Koin docs) | 🔜 Document patterns, no built-in coupling |
| Multiple back stacks (bottom nav) | ✅ | 🔜 Milestone 6+ |
| `rememberDestinationsNavigator` hook | ✅ | ✅ → `LocalTraverseNavigator.current` |

---

## Features to replicate (with implementation notes)

### 1. Type-safe destinations ✅

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>
@Composable
fun HomeScreen(navigator: DestinationsNavigator) { ... }
// KSP generates: HomeScreenDestination, HomeScreenNavArgs, etc.
```

**Traverse approach (no codegen):**
```kotlin
@Serializable data object Home : Destination
// In builder:
screen<Home> { HomeScreen() }
// In composable:
val navigator = LocalTraverseNavigator.current
```

**Implementation notes:**
- `Destination` is a plain `interface` in `traverse-core`.
- `@Serializable` is required for state saving — document clearly, validate at runtime.
- `inline fun <reified T : Destination>` handles type erasure at registration.

---

### 2. Typed navigation arguments ✅

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>
@Composable
fun UserScreen(userId: String, age: Int)  // KSP generates NavArgs holder
```

**Traverse approach:**
```kotlin
@Serializable data class UserScreen(val userId: String, val age: Int) : Destination
// All args are fields of the destination data class — no extra boilerplate
```

**Implementation notes:**
- kotlinx.serialization handles serialization of Int, String, Boolean, List<T>, nested @Serializable objects automatically.
- No custom `NavType` needed.

---

### 3. DestinationsNavigator → TraverseNavigator ✅

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    navigator.navigate(ProfileScreenDestination(userId = "42"))
}
```

**Traverse approach:**
```kotlin
@Composable
fun HomeScreen() {
    val navigator = LocalTraverseNavigator.current
    navigator.navigate(ProfileScreen(userId = "42"))
}
```

**Interface contract:**
```kotlin
public interface TraverseNavigator {
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})
    public fun navigateUp(): Boolean
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean
    public val canNavigateUp: Boolean
    public val backStack: List<Destination>  // read-only view
}
```

**What Traverse adds that compose-destinations lacked:**
- `canNavigateUp` property
- `launchAsNewRoot<Root>(destination)` helper
- `navigateAndClearUpTo<T>(destination)` helper
- KMP-compatible (works on iOS and Desktop)

---

### 4. Nested navigation graphs ✅

**compose-destinations approach:**
```kotlin
@NavGraph<RootGraph>(start = true)  // defines a nested graph
annotation class OnboardingGraph

@Destination<OnboardingGraph>
@Composable
fun OnboardingStep1Screen() { ... }
```

**Traverse approach:**
```kotlin
@Serializable data object OnboardingGraph : Destination  // graph key

TraverseHost {
    nested(startDestination = OnboardingStep1, graphKey = OnboardingGraph) {
        screen<OnboardingStep1> { OnboardingStep1Screen() }
        screen<OnboardingStep2> { OnboardingStep2Screen() }
    }
}
// Navigate into: navigator.navigate(OnboardingGraph)
```

**Implementation notes:**
- `nested()` creates a sub-scope of `TraverseGraphBuilder`.
- Under the hood: either nested `NavDisplay` or flat back stack — see `.agent/refs/nav3.md` for the two approaches. Confirm with nav3 docs before implementing.

---

### 5. Navigation results ✅

**compose-destinations approach:**
```kotlin
// Producer:
@Destination<RootGraph>
@Composable
fun ColorPickerScreen(resultNavigator: ResultBackNavigator<Color>) {
    resultNavigator.navigateBack(result = Color.Red)
}
// Consumer:
@Destination<RootGraph>
@Composable
fun HomeScreen(resultRecipient: ResultRecipient<ColorPickerScreenDestination, Color>) {
    resultRecipient.onNavResult { result ->
        when (result) {
            is NavResult.Canceled -> { }
            is NavResult.Value -> handleColor(result.value)
        }
    }
}
```

**Traverse approach:**
```kotlin
// Producer:
val navigator = LocalTraverseNavigator.current
navigator.setResultAndNavigateUp(key = "color", value = "red")

// Consumer:
CollectTraverseResultOnce<String>(key = "color") { color ->
    handleColor(color)
}
```

**Implementation notes:**
- On Android: use the back-stack entry `SavedStateHandle` (compatible with nav3 ViewModel layer).
- On iOS/Desktop: use `TraverseResultStore` backed by a `MutableSharedFlow<Pair<String, Any?>>` scoped to the back-stack entry key.
- `CollectTraverseResultOnce` must be a `@Composable` that consumes and clears the result after one delivery (to prevent re-triggering on recomposition).

---

### 6. Dialog destinations ✅

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>(style = DestinationStyle.Dialog::class)
@Composable
fun ConfirmDeleteDialog(navigator: DestinationsNavigator) {
    AlertDialog(onDismissRequest = { navigator.navigateUp() }, ...)
}
```

**Traverse approach:**
```kotlin
dialog<ConfirmDelete> { _, dest ->
    AlertDialog(onDismissRequest = { navigator.navigateUp() }, ...)
}
```

**Implementation notes:**
- nav3 may have a `dialog` `NavEntry` type — check `.agent/refs/nav3.md` Limitations section. If not, wrap in `Dialog { }` composable.
- `DialogProperties` should be passable: `dialog<T>(properties = DialogProperties(...)) { }`.

---

### 7. Bottom sheet destinations ✅ (pending nav3)

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>(style = DestinationStyle.BottomSheet::class)
@Composable
fun ShareSheet(navigator: DestinationsNavigator) { ... }
```

**Traverse approach:**
```kotlin
bottomSheet<ShareSheet> { _, dest ->
    ShareSheetContent(dest)
}
```

**Implementation notes:**
- nav3 does NOT currently have a `bottomSheet` entry type (as of May 2026).
- If not in nav3: implement by wrapping `ModalBottomSheet` composable within the entry content, managing show/hide state via the back stack.
- Add a `NOTE` comment in `TraverseGraphBuilder` (mirroring how Armature handled this) if not yet available.

---

### 8. Animated transitions ✅

**compose-destinations approach:**
```kotlin
// Global:
DestinationsNavHost(
    navController = rememberNavController(),
    engine = rememberAnimatedNavHostEngine(
        defaultAnimationsForNestedNavGraph = DefaultFadingTransitions
    )
)
// Per-destination:
@Destination<RootGraph>(style = SlideInOutTransitionStyle::class)
```

**Traverse approach:**
```kotlin
// Global:
TraverseHost(
    startDestination = Home,
    transitions = TraverseTransitionSpec.horizontalSlide()
) { ... }

// Per-destination (override):
screen<Settings>(
    enterTransition = { fadeIn() },
    exitTransition = { fadeOut() }
) { SettingsScreen() }
```

**Built-in presets:** `fade()`, `horizontalSlide()`, `none()`.

---

### 9. Deep links 🔜 (Milestone 6)

**compose-destinations approach:**
```kotlin
@Destination<RootGraph>(
    deepLinks = [DeepLink(uriPattern = "https://example.com/user/{userId}")]
)
@Composable
fun UserScreen(userId: String) { ... }
```

**Traverse approach (planned):**
```kotlin
screen<UserScreen>(
    deepLinks = listOf(deepLink("https://example.com/user/{userId}"))
) { dest -> UserScreen(dest.userId) }
```

**Implementation notes:**
- Deep links are per-platform: Android uses `IntentFilter`, Desktop uses custom URI scheme, iOS uses `Info.plist` URL scheme.
- Traverse will provide a `TraverseDeepLinkHandler` that parses the URI and pushes the correct destination to back stack.
- The URI pattern `{paramName}` extracts parameters and maps them to `data class` fields.

---

### 10. Bottom nav integration ✅

**compose-destinations approach:**
```kotlin
val navController = rememberNavController()
Scaffold(
    bottomBar = {
        BottomNavigationBar(navController = navController)
    }
) {
    DestinationsNavHost(navController = navController, ...)
}
```

**Traverse approach:**
Traverse does not provide a `BottomNavigationBar` component — it is a Screen-level concern handled by `armature-screen` (or any Scaffold). The `TraverseNavigator` provides enough for any bottom nav implementation:

```kotlin
val navigator = LocalTraverseNavigator.current
val currentDestination = navigator.backStack.last()
NavigationBar {
    NavigationBarItem(
        selected = currentDestination is Home,
        onClick = { navigator.navigate(Home) { launchSingleTop = true } },
        ...
    )
}
```

---

## Features compose-destinations had that Traverse intentionally DROPS

### ① KSP annotation processing

compose-destinations required adding a KSP processor which: slowed incremental builds, required `ksp()` configuration, generated boilerplate `*Destination` objects, caused intermittent incremental cache issues.

**Drop reason:** Traverse uses `@Serializable` Kotlin data classes + `inline reified` functions for 100% of what KSP provided, with zero additional toolchain.

---

### ② Custom `NavType` for complex types

compose-destinations required implementing `DestinationNavTypeSerializer<T>` for any non-primitive argument type.

**Drop reason:** `@Serializable` on the destination data class handles all argument types automatically. kotlinx.serialization serializes `List<T>`, `Map<K,V>`, nested objects, sealed classes, etc.

---

### ③ `@ActivityDestination` 

Allowed navigating to an `Activity` instead of a `Composable`.

**Drop reason:** KMP has no `Activity` concept. On Android, launching activities is an application-level concern handled by `startActivity(intent)` — not a navigation library concern.

---

### ④ `NavArgs` data holders

Auto-generated classes that held navigation arguments as properties.

**Drop reason:** The destination `data class` itself IS the args holder. `UserScreen(userId = "42")` is both the destination key and the args. No extra class needed.

---

## Things compose-destinations did NOT have that Traverse will add

| Feature | Notes |
|---|---|
| **iOS + Desktop support** | KMP-first; compose-destinations was Android-only |
| **`canNavigateUp` property** | Useful for conditionally showing back buttons |
| **`launchAsNewRoot<Root>(dest)`** | Clear entire stack and start fresh |
| **`navigateAndClearUpTo<T>(dest)`** | Type-safe pop-to-then-push |
| **Fake navigator for tests** | `FakeTraverseNavigator` in `traverse-test` module |
| **Global + per-screen transitions in one API** | compose-destinations required separate engines |
| **Optional `navigator` injection into `TraverseHost`** | Enables custom navigators + test doubles |

---

## compose-destinations source files of interest (for algorithm reference)

If you need to understand how compose-destinations implemented something, these are the key source files (GitHub: `raamcosta/compose-destinations`):

| Class | Location | What it does |
|---|---|---|
| `DestinationsNavHostEngine` | `animations/` | Wires transitions into NavHost |
| `ResultBackNavigator` | `result/` | Producer side of nav results |
| `ResultRecipient` | `result/` | Consumer side of nav results |
| `DestinationsNavigator` | `navigation/` | Navigator wrapper over NavController |
| `NavGraphBuilder` extensions | `navgraphs/` | How destination registration worked |

