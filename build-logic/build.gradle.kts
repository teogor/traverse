plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("traverseKmpLibrary") {
            id = "traverse.kmp.library"
            implementationClass = "TraverseKmpLibraryPlugin"
        }
        register("traverseCompose") {
            id = "traverse.compose"
            implementationClass = "TraverseComposePlugin"
        }
        register("traverseKmpApplication") {
            id = "traverse.kmp.application"
            implementationClass = "TraverseKmpApplicationPlugin"
        }
    }
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.jetbrains.kotlin.gradle.plugin)
    implementation(libs.jetbrains.kotlin.compose.gradle.plugin)
    implementation(libs.jetbrains.compose.gradle.plugin)
}
