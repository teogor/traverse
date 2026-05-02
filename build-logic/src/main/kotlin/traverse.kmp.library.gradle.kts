import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for all KMP library modules (traverse-core, traverse-compose, etc.).
 *
 * Applies:
 *  - org.jetbrains.kotlin.multiplatform
 *  - com.android.library
 *
 * Configures:
 *  - KMP targets: androidTarget (JVM 11), iosArm64, iosSimulatorArm64, jvm (JVM 11), wasmJs
 *  - explicitApi() — all public declarations must carry `public` modifier
 *  - commonTest: kotlin("test") dependency
 *  - android: compileSdk, minSdk, Java 11 compileOptions (from version catalog)
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

kotlin {
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    compileSdk = libs.findVersion("android-sdk-compile").get().requiredVersion.toInt()

    defaultConfig {
        minSdk = libs.findVersion("android-sdk-min").get().requiredVersion.toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


