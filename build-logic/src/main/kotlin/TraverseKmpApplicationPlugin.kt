import com.android.build.api.dsl.ApplicationExtension
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
 * Convention plugin for the demo application module.
 *
 * Plugin ID: `traverse.kmp.application`
 *
 * Applies:
 *  - `org.jetbrains.kotlin.multiplatform`
 *  - `com.android.application`
 *
 * Configures:
 *  - KMP targets: androidTarget (JVM 11), iosArm64 + iosSimulatorArm64 (ComposeApp framework,
 *    static), jvm, js (browser + executable), wasmJs (browser + executable)
 *  - `commonTest` → `kotlin("test")` dependency
 *  - Android: compileSdk, minSdk, targetSdk, packaging excludes, release build type, Java 11
 *
 * The module build file still sets: `namespace`, `applicationId`, `versionCode`/`versionName`,
 * `compose.desktop`, `compose.resources`, and all `sourceSets` dependencies.
 *
 * Pair with `traverse.compose` to add Compose Multiplatform support.
 */
class TraverseKmpApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.application")
        configureKotlinMultiplatformApplication()
        configureAndroidApplication()
    }

    private fun Project.configureKotlinMultiplatformApplication() {
        configure<KotlinMultiplatformExtension> {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }

            listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }

            jvm()

            js {
                browser()
                binaries.executable()
            }

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
                binaries.executable()
            }

            sourceSets.getByName("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }

    private fun Project.configureAndroidApplication() {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        configure<ApplicationExtension> {
            compileSdk = libs.findVersion("android-sdk-compile").get().requiredVersion.toInt()
            defaultConfig {
                minSdk = libs.findVersion("android-sdk-min").get().requiredVersion.toInt()
                targetSdk = libs.findVersion("android-sdk-target").get().requiredVersion.toInt()
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}

