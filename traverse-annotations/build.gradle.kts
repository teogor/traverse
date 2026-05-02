plugins {
    id("traverse.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Zero external dependencies — stdlib only (KClass, etc.)
        }
    }
}

android {
    namespace = "dev.teogor.traverse.annotations"
}

