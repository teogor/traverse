plugins {
    id("traverse.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Expose traverse-core as API — consumers of traverse-test get full navigator access
            api(projects.traverseCore)
            // kotlin.test provides assertTrue / assertEquals used in assertion extensions
            implementation(kotlin("test"))
            // Coroutines for MutableSharedFlow used in result observation
            implementation(libs.jetbrains.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "dev.teogor.traverse.test"
}

