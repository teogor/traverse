package dev.teogor.traverse.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import dev.teogor.traverse.ksp.generators.GraphSpecGenerator
import dev.teogor.traverse.ksp.generators.NavigatorExtensionGenerator
import dev.teogor.traverse.ksp.generators.RouteObjectGenerator
import dev.teogor.traverse.ksp.model.DestinationModel
import dev.teogor.traverse.ksp.visitors.DestinationVisitor

// Annotation names to look up via KSP resolver
private val DESTINATION_ANNOTATION_NAMES = listOf(
    "dev.teogor.traverse.annotations.TraverseScreen",
    "dev.teogor.traverse.annotations.TraverseDialog",
    "dev.teogor.traverse.annotations.TraverseBottomSheet",
)

/**
 * Main KSP [SymbolProcessor] for Traverse.
 *
 * **Processing rounds:**
 * 1. `process()` is called once per compilation round. It collects all classes annotated with
 *    `@TraverseScreen`, `@TraverseDialog`, or `@TraverseBottomSheet` and turns each into a
 *    [DestinationModel] via [DestinationVisitor].
 * 2. `finish()` is called after all rounds complete. It invokes the three generators:
 *    - [RouteObjectGenerator] — one `{Class}Route.kt` per destination with deep links.
 *    - [NavigatorExtensionGenerator] — one `{Class}NavigatorExtensions.kt` per parameterised destination.
 *    - [GraphSpecGenerator] — one `TraverseAutoGraph.kt` + `TraverseScreenRegistry.kt` for the module.
 *
 * Deferred symbols (not yet validated after one round) are returned from `process()` and
 * retried in subsequent rounds — the standard KSP incremental processing contract.
 */
public class TraverseSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val collectedModels = mutableListOf<DestinationModel>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferred = mutableListOf<KSAnnotated>()

        for (annotationName in DESTINATION_ANNOTATION_NAMES) {
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            symbols.forEach { symbol ->
                if (!symbol.validate()) {
                    deferred += symbol
                } else if (symbol is KSClassDeclaration) {
                    val visitor = DestinationVisitor(logger)
                    symbol.accept(visitor, Unit)
                    visitor.model?.let { model ->
                        // Deduplicate: a class annotated with multiple Traverse annotations
                        // (shouldn't happen, but guard anyway) should only be processed once.
                        if (collectedModels.none { it.qualifiedName == model.qualifiedName }) {
                            collectedModels += model
                        }
                    }
                } else {
                    logger.warn("traverse-ksp: @TraverseScreen / @TraverseDialog / @TraverseBottomSheet applied to non-class symbol: ${symbol.javaClass.simpleName}")
                }
            }
        }

        return deferred
    }

    override fun finish() {
        if (collectedModels.isEmpty()) {
            logger.info("traverse-ksp: no annotated destinations found — skipping code generation")
            return
        }

        logger.info("traverse-ksp: generating code for ${collectedModels.size} destination(s)")

        val routeGen = RouteObjectGenerator(codeGenerator, logger)
        val navExtGen = NavigatorExtensionGenerator(codeGenerator, logger)
        val graphGen = GraphSpecGenerator(codeGenerator, logger)

        collectedModels.forEach { model ->
            routeGen.generate(model)
            navExtGen.generate(model)
        }
        graphGen.generate(collectedModels)
    }
}

