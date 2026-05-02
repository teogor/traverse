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
- [ ] `traverse-test/build.gradle.kts` — KMP, test scope only
- [x] `demo/composeApp/build.gradle.kts` — application, depends on traverse-compose
- [x] `build-logic/` — convention plugins (`TraverseKmpLibraryPlugin`, `TraverseComposePlugin`, `TraverseKmpApplicationPlugin`)
- [x] All modules compile: `./gradlew :traverse-core:compileKotlinJvm` ✅

---

## Milestone 2 — Core API (`traverse-core`)
*Goal: `Destination` interface, `TraverseNavigator` interface, back-stack extensions, result API.*

- [x] `traverse-core/src/commonMain/…/core/Destination.kt`
- [ ] `traverse-core/src/commonMain/…/navigator/TraverseNavigator.kt`
  - `navigate(destination, builder)`
  - `navigateUp(): Boolean`
  - `popTo(destination, inclusive): Boolean`
  - `canNavigateUp: Boolean`
- [ ] `traverse-core/src/commonMain/…/navigator/TraverseNavigatorExtensions.kt`
  - `navigateAndClearUpTo<T>(destination)`
  - `launchAsNewRoot<Root>(destination)`
- [ ] `traverse-core/src/commonMain/…/result/NavigationResult.kt`
  - `TraverseResultStore` (platform-abstracted)
  - `setResultAndNavigateUp(key, value)`
  - `setResultAndPopTo(key, value, destination, inclusive)`
  - `CollectTraverseResultOnce<T>(key, onResult)` composable
- [ ] `traverse-core/src/commonMain/…/dsl/TraverseDsl.kt` — `@DslMarker` annotation
- [ ] Unit tests in `traverse-core/src/commonTest/…`
- [ ] `explicitApi()` enabled

---

## Milestone 3 — Compose host (`traverse-compose`)
*Goal: `TraverseHost` composable, `TraverseGraphBuilder` DSL, `LocalTraverseNavigator`, transitions.*

- [ ] `traverseGraphBuilder.kt` — `@TraverseDsl` class with:
  - `screen<T : Destination>(enterTransition?, exitTransition?, content)`
  - `dialog<T : Destination>(properties?, content)`
  - `bottomSheet<T : Destination>(content)` *(pending nav3 API)*
  - `nested(startDestination, graphKey?, builder)`
- [ ] `TraverseHost.kt` — `@Composable fun TraverseHost(startDestination, modifier, navigator?, transitions?, builder)`
  - Wraps nav3 `NavDisplay` + `rememberNavBackStack`
  - Provides `LocalTraverseNavigator`
- [ ] `DefaultTraverseNavigator.kt` — nav3 `NavBackStack` adapter
- [ ] `LocalTraverseNavigator.kt` — `CompositionLocal<TraverseNavigator>`
- [ ] `TraverseTransitionSpec.kt` — `fade()`, `horizontalSlide()`, `none()` presets
- [ ] Integration tests (compose test rule)
- [ ] `explicitApi()` enabled

---

## Milestone 4 — Test utilities (`traverse-test`)
*Goal: `FakeTraverseNavigator` and assertion helpers.*

- [ ] `FakeTraverseNavigator` — records all calls, exposes history list
- [ ] `assertNavigatedTo<T>()` extension
- [ ] `assertNavigatedUp()` extension
- [ ] `assertPoppedTo<T>(inclusive)` extension
- [ ] `assertResultSet(key, value)` extension

---

## Milestone 5 — Demo app
*Goal: Demo app covering all features with living, runnable screens.*

- [ ] **Basic navigation** — `navigate()`, `navigateUp()`
- [ ] **Typed arguments** — `@Serializable data class` destinations
- [ ] **Navigation results** — `setResultAndNavigateUp` + `CollectTraverseResultOnce`
- [ ] **Nested graphs** — sub-graph with its own start destination
- [ ] **Back-stack ops** — `popTo`, `navigateAndClearUpTo`, `launchAsNewRoot`
- [ ] **Dialog destinations** — `dialog<T> { }`
- [ ] **Transitions** — `fade()` / `horizontalSlide()` / custom per-screen
- [ ] Runs on Android, JVM Desktop, iOS simulator

---

## Milestone 6 — Deep Links

- [ ] Design URI scheme (`traverse://host/path?param=value`)
- [ ] Android: Intent handling via `TraverseDeepLinkHandler`
- [ ] Desktop: Custom URI scheme registration
- [ ] iOS: URL scheme in `Info.plist` + handler
- [ ] `deepLink<T>(uriPattern)` in `TraverseGraphBuilder`

---

## Milestone 7 — Publication

- [ ] Maven publishing (`dev.teogor.traverse:traverse-core:x.y.z`)
- [ ] GitHub Actions CI — build all targets on every PR
- [ ] API binary compatibility check (Metalava or `kotlinx-binary-compatibility-validator`)
- [ ] Dokka docs site
- [ ] Publish `0.1.0-alpha01` to Maven Central

---

## Backlog (post-1.0)

- [ ] `@Destination` annotation + KSP processor (optional, for projects that prefer codegen)
- [ ] Navigation analytics plugin (intercept all nav events)
- [ ] Shared-element transitions (nav3 dependent)
- [ ] Compose Navigation testing integration
- [ ] Web browser history integration (via `navigation3-browser` or native nav3 1.1.0)

