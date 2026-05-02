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

---

## ⚔️ Operating Protocol — MILITARY COMMAND MODE

**YOU ARE A PERSONAL ASSISTANT OPERATING UNDER STRICT COMMAND AUTHORITY.**

- **STAND BY** — Do NOT take any action, write any code, create any files, or run any commands
  unless the user has explicitly ordered you to do so in this session.
- **EXECUTE FULLY** — Once an order is given, carry it out completely and to the best of your
  ability without asking for unnecessary clarification. Research what you need, then act.
- **REPORT** — After completing an order, report back clearly: what was done, what files were
  changed, what is the current state, and what the logical next order would be.
- **NO FREELANCING** — Do not add unrequested features, files, or changes. Stay exactly within
  the scope of the order.
- **NO HESITATION** — Do not ask permission to proceed with steps that are obviously part of
  carrying out the given order.

## .agent/ Directory Layout

```
.agent/
├── MEMORY.md        ← this file — read first, update after every task
├── ARCHITECTURE.md  ← all design decisions — update when decisions change
├── ROADMAP.md       ← milestones and per-item checklist — tick off as you go
└── refs/
    ├── navigation-reference.md  ← full nav ecosystem reference: nav2, nav3-KMP, nav3-Android, migration, links
    └── compose-destinations.md  ← compose-destinations feature inventory; what to replicate/drop/improve

docs/
└── PLAN.md          ← THE exhaustive library plan: all features, all classes, all milestones, all risks
                       READ THIS before starting any milestone. It supersedes the inline notes in ROADMAP.md.
```

**When to read refs:**
- `refs/navigation-reference.md` — BEFORE writing any code in `traverse-compose`. Contains the exact API, known limitations, and code examples.
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

**Important:** nav3 destinations must implement `NavKey` (not just `Any`) for KMP serialization. Traverse's `Destination` interface extends `NavKey`. See `.agent/refs/navigation-reference.md` for full details.

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
10. **🔁 DEMO SYNC RULE — MANDATORY:** Every time a public API is added or changed in the library (`traverse-core` or `traverse-compose`), the corresponding piece must be reflected in `demo/composeApp` **in the same commit**. The demo is a real, naturally flowing app (a Journal app — see Demo App Design below), NOT a feature checklist. Add/update the relevant screen, destination, or wiring in the demo. If the Compose layer isn't available yet (e.g. during M2), create the destination types + skeleton screen files so they're ready to wire in M3.

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
| `MEMORY.md` | ✅ (this file) |
| Gradle skeleton (settings, build, libs.versions.toml) | ✅ |
| `demo/composeApp` (KMP JetBrains default demo) | ✅ |
| `traverse-core` module skeleton (build.gradle.kts + Destination.kt) | ✅ |
| `traverse-compose` module skeleton (build.gradle.kts) | ✅ |
| `build-logic` convention plugins (Kotlin `Plugin<Project>` classes) | ✅ |
| `traverse.version=1.0.0-alpha01` in `gradle.properties` | ✅ |
| `LICENSE` file (Apache 2.0) | ✅ |
| `traverse-core` M2 source files (Navigator, NavOptions, extensions, result helpers, DSL marker) | ✅ |
| `traverse-core` unit tests (15 passing — extensions + NavOptions) | ✅ |
| `traverse-compose` first source files (TraverseHost, etc.) | ❌ TODO |
| `traverse-test` skeleton | ❌ TODO |

**Next task for the next agent:** Milestone 3 — implement `traverse-compose` source files. See ROADMAP.md and `docs/PLAN.md` §11 M3.

---

## Open Research Questions (resolve before implementing affected areas)

> Full context for each question is in `.agent/refs/navigation-reference.md`.

