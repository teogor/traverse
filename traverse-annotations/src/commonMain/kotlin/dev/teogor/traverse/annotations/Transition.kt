package dev.teogor.traverse.annotations

/**
 * Built-in transition presets for [Transition].
 *
 * Every value corresponds to a `TraverseTransitionSpec` factory function in `traverse-compose`.
 * The `traverse-ksp-processor` maps these enum values to the appropriate factory call in the
 * generated `traverseAutoGraph()` registration.
 *
 * | Preset | Factory call | Visual effect |
 * |---|---|---|
 * | [FADE] | `TraverseTransitionSpec.fade()` | Cross-fade |
 * | [HORIZONTAL_SLIDE] | `TraverseTransitionSpec.horizontalSlide()` | Slide left/right |
 * | [VERTICAL_SLIDE] | `TraverseTransitionSpec.verticalSlide()` | Slide up/down (modal) |
 * | [SLIDE_AND_FADE] | `TraverseTransitionSpec.slideAndFade()` | Material Shared Axis X |
 * | [SCALE_AND_FADE] | `TraverseTransitionSpec.scaleAndFade()` | Zoom + cross-fade |
 * | [ELEVATE] | `TraverseTransitionSpec.elevate()` | Rise-up + fade |
 * | [NONE] | `TraverseTransitionSpec.none()` | Instant cut |
 */
public enum class TransitionPreset {
    /** Cross-fade transition. */
    FADE,

    /** Slide from right on push, slide to right on pop. */
    HORIZONTAL_SLIDE,

    /** Slide up from bottom on push, scale back slightly; reverse on pop. */
    VERTICAL_SLIDE,

    /** Slight horizontal slide + fade — Material Motion Shared Axis X. */
    SLIDE_AND_FADE,

    /** Scale from 92%→100% + fade — zoom into destination. */
    SCALE_AND_FADE,

    /**
     * Rise from 94% scale with upward drift + fade — floating panel feel.
     * Recommended for settings panels, detail views, and contextual overlays.
     */
    ELEVATE,

    /** Instant cut — no animation whatsoever. */
    NONE,
}

/**
 * Specifies the enter/exit transition preset for a [TraverseScreen] destination.
 *
 * The `preset` applies to all four transition directions (enter, exit, popEnter, popExit).
 * For asymmetric animations, use the manual DSL with per-direction lambda overrides in
 * [TraverseGraphBuilder.screen][dev.teogor.traverse.compose.graph.TraverseGraphBuilder.screen].
 *
 * ## Usage
 * ```kotlin
 * // Use horizontal slide when navigating to and from this screen
 * @TraverseScreen
 * @Transition(TransitionPreset.HORIZONTAL_SLIDE)
 * @Serializable
 * data class Profile(val userId: String) : Destination
 *
 * // Custom duration
 * @TraverseScreen
 * @Transition(TransitionPreset.SCALE_AND_FADE, durationMillis = 450)
 * @Serializable
 * data object Settings : Destination
 * ```
 *
 * @param preset The transition animation to apply. Defaults to [TransitionPreset.FADE].
 * @param durationMillis Animation duration in milliseconds. Defaults to `300`.
 *
 * @see TraverseScreen
 * @see TransitionPreset
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class Transition(
    val preset: TransitionPreset = TransitionPreset.FADE,
    val durationMillis: Int = 300,
)

