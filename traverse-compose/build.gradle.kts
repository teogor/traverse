plugins {
    id("traverse.kmp.library")
    id("traverse.compose")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.traverseCore)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.androidx.navigation3.ui)
        }
    }
}

android {
    namespace = "dev.teogor.traverse.compose"
}
