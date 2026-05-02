import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("traverse.kmp.application")
    id("traverse.compose")
    alias(libs.plugins.jetbrains.compose.hot.reload)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.jetbrains.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
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
