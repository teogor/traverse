import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for the demo application module.
 *
 * Applies:
 *  - org.jetbrains.kotlin.multiplatform
 *  - com.android.application
 *  - org.jetbrains.compose  (Compose Multiplatform)
 *  - org.jetbrains.kotlin.plugin.compose  (Compose compiler plugin)
 *
 * Configures:
 *  - KMP targets: androidTarget (JVM 11), iosArm64 + iosSimulatorArm64 (ComposeApp framework),
 *    jvm, js (browser + executable), wasmJs (browser + executable)
 *  - android: compileSdk, minSdk, targetSdk, packaging excludes, release build type, Java 11
 *
 * Module build file still sets: namespace, applicationId, versionCode/versionName,
 * compose.desktop, compose.resources, and all sourceSets dependencies.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
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