1. ✅ **nav3 exact artifact coordinates** — VERIFIED: `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05`. See `.agent/refs/navigation-reference.md` → "Maven Coordinates".
2. ✅ **Platform support** — VERIFIED: Android + iOS + Desktop + **wasmJs** all supported as of `1.0.0-alpha05`. Include `wasmJs` in KMP targets.
3. **`Destination` interface design** — Decide in Milestone 2: `typealias Destination = NavKey` vs `interface Destination : NavKey`. Recommendation: Option B (extend NavKey). See `.agent/ARCHITECTURE.md` → Section 2.
4. **`SavedStateConfiguration` auto-generation** — Traverse must collect all `T::class + serializer<T>()` from DSL registrations and build `SavedStateConfiguration` internally. See `.agent/refs/navigation-reference.md` → "SavedStateConfiguration".
5. **Multi-module serialization** — `TraverseHost` needs optional `serializersModule: SerializersModule?` param. See `.agent/refs/navigation-reference.md` → "Three Serialization Patterns".
6. **nav3 nested back-stack API** — Not covered in JetBrains docs. Research KMP nav3 GitHub samples before implementing `nested()`. See `.agent/refs/navigation-reference.md` → "Known Limitations".
7. **nav3 `dialog` and `bottomSheet` entry types** — Likely absent — verify before implementing, then wrap `Dialog { }` / `ModalBottomSheet` if absent.
8. **`SavedStateHandle` on iOS/Web** — Not available. Use `TraverseResultStore` backed by `MutableSharedFlow`. See `.agent/refs/compose-destinations.md` → "Navigation results".
9. **nav3 `NavOptions` equivalent** — Confirm nav3's single-top/restore-state mechanism before implementing `navigate(destination, builder)` overload.

---

## Demo App Design — "Journal" (dev.teogor.traverse.demo)

The demo is a **personal journal app**. It is a real, shippable-looking app — not a feature checklist.
Every Traverse feature is exercised through a natural user flow.

### Navigation structure

```
Onboarding graph (nested)
  ├── OnboardingWelcomeScreen     data object OnboardingWelcome
  ├── OnboardingFeaturesScreen    data object OnboardingFeatures
  └── OnboardingReadyScreen       data object OnboardingReady   → navigate to Home (launchAsNewRoot)

Main app
  ├── HomeScreen                  data object Home               → list of journal entries
  │     ├── navigate → EntryDetailScreen(entryId)
  │     ├── navigate → NewEntryScreen                            (returns entry title via result)
  │     └── navigate → Settings
  ├── EntryDetailScreen           data class EntryDetail(entryId: String)
  │     ├── navigate → ConfirmDeleteDialog(entryId)              (dialog — result: deleted Boolean)
  │     └── navigate → TagPickerSheet                            (bottomSheet — result: tag String)
  ├── NewEntryScreen              data object NewEntry            → setResultAndNavigateUp("new_entry_title", title)
  ├── SettingsScreen              data object Settings
  ├── ConfirmDeleteDialog         data class ConfirmDelete(entryId: String)   [dialog destination]
  └── TagPickerSheet              data object TagPicker                        [bottomSheet destination]
```

### What each screen demonstrates

| Screen | Traverse feature demonstrated |
|---|---|
| Onboarding* → Ready → Home | `nested()`, `launchAsNewRoot` |
| Home | `navigate()`, `canNavigateUp`, bottom nav base |
| EntryDetail(entryId) | Typed args via `data class` destination |
| NewEntry | `setResultAndNavigateUp` (producer), `CollectTraverseResultOnce` on Home (consumer) |
| Settings | `popTo`, `navigateAndClearUpTo` |
| ConfirmDeleteDialog | `dialog<T>`, result passing, `navigateUp` |
| TagPickerSheet | `bottomSheet<T>`, result passing |

### File layout in demo/composeApp/src/commonMain

```
kotlin/dev/teogor/traverse/demo/
├── App.kt                           ← TraverseHost setup (wired in M3)
├── Destinations.kt                  ← All @Serializable destination types
├── screen/
│   ├── HomeScreen.kt
│   ├── EntryDetailScreen.kt
│   ├── NewEntryScreen.kt
│   └── SettingsScreen.kt
├── onboarding/
│   ├── OnboardingWelcomeScreen.kt
│   ├── OnboardingFeaturesScreen.kt
│   └── OnboardingReadyScreen.kt
├── dialog/
│   └── ConfirmDeleteDialog.kt
└── sheet/
    └── TagPickerSheet.kt
```

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

