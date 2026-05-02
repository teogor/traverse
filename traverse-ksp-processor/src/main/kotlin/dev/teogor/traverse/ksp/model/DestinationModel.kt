package dev.teogor.traverse.ksp.model

/**
 * Type of a navigation destination as read from the annotation by the KSP visitor.
 */
public enum class DestinationType {
    SCREEN,
    DIALOG,
    BOTTOM_SHEET,
}

/**
 * Resolved transition information extracted from [@Transition][dev.teogor.traverse.annotations.Transition].
 *
 * @property preset Simple name of the [TransitionPreset][dev.teogor.traverse.annotations.TransitionPreset] enum constant (e.g. `"HORIZONTAL_SLIDE"`).
 * @property durationMillis Animation duration in milliseconds.
 */
public data class TransitionInfo(
    val preset: String,
    val durationMillis: Int,
)

/**
 * Resolved metadata extracted from [@ScreenMeta][dev.teogor.traverse.annotations.ScreenMeta].
 */
public data class ScreenMetaInfo(
    val name: String,
    val description: String,
    val group: String,
)

/**
 * A constructor parameter on a destination data class (used for `navigateTo*` extension generation).
 *
 * @property name  Parameter name (matches the destination's constructor parameter name).
 * @property typeName Fully-qualified type name (e.g. `"kotlin.String"`, `"kotlin.Int"`).
 * @property isNullable Whether the type is nullable (`?`).
 * @property hasDefault Whether the parameter has a default value (skipped in generated navigate call).
 */
public data class DestinationParameter(
    val name: String,
    val typeName: String,
    val isNullable: Boolean,
    val hasDefault: Boolean,
)

/**
 * Complete metadata for a single annotated destination class — the core model passed to all generators.
 *
 * @property className   Simple class name (e.g. `"Profile"`).
 * @property packageName Package name (e.g. `"com.example.app"`).
 * @property type        Destination type — SCREEN, DIALOG, or BOTTOM_SHEET.
 * @property isObject    `true` when the Kotlin declaration is a `data object` (no constructor params).
 * @property parameters  Constructor parameters (empty for `data object` destinations).
 * @property deepLinkPatterns URI patterns from [@DeepLink][dev.teogor.traverse.annotations.DeepLink].
 * @property transitionInfo  Resolved [@Transition][dev.teogor.traverse.annotations.Transition], or `null` if not present.
 * @property meta        Resolved [@ScreenMeta][dev.teogor.traverse.annotations.ScreenMeta], or `null` if not present.
 * @property isRoot      `true` if the destination carries [@TraverseRoot][dev.teogor.traverse.annotations.TraverseRoot].
 * @property dismissOnBackPress     Dialog-only: dismiss on system back press.
 * @property dismissOnClickOutside  Dialog-only: dismiss on click outside.
 * @property skipPartiallyExpanded  BottomSheet-only: skip partially-expanded state.
 */
public data class DestinationModel(
    val className: String,
    val packageName: String,
    val type: DestinationType,
    val isObject: Boolean,
    val parameters: List<DestinationParameter>,
    val deepLinkPatterns: List<String>,
    val transitionInfo: TransitionInfo?,
    val meta: ScreenMetaInfo?,
    val isRoot: Boolean,
    // Dialog-only
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    // BottomSheet-only
    val skipPartiallyExpanded: Boolean = false,
) {
    /** Fully-qualified class name (e.g. `"com.example.app.Profile"`). */
    val qualifiedName: String get() = if (packageName.isEmpty()) className else "$packageName.$className"

    /** `true` when the destination has at least one non-default constructor parameter. */
    val hasParameters: Boolean get() = parameters.any { !it.hasDefault }

    /** The display name: [ScreenMetaInfo.name] when non-blank, otherwise [className]. */
    val displayName: String get() = meta?.name?.takeIf { it.isNotBlank() } ?: className
}

