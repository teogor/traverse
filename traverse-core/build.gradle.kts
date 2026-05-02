plugins {
    id("traverse.kmp.library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.kotlinx.serialization.core)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "dev.teogor.traverse.core"
}