### 2026-05-02 — Session 9 (current)
- **Added DEMO SYNC RULE** to Conventions (rule #10): every public API change must be mirrored in the demo in the same commit.
- **Added Demo App Design section** to MEMORY.md — "Journal" app, full screen/destination/file layout documented.
- **Demo app built (M2 correspondence):**
  - `Destinations.kt` — all `@Serializable` destination types: `Onboarding`, `OnboardingWelcome`, `OnboardingFeatures`, `OnboardingReady`, `Home`, `EntryDetail(entryId)`, `NewEntry`, `Settings`, `ConfirmDelete(entryId)`, `TagPicker`
  - `onboarding/OnboardingWelcomeScreen.kt` — step 1, "Get started" button
  - `onboarding/OnboardingFeaturesScreen.kt` — step 2, feature cards
  - `onboarding/OnboardingReadyScreen.kt` — step 3, "Start journaling" → `launchAsNewRoot`
  - `screen/HomeScreen.kt` — entry list, FAB, Settings button, `RESULT_NEW_ENTRY_TITLE` constant
  - `screen/EntryDetailScreen.kt` — typed `entryId` arg, Delete button, Add tag button, result constants
  - `screen/NewEntryScreen.kt` — form, Save → `setResultAndNavigateUp`
  - `screen/SettingsScreen.kt` — toggles, `popTo(Home)` button
  - `dialog/ConfirmDeleteDialog.kt` — `AlertDialog` for `dialog<ConfirmDelete>`
  - `sheet/TagPickerSheet.kt` — `ModalBottomSheet` for `bottomSheet<TagPicker>`
  - `App.kt` — replaced default demo; full `TraverseHost` wiring shown as TODO comment for M3; placeholder `Text` so it compiles now
  - `Greeting.kt` — removed (dead code from JetBrains template)
  - `demo/composeApp/build.gradle.kts` — added `project(":traverse-core")` dependency
- Build: `BUILD SUCCESSFUL` ✅, 15 traverse-core tests passing ✅

### 2026-05-02 — Session 8
- **Architecture decision — Option A:** `Destination` does NOT extend `NavKey`. `traverse-core` is fully framework-free. Updated `.agent/ARCHITECTURE.md` Section 2.
- **Pre-work:**
  - Added `traverse.version=1.0.0-alpha01` to `gradle.properties`.
  - Added `jetbrains-kotlinx-coroutines-core` library to `gradle/libs.versions.toml`.
  - Updated `traverse-core/build.gradle.kts` — added `kotlinx-coroutines-core` dependency.
  - Created `LICENSE` (Apache 2.0).
- **M2 — `traverse-core` source files (all new/updated):**
  - `Destination.kt` — finalized KDoc, plain marker interface
  - `dsl/TraverseDsl.kt` — `@DslMarker annotation class TraverseDsl`
  - `navigator/NavOptions.kt` — data class with `launchSingleTop`, `popUpTo`, `popUpToInclusive`, `restoreState`
  - `navigator/TraverseNavigator.kt` — interface: `backStack`, `currentDestination`, `canNavigateUp`, `navigate`, `navigateUp`, `popTo`, `setResult`, `clearResult`, `observeResult`
  - `navigator/TraverseNavigatorExtensions.kt` — `navigateAndClearUpTo`, `launchAsNewRoot`, `isOnBackStack`, `entriesOf`
  - `result/NavigationResultExtensions.kt` — `setResultAndNavigateUp`, `setResultAndPopTo`
- **M2 — Unit tests (15 passing on JVM):**
  - `commonTest/navigator/TraverseNavigatorExtensionsTest.kt` — 12 tests covering all extensions + `canNavigateUp` + `currentDestination`
  - `commonTest/navigator/NavOptionsTest.kt` — 3 tests covering defaults, copy, builder lambda
- **KSP future milestone noted** — will be Milestone 9: `traverse-ksp-processor` + `traverse-ksp-annotations` (optional, post-1.0, strictly additive).
- Committed: `feat(core): M2 — traverse-core source files + unit tests`

### 2026-05-02 — Session 7
- Created `docs/PLAN.md` — exhaustive library plan covering:
  - Executive summary, problem statement, design philosophy, competitive landscape
  - Complete feature inventory (sections 5.1–5.13) with milestone tags
  - Full module architecture with file-level breakdown
  - All public API signatures with KDoc (Sections 7.1–7.3)
  - All internal components (Section 8)
  - Platform matrix (Section 9)
  - Dependency map (Section 10)
  - Detailed milestone breakdown M0–M8 (Section 11) with acceptance criteria
  - Testing strategy (Section 12)
  - Publication strategy (Section 13)
  - Risk register (Section 14)
  - Glossary (Section 15)
- Updated `.agent/` directory layout in this file to reference `docs/PLAN.md`.

### 2026-05-02 — Session 6
- Memory file was stale — updated to reflect all work done in sessions 4–5.
- No new code changed in this session — memory sync + commit only.

### 2026-05-02 — Session 5
- Refactored build-logic: replaced precompiled `.gradle.kts` script plugins with Kotlin `Plugin<Project>` classes.
  - Deleted: `traverse.kmp.library.gradle.kts`, `traverse.kmp.library.compose.gradle.kts`, `traverse.kmp.application.gradle.kts`
  - Created: `TraverseKmpLibraryPlugin.kt` (id: `traverse.kmp.library`)
  - Created: `TraverseComposePlugin.kt` (id: `traverse.compose`) — Compose-only, no KMP setup
  - Created: `TraverseKmpApplicationPlugin.kt` (id: `traverse.kmp.application`)
  - Updated `build-logic/build.gradle.kts`: added `gradlePlugin {}` block with all three plugin registrations.
- Compose layer is now **standalone**: `traverse-compose` applies `traverse.kmp.library` + `traverse.compose` (previously one combined `traverse.kmp.library.compose`).
- Demo applies `traverse.kmp.application` + `traverse.compose` + hotReload.
- Committed: `refactor(build-logic): replace .gradle.kts scripts with Kotlin Plugin<Project> classes`.

### 2026-05-02 — Session 4
- Added `build-logic/` convention plugins (precompiled `.gradle.kts`, later replaced in session 5).
  - `traverse.kmp.library` — KMP library baseline: android + iosArm64 + iosSimulatorArm64 + jvm + wasmJs targets, `explicitApi()`, Java 11, android compileSdk/minSdk.
  - `traverse.kmp.library.compose` — extended library + Compose + Kotlin compose compiler.
  - `traverse.kmp.application` — demo app: all targets + js + webMain, android targetSdk/packaging/buildTypes.
  - `traverse-core`, `traverse-compose`, `demo/composeApp` build files slimmed down — convention plugins now handle the boilerplate.
  - `libs.versions.toml`: added `android-gradle-plugin`, `jetbrains-kotlin-gradle-plugin`, `jetbrains-kotlin-compose-gradle-plugin`, `jetbrains-compose-gradle-plugin` (build-logic classpath only).
  - Version catalog entries standardized to `author.library` naming (`jetbrains-*`, `android-*`).
  - Android SDK version entries renamed: `android-sdk-compile`, `android-sdk-min`, `android-sdk-target`.
  - Gradle daemon JVM toolchain configuration added.
  - All packages aligned to `dev.teogor.traverse.*`.
- Committed (multiple commits, see git log `6e097d8`–`fadd5c4`).

### 2026-05-02 — Session 2
- Read all repo files: README.md, CONTRIBUTING.md, ARCHITECTURE.md, ROADMAP.md, MEMORY.md.
- Updated MEMORY.md with Military Command Mode operating protocol.
- Standing by for orders. Next logical order: Milestone 1 — Gradle skeleton.

### 2026-05-02 — Session 3
- Received order: move default KMP demo app to demo module, create traverse library module.
- Moved `composeApp/` → `demo/composeApp/` (all source intact).
- Updated `settings.gradle.kts`: `:composeApp` → `:demo:composeApp`, added `:traverse-core`, `:traverse-compose`.
- Fixed `iosApp/iosApp.xcodeproj/project.pbxproj`: updated Gradle task path to `:demo:composeApp:embedAndSignAppleFrameworkForXcode`.
- Created `traverse-core/build.gradle.kts`: KMP, `explicitApi()`, `kotlinx-serialization-core`, no Compose dep.
- Created `traverse-core/src/commonMain/…/core/Destination.kt`: initial stub.
- Created `traverse-compose/build.gradle.kts`: KMP, `explicitApi()`, Compose + nav3, depends on `:traverse-core`.
- Updated `gradle/libs.versions.toml`: added `kotlinx-serialization 1.8.1`, `navigation3 1.0.0-alpha05`, `kotlinSerialization` plugin.
- Updated root `build.gradle.kts`: declared `kotlinSerialization` plugin.
- Verified: `./gradlew projects` shows all 4 modules correctly. BUILD SUCCESSFUL.
- Committed: `chore: restructure into demo/composeApp + traverse-core + traverse-compose modules`.

### 2026-05-02 — Session 1 (by previous agent from armature project)
- Created the git repo at `/Users/teodor.grigor/Teogor/traverse`.
- Created all documentation and memory files.
- Moved agent files into `.agent/` directory: `MEMORY.md`, `ARCHITECTURE.md`, `ROADMAP.md`.
- Root keeps only `README.md`, `CONTRIBUTING.md`, `LICENSE` (future), `.gitignore`.
- Updated all cross-references to use `.agent/` paths.
- Committed: `chore: move agent docs into .agent/ directory`.
