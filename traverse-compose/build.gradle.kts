plugins {
    id("traverse.kmp.library")
    id("traverse.compose")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(projects.traverseCore)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.kotlinx.serialization.core)
            implementation(libs.jetbrains.kotlinx.serialization.json)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
            implementation(libs.jetbrains.androidx.lifecycle.runtime.compose)
        }
    }
}

android {
    namespace = "dev.teogor.traverse.compose"
}
