import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Convention plugin for JVM-only library modules (e.g. KSP processors, annotation processors).
 *
 * Plugin ID: `traverse.jvm.library`
 *
 * Applies:
 *  - `org.jetbrains.kotlin.jvm`
 *
 * Configures:
 *  - JVM toolchain: Java 11
 *  - `explicitApi()` — every public declaration needs `public`
 *  - `compileTestKotlin` optionally inherits test dependencies via commonTest pattern
 *
 * KSP processor modules use this instead of `traverse.kmp.library` because they are
 * JVM-only Gradle tools that are never shipped to iOS / JS / wasmJS targets.
 */
class TraverseJvmLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        configure<KotlinJvmProjectExtension> {
            explicitApi()
            jvmToolchain(11)
        }

        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
}

