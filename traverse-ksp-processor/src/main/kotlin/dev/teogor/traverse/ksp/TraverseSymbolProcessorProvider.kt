package dev.teogor.traverse.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Service provider entry point for the Traverse KSP processor.
 *
 * Registered via `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
 * so that Gradle's KSP plugin discovers and instantiates it automatically when a consumer
 * project applies `ksp(project(":traverse-ksp-processor"))`.
 */
public class TraverseSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        TraverseSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
}

