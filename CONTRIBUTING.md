# Contributing to Traverse

Thank you for your interest in contributing!
This document covers the project structure, coding conventions, and the process for submitting changes.

---

## Project layout

```
traverse/
├── traverse-core/          # Destination, TraverseNavigator, results — no Compose dep
├── traverse-compose/       # TraverseHost, DSL builder, LocalTraverseNavigator, transitions
├── traverse-test/          # FakeTraverseNavigator, assertion helpers
├── demo/
│   └── composeApp/         # Runnable demo covering all features
├── build-logic/            # Convention Gradle plugins
├── gradle/
│   └── libs.versions.toml  # Single version catalog for all deps
├── ARCHITECTURE.md         # All design decisions
├── ROADMAP.md              # Feature roadmap
└── TRAVERSE_MEMORY.md      # Agent working memory
```

---

## Coding conventions

### Visibility
- `explicitApi()` is enabled in **all** modules. Every public declaration must carry an explicit `public` modifier.
- If a declaration is only needed within the same module, mark it `internal`.
- All implementation classes / data structures used only inside a `@Composable` render path should be `internal`.

### Destinations
- All user-facing destination classes must implement `Destination` (from `traverse-core`).
- All destination classes must also be `@Serializable`.
- Prefer `data object` for argument-free destinations; `data class` for parameterized ones.

### DSL
- The `@TraverseDsl` annotation (`@DslMarker`) is applied to `TraverseGraphBuilder` and any nested builder. Always ensure DSL functions cannot be called from the wrong scope.
- Prefer `inline fun <reified T : Destination>` functions to avoid class literal boilerplate.

### Naming
- Prefix all public API with `Traverse` (e.g., `TraverseHost`, `TraverseNavigator`, `TraverseTransitionSpec`).
- Exception: `Destination` (not `TraverseDestination`) — it's the fundamental type.
- `Local*` CompositionLocals follow Compose convention: `LocalTraverseNavigator`.

### Formatting
- Kotlin official code style (4-space indent, no wildcard imports).
- Group imports: stdlib → kotlinx → compose → traverse-internal.

---

## Modules and cross-module rules

```
traverse-core    ──►  (nothing from this project)
traverse-compose ──►  traverse-core
traverse-test    ──►  traverse-core
demo             ──►  traverse-compose
```

**Strict rule:** `traverse-core` must never import from `traverse-compose`. Violations will fail the build.

---

## Branch strategy

| Branch pattern | Purpose |
|---|---|
| `main` | Stable, always compiles, always tested |
| `feature/*` | New features or modules |
| `fix/*` | Bug fixes |
| `chore/*` | Tooling, documentation, CI changes |

Always branch from `main`. PRs must target `main`.

---

## Commit conventions

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(core): add TraverseNavigator.canNavigateUp property
fix(compose): correct popTo inclusive=true behaviour
docs: update ARCHITECTURE.md nested navigation section
chore(deps): bump navigation3 to 1.0.0-alpha05
test(compose): add TraverseHost integration test for dialog destinations
```

**Types:** `feat`, `fix`, `docs`, `chore`, `test`, `refactor`, `perf`, `ci`

---

## Running the build

```bash
# Compile all modules (JVM target)
./gradlew compileKotlinJvm

# Run all tests
./gradlew test

# Run the demo on Desktop
./gradlew :demo:composeApp:run

# Check for explicitApi violations
./gradlew :traverse-core:compileKotlinJvm :traverse-compose:compileKotlinJvm
```

---

## Submitting a PR

1. Create a feature branch from `main`.
2. Make your changes — ensure `./gradlew test` passes.
3. Update `ARCHITECTURE.md` if you made an architectural decision.
4. Update `ROADMAP.md` — check off completed items, add new ones.
5. Add KDoc to all new public API.
6. Open a PR with a clear description referencing the roadmap item.

