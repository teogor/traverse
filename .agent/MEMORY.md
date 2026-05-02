# Traverse — Agent Working Memory

> **YOU ARE THE AGENT FOR THIS PROJECT.**
>
> ⚠️ READ THIS FILE COMPLETELY AT THE START OF EVERY SESSION.
>
> 🔁 **UPDATE THIS FILE after every task you complete** — add a progress log entry, update TODO status.
>
> 💾 **COMMIT AT THE END OF EVERY SESSION** using:
> ```bash
> git add -A && git commit -m "<conventional commit message summarizing what you did>"
> ```
>
> Branch: `main` (commit directly to main until the project reaches beta)

## .agent/ Directory Layout

```
.agent/
├── MEMORY.md        ← this file — read first, update after every task
├── ARCHITECTURE.md  ← all design decisions — update when decisions change
├── ROADMAP.md       ← milestones and per-item checklist — tick off as you go
└── refs/
    ├── nav3.md                  ← androidx.navigation3 full API reference + gotchas
    └── compose-destinations.md  ← compose-destinations feature inventory; what to replicate/drop/improve
```

**When to read refs:**
- `refs/nav3.md` — BEFORE writing any code in `traverse-compose`. Contains the exact API, known limitations, and code examples.
- `refs/compose-destinations.md` — when implementing a new feature; check what compose-destinations did and what the Traverse verdict is.

---

**Traverse** is a **Kotlin Multiplatform navigation library** for Compose Multiplatform.

