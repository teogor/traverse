import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin that layers Compose Multiplatform on top of any KMP module.
 *
 * Plugin ID: `traverse.compose`
 *
 * Applies:
 *  - `org.jetbrains.compose`         (Compose Multiplatform Gradle plugin)
 *  - `org.jetbrains.kotlin.plugin.compose`  (Kotlin Compose compiler plugin)
 *
 * Intentionally has no KMP, no Android, no targets — those come from
 * `traverse.kmp.library` or `traverse.kmp.application`. This plugin is purely
 * the Compose layer and can be stacked on top of any KMP convention plugin.
 */
class TraverseComposePlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }
}

