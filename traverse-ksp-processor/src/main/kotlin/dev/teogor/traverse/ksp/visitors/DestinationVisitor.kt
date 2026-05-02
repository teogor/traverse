package dev.teogor.traverse.ksp.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import dev.teogor.traverse.ksp.model.DestinationModel
import dev.teogor.traverse.ksp.model.DestinationParameter
import dev.teogor.traverse.ksp.model.DestinationType
import dev.teogor.traverse.ksp.model.ScreenMetaInfo
import dev.teogor.traverse.ksp.model.TransitionInfo

// Annotation fully-qualified names — accessed as strings to avoid a compile-time dependency
// on traverse-annotations from the processor module.
private const val ANN_TRAVERSE_SCREEN = "dev.teogor.traverse.annotations.TraverseScreen"
private const val ANN_TRAVERSE_DIALOG = "dev.teogor.traverse.annotations.TraverseDialog"
private const val ANN_TRAVERSE_BOTTOM_SHEET = "dev.teogor.traverse.annotations.TraverseBottomSheet"
private const val ANN_TRAVERSE_ROOT = "dev.teogor.traverse.annotations.TraverseRoot"
private const val ANN_DEEP_LINK = "dev.teogor.traverse.annotations.DeepLink"
private const val ANN_TRANSITION = "dev.teogor.traverse.annotations.Transition"
private const val ANN_SCREEN_META = "dev.teogor.traverse.annotations.ScreenMeta"

/**
 * KSP visitor that extracts a [DestinationModel] from a class declaration annotated with one of
 * the Traverse destination annotations.
 *
 * Usage:
 * ```kotlin
 * val visitor = DestinationVisitor(logger)
 * classDeclaration.accept(visitor, Unit)
 * val model: DestinationModel? = visitor.model
 * ```
 */
public class DestinationVisitor(private val logger: KSPLogger) : KSVisitorVoid() {

    /** The extracted model, or `null` if the visited class has no recognised Traverse annotation. */
    public var model: DestinationModel? = null

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotations = classDeclaration.annotations.toList()
        val annotationQNames = annotations.map { it.annotationType.resolve().declaration.qualifiedName?.asString() }

        // Determine destination type from annotation presence
        val type: DestinationType = when {
            ANN_TRAVERSE_SCREEN in annotationQNames -> DestinationType.SCREEN
            ANN_TRAVERSE_DIALOG in annotationQNames -> DestinationType.DIALOG
            ANN_TRAVERSE_BOTTOM_SHEET in annotationQNames -> DestinationType.BOTTOM_SHEET
            else -> return // not a Traverse destination
        }

        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        val isObject = classDeclaration.classKind == ClassKind.OBJECT

        // ── Constructor parameters ─────────────────────────────────────────────
        val parameters = if (isObject) emptyList() else {
            classDeclaration.primaryConstructor?.parameters
                ?.map { param ->
                    val resolvedType = param.type.resolve()
                    val typeDecl = resolvedType.declaration
                    val typePkg = typeDecl.packageName.asString()
                    val typeName = typeDecl.simpleName.asString()
                    // Omit package prefix for kotlin.* and kotlin.collections.* types for readability
                    val shortTypeName = when {
                        typePkg == "kotlin" -> typeName
                        typePkg == "kotlin.collections" -> typeName
                        typePkg.isEmpty() -> typeName
                        else -> "$typePkg.$typeName"
                    }
                    DestinationParameter(
                        name = param.name?.asString() ?: "_",
                        typeName = shortTypeName,
                        isNullable = resolvedType.nullability == Nullability.NULLABLE,
                        hasDefault = param.hasDefault,
                    )
                } ?: emptyList()
        }

        // ── Deep links ────────────────────────────────────────────────────────
        // @DeepLink is @Repeatable — KSP may represent repeated applications either as
        // individual annotations or wrapped in the compiler-generated container. We handle both.
        val deepLinkPatterns = buildDeepLinkPatterns(annotations)

        // ── @Transition ───────────────────────────────────────────────────────
        val transitionInfo = annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANN_TRANSITION }
            ?.let { ann -> buildTransitionInfo(ann) }

        // ── @ScreenMeta ───────────────────────────────────────────────────────
        val metaInfo = annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANN_SCREEN_META }
            ?.let { ann ->
                val args = ann.arguments.associate { it.name?.asString() to it.value }
                ScreenMetaInfo(
                    name = args["name"] as? String ?: "",
                    description = args["description"] as? String ?: "",
                    group = args["group"] as? String ?: "",
                )
            }

        // ── @TraverseRoot ─────────────────────────────────────────────────────
        val isRoot = ANN_TRAVERSE_ROOT in annotationQNames

        // ── Dialog-specific fields ────────────────────────────────────────────
        var dismissOnBackPress = true
        var dismissOnClickOutside = true
        if (type == DestinationType.DIALOG) {
            annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANN_TRAVERSE_DIALOG }
                ?.let { ann ->
                    val args = ann.arguments.associate { it.name?.asString() to it.value }
                    dismissOnBackPress = args["dismissOnBackPress"] as? Boolean ?: true
                    dismissOnClickOutside = args["dismissOnClickOutside"] as? Boolean ?: true
                }
        }

        // ── BottomSheet-specific fields ───────────────────────────────────────
        var skipPartiallyExpanded = false
        if (type == DestinationType.BOTTOM_SHEET) {
            annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANN_TRAVERSE_BOTTOM_SHEET }
                ?.let { ann ->
                    val args = ann.arguments.associate { it.name?.asString() to it.value }
                    skipPartiallyExpanded = args["skipPartiallyExpanded"] as? Boolean ?: false
                }
        }

        model = DestinationModel(
            className = className,
            packageName = packageName,
            type = type,
            isObject = isObject,
            parameters = parameters,
            deepLinkPatterns = deepLinkPatterns,
            transitionInfo = transitionInfo,
            meta = metaInfo,
            isRoot = isRoot,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun buildDeepLinkPatterns(annotations: List<KSAnnotation>): List<String> {
        val patterns = mutableListOf<String>()
        for (ann in annotations) {
            val qName = ann.annotationType.resolve().declaration.qualifiedName?.asString()
            when (qName) {
                ANN_DEEP_LINK -> {
                    // Single application
                    val pattern = ann.arguments.firstOrNull { it.name?.asString() == "pattern" }?.value as? String
                    if (pattern != null) patterns += pattern
                }
                // Compiler-generated container for @Repeatable (qualified name ends in "s")
                "${ANN_DEEP_LINK}s", "dev.teogor.traverse.annotations.DeepLinks" -> {
                    @Suppress("UNCHECKED_CAST")
                    val inner = ann.arguments.firstOrNull { it.name?.asString() == "value" }?.value as? List<*>
                    inner?.forEach { innerAnn ->
                        val ksAnn = innerAnn as? KSAnnotation
                        val pattern = ksAnn?.arguments?.firstOrNull { it.name?.asString() == "pattern" }?.value as? String
                        if (pattern != null) patterns += pattern
                    }
                }
            }
        }
        return patterns
    }

    private fun buildTransitionInfo(ann: KSAnnotation): TransitionInfo {
        val args = ann.arguments.associate { it.name?.asString() to it.value }
        // Enum values in KSP annotation arguments are resolved as KSType
        val presetName = (args["preset"] as? KSType)?.declaration?.simpleName?.asString() ?: "FADE"
        val duration = (args["durationMillis"] as? Int) ?: 300
        return TransitionInfo(preset = presetName, durationMillis = duration)
    }
}

