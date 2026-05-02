plugins {
    id("traverse.jvm.library")
}

dependencies {
    // KSP symbol processing API — the only required dependency for a KSP processor.
    // The processor accesses annotation types by their fully-qualified name string,
    // so traverse-annotations does NOT need to be on the classpath.
    implementation(libs.ksp.symbol.processing.api)
}

