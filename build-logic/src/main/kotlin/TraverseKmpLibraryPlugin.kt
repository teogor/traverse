import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for all KMP library modules (traverse-core, traverse-compose, etc.).
 *
 * Plugin ID: `traverse.kmp.library`
 *
 * Applies:
 *  - `org.jetbrains.kotlin.multiplatform`
 *  - `com.android.library`
 *
 * Configures:
 *  - KMP targets: androidTarget (JVM 11), iosArm64, iosSimulatorArm64, jvm (JVM 11), js (browser), wasmJs (browser)
 *  - `explicitApi()` — every public declaration needs the `public` modifier
 *  - `commonTest` → `kotlin("test")` dependency
 *  - Android: compileSdk, minSdk, Java 11 compile options (sourced from version catalog)
 *
 * Each library module still sets its own `namespace` and module-specific dependencies.
 */
class TraverseKmpLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")
        configureKotlinMultiplatformLibrary()
        configureAndroidLibrary()
    }

    private fun Project.configureKotlinMultiplatformLibrary() {
        configure<KotlinMultiplatformExtension> {
            explicitApi()

            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }

            iosArm64()
            iosSimulatorArm64()

            jvm {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }

            js {
                browser()
            }

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
            }

            sourceSets.getByName("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }

    private fun Project.configureAndroidLibrary() {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        configure<LibraryExtension> {
            compileSdk = libs.findVersion("android-sdk-compile").get().requiredVersion.toInt()
            defaultConfig {
                minSdk = libs.findVersion("android-sdk-min").get().requiredVersion.toInt()
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}

