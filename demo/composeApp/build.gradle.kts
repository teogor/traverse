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
// kspCommonMainMetadata: generates into the commonMain shared source dir (iOS, JS, wasmJS + tooling).
// kspAndroid / kspJvm: platform-specific runs so each entry point (MainActivity.kt, main.kt)
// can call initTraverseScreenRegistry() from its own generated source set.
dependencies {
    add("kspCommonMainMetadata", project(":traverse-ksp-processor"))
    add("kspAndroid",            project(":traverse-ksp-processor"))
    add("kspJvm",                project(":traverse-ksp-processor"))
}

// Make every KMP compile task depend on kspCommonMainKotlinMetadata so that
// shared generated sources are ready before any target compilation starts.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Explicitly wire platform-specific KSP tasks after commonMain KSP to satisfy
// Gradle's task-ordering validation (implicit dependency warning).
tasks.matching { it.name.startsWith("ksp") && (it.name.contains("KotlinAndroid") || it.name.contains("KotlinJvm")) }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

// Add the KSP-generated commonMain sources to the commonMain source set.
// This makes TraverseAutoGraph.kt, TraverseScreenRegistry.kt, etc. visible to all targets.
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
