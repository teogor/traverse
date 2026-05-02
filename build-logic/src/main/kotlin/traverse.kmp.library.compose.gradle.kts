/**
 * Convention plugin for KMP library modules that use Compose Multiplatform.
 *
 * Extends [traverse.kmp.library] and additionally applies:
 *  - org.jetbrains.compose  (Compose Multiplatform)
 *  - org.jetbrains.kotlin.plugin.compose  (Compose compiler plugin)
 */
plugins {
    id("traverse.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

