import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("traverse.kmp.application")
    id("traverse.compose")
    alias(libs.plugins.jetbrains.compose.hot.reload)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.jetbrains.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":traverse-core"))
            implementation(project(":traverse-compose"))
            implementation(project(":traverse-annotations"))
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.components.resources)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.androidx.lifecycle.runtime.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jetbrains.kotlinx.coroutines.swing)
        }
    }
}

// ── KSP: run the Traverse annotation processor ───────────────────────────────
// kspCommonMainMetadata: generates TraverseScreens (static object), TraverseAutoGraph, and
// navigator extensions into commonMain. No platform-specific KSP runs are needed because
// TraverseScreens is a plain Kotlin object — zero runtime registration, zero init calls.
dependencies {
    add("kspCommonMainMetadata", project(":traverse-ksp-processor"))
}

// Make every KMP compile task depend on kspCommonMainKotlinMetadata so that
// shared generated sources are ready before any target compilation starts.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Add the KSP-generated commonMain sources to the commonMain source set.
// This makes TraverseAutoGraph.kt, TraverseScreens.kt, etc. visible to all targets.
kotlin.sourceSets.getByName("commonMain") {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

android {
    namespace = "dev.teogor.traverse.demo"

    defaultConfig {
        applicationId = "dev.teogor.traverse.demo"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    debugImplementation(libs.jetbrains.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "dev.teogor.traverse.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.teogor.traverse.demo"
            packageVersion = "1.0.0"
        }
    }
}

compose.resources {
    packageOfResClass = "dev.teogor.traverse.demo.generated.resources"
}