It is the spiritual successor to [compose-destinations](https://github.com/raamcosta/compose-destinations), which is no longer maintained. Traverse is built on top of the **JetBrains KMP fork of androidx.navigation3** (`org.jetbrains.androidx.navigation3`, version `1.0.0-alpha05`) and targets **Android, iOS, Desktop (JVM), and Web (wasmJs)** — all four platforms.

**Key differentiators vs compose-destinations:**
- No annotation processing / KSP — zero codegen
- KMP-first (Android + iOS + Desktop + Web)
- Built on nav3 (not the deprecated nav2)
- Type-safe via `@Serializable` + `NavKey` — no code generation needed

**Author:** Teodor Grigor (`dev.teogor`)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.x |
| UI | Compose Multiplatform (JetBrains) |
| Navigation base | `org.jetbrains.androidx.navigation3:navigation3-ui` `1.0.0-alpha05` (JetBrains KMP fork — NOT Google's `androidx.navigation3`) |
| Serialization | `kotlinx.serialization` |
| Build | Gradle with `libs.versions.toml` version catalog |
| KMP Targets | `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `jvm`, `wasmJs` |

**Important — TWO nav3 implementations exist:**
- `androidx.navigation3:*` — Google original, **Android-only** — DO NOT USE
- `org.jetbrains.androidx.navigation3:*` — JetBrains KMP fork, **Android + iOS + Desktop + Web** ← USE THIS

**Important:** nav3 destinations must implement `NavKey` (not just `Any`) for KMP serialization. Traverse's `Destination` interface extends `NavKey`. See `.agent/refs/nav3.md` for full details.

---

## Module Structure

```
traverse/
├── traverse-core/          ← KMP, NO Compose dependency
│   └── Destination.kt      ← public interface Destination
│   └── TraverseNavigator.kt ← interface + extensions
│   └── result/             ← setResultAndNavigateUp, CollectTraverseResultOnce
│   └── dsl/TraverseDsl.kt  ← @DslMarker annotation
│
├── traverse-compose/       ← KMP, depends on traverse-core + Compose + nav3
│   └── TraverseHost.kt     ← @Composable entry point
│   └── TraverseGraphBuilder.kt ← DSL wrapper over nav3 entryProvider
│   └── DefaultTraverseNavigator.kt ← nav3 NavBackStack adapter
│   └── LocalTraverseNavigator.kt   ← CompositionLocal<TraverseNavigator>
│   └── TraverseTransitionSpec.kt   ← fade/horizontalSlide/none presets
│
├── traverse-test/          ← KMP, testImplementation only
│   └── FakeTraverseNavigator.kt
│   └── TraverseAssertions.kt
│
├── demo/
│   └── composeApp/         ← Runnable Compose MP app, depends on traverse-compose
│
├── build-logic/            ← Convention Gradle plugins
└── gradle/
    └── libs.versions.toml  ← All versions in one place
```

**Strict dependency rule:**
- `traverse-core` → nothing (no project dependencies)
- `traverse-compose` → `traverse-core`
- `traverse-test` → `traverse-core`
- `demo` → `traverse-compose`

---

## Core API Contract (do not change without updating ARCHITECTURE.md)

### Destination

```kotlin
// traverse-core
public interface Destination
// All concrete destinations must also be @Serializable:
@Serializable data object Home : Destination
@Serializable data class UserProfile(val userId: String) : Destination
```

### TraverseNavigator

```kotlin
public interface TraverseNavigator {
    public val backStack: NavBackStack<Destination>
    public fun navigate(destination: Destination, builder: NavOptions.() -> Unit = {})
    public fun navigateUp(): Boolean
    public fun popTo(destination: Destination, inclusive: Boolean = false): Boolean
    public val canNavigateUp: Boolean
}
// Extension functions (top-level, NOT interface methods):
public inline fun <reified T : Destination> TraverseNavigator.navigateAndClearUpTo(destination: T)
public inline fun <reified Root : Destination> TraverseNavigator.launchAsNewRoot(destination: Destination)
```

### TraverseHost

```kotlin
@Composable
public fun TraverseHost(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navigator: TraverseNavigator? = null,   // optional — pass for testing
    transitions: TraverseTransitionSpec? = null,
    builder: TraverseGraphBuilder.() -> Unit,
)
```

### TraverseGraphBuilder

```kotlin
@TraverseDsl
public class TraverseGraphBuilder {
    public inline fun <reified T : Destination> screen(
        enterTransition: ...? = null,
        exitTransition: ...? = null,
        content: @Composable (entry: NavEntry<T>, dest: T) -> Unit,
    )
    public inline fun <reified T : Destination> dialog(
        properties: DialogProperties = DialogProperties(),
        content: @Composable (entry: NavEntry<T>, dest: T) -> Unit,
    )
    public inline fun <reified T : Destination> bottomSheet(  // pending nav3 API
        content: @Composable (entry: NavEntry<T>, dest: T) -> Unit,
    )
    public fun nested(
        startDestination: Destination,
        graphKey: Destination? = null,
        builder: TraverseGraphBuilder.() -> Unit,
    )
}
```

### Navigation Results

```kotlin
// Producer
public fun <T> TraverseNavigator.setResultAndNavigateUp(key: String, value: T)
public fun <T> TraverseNavigator.setResultAndPopTo(key: String, value: T, destination: Destination, inclusive: Boolean = false)

// Consumer
@Composable
public fun <T> CollectTraverseResultOnce(key: String, onResult: (T) -> Unit)
```

### Transitions

```kotlin
public data class TraverseTransitionSpec(
    val enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition?)? = null,
    val exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition?)? = null,
    val popEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition?)? = null,
    val popExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition?)? = null,
) {
    public companion object {
        public fun fade(durationMillis: Int = 300): TraverseTransitionSpec
        public fun horizontalSlide(durationMillis: Int = 300): TraverseTransitionSpec
        public fun none(): TraverseTransitionSpec
    }
}
```

---

## Conventions (always follow these)

1. **`explicitApi()` is enabled in all library modules** — every public declaration needs `public` modifier.
2. **`@TraverseDsl`** (`@DslMarker`) must be on `TraverseGraphBuilder` and any nested builder class.
3. **`internal`** for all implementation classes — `DefaultTraverseNavigator`, element classes, render functions.
4. **`inline fun <reified T : Destination>`** for all graph-builder registration functions.
5. **Naming prefix** — all public API is prefixed `Traverse` except `Destination` and CompositionLocals (`LocalTraverseNavigator`).
6. **Commits** — use [Conventional Commits](https://www.conventionalcommits.org/): `feat(core):`, `fix(compose):`, `chore(deps):`, etc.
7. **One concern per file** — `TraverseNavigator.kt` only, `TraverseGraphBuilder.kt` only, etc.
8. **KDoc** on every public declaration in library modules.
9. **`traverse-core` has NO Compose dependency** — if something requires `@Composable`, it goes in `traverse-compose`.

---

## Current State

| Item | Status |
|---|---|
| Git repo initialized | ✅ |
| `.gitignore` | ✅ |
| `README.md` | ✅ |
| `ARCHITECTURE.md` | ✅ |
| `ROADMAP.md` | ✅ |
| `CONTRIBUTING.md` | ✅ |
| `TRAVERSE_MEMORY.md` | ✅ (this file) |
| Gradle skeleton (settings, build, libs.versions.toml) | ❌ TODO |
| `traverse-core` first source files | ❌ TODO |
| `traverse-compose` first source files | ❌ TODO |
| `traverse-test` skeleton | ❌ TODO |
| Demo app skeleton | ❌ TODO |

**Next task for the next agent:** Start Milestone 1 — Gradle skeleton. See ROADMAP.md for the checklist.

---

## Open Research Questions (resolve before implementing affected areas)

> Full context for each question is in `.agent/refs/nav3.md`.

1. ✅ **nav3 exact artifact coordinates** — VERIFIED: `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05`. See `.agent/refs/nav3.md` → "Maven Coordinates".
2. ✅ **Platform support** — VERIFIED: Android + iOS + Desktop + **wasmJs** all supported as of `1.0.0-alpha05`. Include `wasmJs` in KMP targets.
3. **`Destination` interface design** — Decide in Milestone 2: `typealias Destination = NavKey` vs `interface Destination : NavKey`. Recommendation: Option B (extend NavKey). See `.agent/ARCHITECTURE.md` → Section 2.
4. **`SavedStateConfiguration` auto-generation** — Traverse must collect all `T::class + serializer<T>()` from DSL registrations and build `SavedStateConfiguration` internally. See `.agent/refs/nav3.md` → "SavedStateConfiguration".
5. **Multi-module serialization** — `TraverseHost` needs optional `serializersModule: SerializersModule?` param. See `.agent/refs/nav3.md` → "Three Serialization Patterns".
6. **nav3 nested back-stack API** — Not covered in JetBrains docs. Research KMP nav3 GitHub samples before implementing `nested()`. See `.agent/refs/nav3.md` → "Known Limitations".
7. **nav3 `dialog` and `bottomSheet` entry types** — Likely absent — verify before implementing, then wrap `Dialog { }` / `ModalBottomSheet` if absent.
8. **`SavedStateHandle` on iOS/Web** — Not available. Use `TraverseResultStore` backed by `MutableSharedFlow`. See `.agent/refs/compose-destinations.md` → "Navigation results".
9. **nav3 `NavOptions` equivalent** — Confirm nav3's single-top/restore-state mechanism before implementing `navigate(destination, builder)` overload.

---

## Reference: What Armature Did (for inspiration, do not copy-paste)

Armature (`/Users/teodor.grigor/Teogor/armature`) is the project this grew from. It used **nav2 (`navigation-compose` 2.9.1)** with a custom DSL wrapper. Key files for reference:

| Armature file | Traverse equivalent to create |
|---|---|
| `ArmatureNavigator.kt` | `TraverseNavigator.kt` |
| `ArmatureNavHost.kt` | `TraverseHost.kt` |
| `ArmatureGraphBuilder.kt` | `TraverseGraphBuilder.kt` |
| `ArmatureTransitionSpec.kt` | `TraverseTransitionSpec.kt` |
| `LocalArmatureNavigator.kt` | `LocalTraverseNavigator.kt` |
| `NavigationResult.kt` | `NavigationResult.kt` (in traverse-core) |

**Key differences Traverse must fix vs Armature:**
- Armature used `DefaultArmatureNavigator(controller: NavHostController)` — nav2 API. Traverse wraps `NavBackStack` instead.
- Armature's nested graph used `KClass<out Route>` overload. Traverse should use `reified` inline functions everywhere.
- Armature's result store was Android-only (`savedStateHandle`). Traverse needs a KMP-compatible result store.

---

## Progress Log

### 2026-05-02 — Session 1 (by previous agent from armature project)
- Created the git repo at `/Users/teodor.grigor/Teogor/traverse`.
- Created all documentation and memory files.
- Moved agent files into `.agent/` directory: `MEMORY.md`, `ARCHITECTURE.md`, `ROADMAP.md`.
- Root keeps only `README.md`, `CONTRIBUTING.md`, `LICENSE` (future), `.gitignore`.
- Updated all cross-references to use `.agent/` paths.
- Committed: `chore: move agent docs into .agent/ directory`.
