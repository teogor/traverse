# Traverse — Roadmap

> This document lives in `.agent/ROADMAP.md`.
> Tracks feature priorities and release milestones.
> **Update this after each session. Mark items ✅ when done, ⚠️ when blocked.**
> Cross-reference `.agent/MEMORY.md` for current agent session context.

---

## Milestone 0 — Scaffold ✅
*Goal: Empty repo with full documentation so a new agent can pick up without context loss.*

- [x] Git repo initialized (`main` branch)
- [x] `README.md` — full library description, API sketches, comparison table
- [x] `ARCHITECTURE.md` — all key design decisions documented
- [x] `ROADMAP.md` — this file
- [x] `TRAVERSE_MEMORY.md` — agent working memory
- [x] `CONTRIBUTING.md` — contribution guidelines
- [x] `.gitignore` — Kotlin/KMP/Gradle/Xcode

---

## Milestone 1 — Gradle skeleton ✅
*Goal: Build system in place, all modules compile an empty `commonMain` source set.*

- [x] `settings.gradle.kts` — multi-module project setup, version catalog
- [x] `gradle/libs.versions.toml` — all dependency versions (nav3 `1.0.0-alpha05` from `org.jetbrains.androidx.navigation3`, Compose, Kotlin, serialization…)
- [x] `build.gradle.kts` (root) — convention plugins applied
- [x] `traverse-core/build.gradle.kts` — KMP, no Compose dep, targets: `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `jvm`, `wasmJs`
- [x] `traverse-compose/build.gradle.kts` — KMP, Compose, nav3, same targets
- [x] `traverse-test/build.gradle.kts` — KMP, test scope only
- [x] `demo/composeApp/build.gradle.kts` — application, depends on traverse-compose
- [x] `build-logic/` — convention plugins (`TraverseKmpLibraryPlugin`, `TraverseComposePlugin`, `TraverseKmpApplicationPlugin`)
- [x] All modules compile: `./gradlew :traverse-core:compileKotlinJvm` ✅

---

## Milestone 2 — Core API (`traverse-core`) ✅
*Goal: `Destination` interface, `TraverseNavigator` interface, back-stack extensions, result API.*

- [x] `traverse-core/src/commonMain/…/core/Destination.kt`
- [x] `traverse-core/src/commonMain/…/navigator/TraverseNavigator.kt`
  - `navigate(destination, builder)`
  - `navigateUp(): Boolean`
  - `popTo(destination, inclusive): Boolean`
  - `canNavigateUp: Boolean`
- [x] `traverse-core/src/commonMain/…/navigator/TraverseNavigatorExtensions.kt`
  - `navigateAndClearUpTo<T>(destination)`
  - `launchAsNewRoot<Root>(destination)`
- [x] `traverse-core/src/commonMain/…/result/NavigationResultExtensions.kt`
  - `setResultAndNavigateUp(key, value)`
  - `setResultAndPopTo(key, value, destination, inclusive)`
- [x] `traverse-core/src/commonMain/…/dsl/TraverseDsl.kt` — `@DslMarker` annotation
- [x] Unit tests in `traverse-core/src/commonTest/…` — 15 passing
- [x] `explicitApi()` enabled

---

## Milestone 3 — Compose host (`traverse-compose`) ✅
*Goal: `TraverseHost` composable, `TraverseGraphBuilder` DSL, `LocalTraverseNavigator`, transitions.*

- [x] `TraverseGraphBuilder.kt` — `@TraverseDsl` class with:
  - `screen<T : Destination>(content)`
  - `dialog<T : Destination>(content)`
  - `bottomSheet<T : Destination>(content)`
  - `nested(startDestination, graphKey?, builder)`
- [x] `TraverseHost.kt` — `@Composable fun TraverseHost(startDestination, modifier, navigator?, transitions?, builder)`
  - Self-contained: uses `AnimatedContent` + `SnapshotStateList<Destination>` (no external nav library)
  - Provides `LocalTraverseNavigator`
  - Renders dialogs as `Dialog {}` overlay, bottom sheets as `ModalBottomSheet {}` overlay
- [x] `DefaultTraverseNavigator.kt` — `SnapshotStateList<Destination>` adapter, handles popUpTo/launchSingleTop
- [x] `LocalTraverseNavigator.kt` — `CompositionLocal<TraverseNavigator>`
- [x] `TraverseTransitionSpec.kt` — `fade()`, `horizontalSlide()`, `none()` presets
- [x] `TraverseResultStore.kt` — `MutableSharedFlow`-backed result store (KMP-compatible)
- [x] `CollectTraverseResultOnce` — composable collector (clears result after delivery)
- [x] Back-gesture handling — `TraverseBackHandler` with `expect/actual` per platform
- [x] `explicitApi()` enabled

---

## Milestone 4 — Test utilities (`traverse-test`) ✅
*Goal: `FakeTraverseNavigator` and assertion helpers.*

- [x] `traverse-test/build.gradle.kts` — KMP library, `api(traverse-core)`, `implementation(kotlin("test"))`, `implementation(coroutines-core)`
- [x] `FakeTraverseNavigator` — implements `TraverseNavigator`; records all navigate/navigateUp/popTo calls; real in-memory back stack with full popUpTo + launchSingleTop logic; result store backed by `MutableSharedFlow`
- [x] `assertNavigatedTo<T>()` — at least one navigate call matched type T
- [x] `assertLastNavigatedTo<T>()` — most recent navigate call matched type T
- [x] `assertCurrentDestination<T>()` — top of back stack is type T
- [x] `assertNavigatedUp(times)` — navigateUp called exactly N times
- [x] `assertPoppedTo<T>(inclusive)` — popTo called with matching type + inclusive flag
- [x] `assertResultSet(key, value)` — setResult called with matching key and value
- [x] `assertBackStack(vararg destinations)` — exact back-stack equality check
- [x] `assertNoNavigation()` — no navigate/navigateUp/popTo calls made
- [x] `FakeTraverseNavigatorTest` — 29 tests, 0 failures ✅

---

## Milestone 5 — Demo app ✅
*Goal: Demo app covering all features with living, runnable screens.*

- [x] **Basic navigation** — `navigate()`, `navigateUp()`
- [x] **Typed arguments** — `@Serializable data class` destinations (`FeatureDetail`)
- [x] **Navigation results** — `setResult` + `CollectTraverseResultOnce` (color picker demo)
- [x] **Nested graphs** — `NestedFlowGraph` → 3-step wizard → `popTo(Catalog)`
- [x] **Back-stack ops** — `popTo`, `launchAsNewRoot`, `launchSingleTop`
- [x] **Dialog destinations** — `dialog<ShowcaseDialog>`
- [x] **Bottom-sheet destinations** — `bottomSheet<OptionSheet>`
- [x] **Live back-stack visualizer** — `BackStackBar` component on every screen
- [x] Runs on Desktop JVM ✅

---

## Milestone 6 — Deep Links ✅
*Goal: URI-based navigation for all platforms — DSL registration, pattern matching, programmatic navigation.*

- [x] `TraverseDeepLink` public data class + `deepLink()` factory function
- [x] `TraverseDeepLinkMatcher` — regex-based pattern compiler; extracts path + query params; KMP-safe positional groups
- [x] `TraverseDeepLinkRegistry` — built from `TraverseGraphBuilder` entries; `resolve(uri): Destination?`
- [x] Destination reconstruction via `kotlinx-serialization-json` — String → Long/Double/Boolean coercion
- [x] `screen<T>(deepLinks = listOf(...))` — registration in `TraverseGraphBuilder`
- [x] `EntrySpec.serializer` now populated (via `serializer<T>()` in inline reified `screen<T>`)
- [x] `TraverseNavigator.navigateToDeepLink(uri: String): Boolean` — interface method with default `false`
- [x] `DefaultTraverseNavigator.navigateToDeepLink` — production implementation using registry
- [x] `TraverseHost` builds + injects `TraverseDeepLinkRegistry` into both default and external-navigator paths
- [x] Android demo: `MainActivity` handles `Intent.data` + `onNewIntent`; URI passed to `App(pendingDeepLink)`
- [x] `FakeTraverseNavigator.deepLinkCalls` list + `assertDeepLinkNavigatedTo(uri)` + `assertDeepLinkNavigated()`
- [x] `TraverseDeepLinkMatcherTest` — 13 unit tests covering all matching scenarios
- [x] `FakeTraverseNavigatorDeepLinkTest` — 7 unit tests
- [x] Demo `DeepLinkDemo` screen with interactive URI input and pre-built examples
- [x] `README.md` updated — Deep Links section, comparison table, design philosophy, Web platform

---

## Milestone 7 — Publication

- [ ] Maven publishing (`dev.teogor.traverse:traverse-core:x.y.z`)
- [ ] GitHub Actions CI — build all targets on every PR
- [ ] API binary compatibility check (Metalava or `kotlinx-binary-compatibility-validator`)
- [ ] Dokka docs site
- [ ] Publish `1.0.0-alpha01` to Maven Central

---

## Backlog (post-1.0)

- [ ] `@Destination` annotation + KSP processor (optional, for projects that prefer codegen)
- [ ] Navigation analytics plugin (intercept all nav events)
- [ ] Shared-element transitions (nav3 dependent)
- [ ] Compose Navigation testing integration
- [ ] Web browser history integration (via `navigation3-browser` or native nav3 1.1.0)

