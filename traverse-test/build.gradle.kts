plugins {
    id("traverse.kmp.library")
    id("traverse.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Expose traverse-core + traverse-compose as API — consumers of traverse-test get
            // full navigator access and can host TraverseHost inside their compose tests.
            api(projects.traverseCore)
            api(projects.traverseCompose)
            // Compose runtime needed to use @Composable in this module (compose.runtime is
            // 'implementation' in traverse-compose so it is not re-exported transitively).
            implementation(libs.jetbrains.compose.runtime)
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

