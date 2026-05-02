# Traverse

> **Type-safe, zero-codegen, KMP-first navigation for Compose Multiplatform.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-brightgreen)](#)
[![Status](https://img.shields.io/badge/Status-In%20Development-orange)](#)
[![First release](https://img.shields.io/badge/First%20release-1.0.0--alpha01-blue)](#)

---

> [!WARNING]
> **🚧 This library is under active development — it is not published yet and not ready for use.**
>
> `main` will only receive commits when a version is officially released.
> All development happens on the [`develop`](../../tree/develop) branch.
>
> The first release will be **`1.0.0-alpha01`**. Watch this repository to get notified when it ships.

---

## What is Traverse?

Traverse is a **Kotlin Multiplatform navigation library** for Compose Multiplatform. It is the spiritual successor to [compose-destinations](https://github.com/raamcosta/compose-destinations) — a widely used but now unmaintained Android-only navigation library — rebuilt from the ground up to work across **Android, iOS, Desktop (JVM), and Web (wasmJs)**.

Traverse is built on top of the [JetBrains KMP fork of navigation3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html), the next-generation navigation foundation that replaces the legacy `navigation-compose` approach entirely.

---

## What will it do?

Traverse will give you a complete, production-grade navigation system for Compose Multiplatform applications without requiring annotation processors, code generation, or complex configuration. Here is what it will deliver:

**Type-safe destinations without codegen.** Navigation destinations are plain Kotlin `@Serializable` data classes and objects. No `@Destination` annotations, no KSP processor, no generated `*Destination` wrappers — the destination itself carries its arguments as fields, and the graph builder registers them with a simple inline DSL.

**A clean navigation DSL.** A `TraverseHost` composable accepts a graph-building lambda where you declare screens, dialogs, bottom sheets, and nested sub-graphs. The DSL is scoped with a `@DslMarker` to prevent scope leaks and is designed to be immediately readable by anyone familiar with Compose.

**A stable navigator interface.** `TraverseNavigator` exposes all standard navigation operations — navigate forward, navigate up, pop to a destination (inclusive or exclusive), clear the stack and launch a new root, and check whether back navigation is possible. It is an interface, not a concrete class, so it can be replaced with a fake implementation in tests without a Compose runtime.

**KMP-native result passing.** Returning values from a destination back to the caller (e.g. a colour picker returning the selected colour) works identically on all four platforms using a shared-flow-based result store — no `SavedStateHandle` Android dependency.

**Animated transitions.** Global and per-screen transition configuration with built-in presets (fade, horizontal slide, none) and full custom override support for both push and pop directions.

**Back-gesture handling on every platform.** Android system back button and predictive back gesture, iOS swipe-back, Desktop keyboard shortcut (Escape / Alt+Left), and web browser back button are all handled transparently — you write zero platform-specific code.

**Deep links.** A URI-pattern DSL entry per destination, with per-platform handlers that parse incoming URIs and push the correct typed destination onto the back stack.

**Multiple independent back stacks.** Support for bottom-navigation tab patterns where each tab maintains its own back stack independently.

**Test utilities.** A `traverse-test` module provides `FakeTraverseNavigator` and a set of assertion extensions so navigation behaviour can be unit-tested without a running Compose host.

---

## Why Traverse over the alternatives?

| | compose-destinations | Voyager | Decompose | **Traverse** |
|---|---|---|---|---|
| KMP — Android + iOS + Desktop + Web | ❌ Android only | ✅ | ✅ | ✅ |
| No annotation processing / codegen | ❌ KSP required | ✅ | ✅ | ✅ |
| Built on navigation3 | ❌ | ❌ | ❌ | ✅ |
| Type-safe destinations | ✅ via codegen | Partial | Partial | ✅ native |
| Navigator is a testable interface | ❌ | ⚠️ | ⚠️ | ✅ |
| Actively maintained | ❌ Abandoned | ⚠️ Low activity | ✅ | ✅ |

---

## Modules

| Artifact | Purpose |
|---|---|
| `dev.teogor.traverse:traverse-core` | `Destination` interface, `TraverseNavigator`, back-stack extensions, navigation results — no Compose or Android dependency |
| `dev.teogor.traverse:traverse-compose` | `TraverseHost`, graph builder DSL, transitions, local navigator, back-gesture handling |
| `dev.teogor.traverse:traverse-test` | `FakeTraverseNavigator` and assertion helpers for unit tests |

---

## Progress

Full details and per-milestone file plan live in [`docs/PLAN.md`](../../blob/develop/docs/PLAN.md) on the `develop` branch.

| Milestone | Status |
|---|---|
| M0 — Scaffold & documentation | ✅ Done |
| M1 — Gradle skeleton, build-logic, all modules compile | ✅ Done |
| M2 — `traverse-core` source files | 🔄 In progress |
| M3 — `traverse-compose` (TraverseHost, DSL, transitions) | ⏳ Planned |
| M4 — `traverse-test` utilities | ⏳ Planned |
| M5 — Demo app (all platforms) | ⏳ Planned |
| M6 — Deep links | ⏳ Planned |
| M7 — Multiple back stacks / tab navigation | ⏳ Planned |
| M8 — Maven Central publication (`1.0.0-alpha01`) | ⏳ Planned |

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
