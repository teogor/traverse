package dev.teogor.traverse.compose.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Configures animated transitions between navigation destinations.
 *
 * Pass to [TraverseHost] via the `transitions` parameter for host-wide defaults:
 * ```kotlin
 * TraverseHost(
 *     startDestination = Home,
 *     transitions = TraverseTransitionSpec.slideAndFade(),
 * ) { ... }
 * ```
 *
 * Or apply per-screen via the `transitionSpec` overload of `screen<T>()`:
 * ```kotlin
 * screen<Settings>(transitionSpec = TraverseTransitionSpec.scaleAndFade()) { ... }
 * ```
 *
 * Per-destination overrides always take precedence over the host-level spec.
 *
 * ## Built-in presets
 *
 * | Preset | Push feel | Pop feel |
 * |---|---|---|
 * | [fade] | Cross-fade | Cross-fade |
 * | [horizontalSlide] | Slides in from right | Slides back from left |
 * | [verticalSlide] | Slides up from bottom | Slides back down |
 * | [slideAndFade] | Material Shared Axis X — slide + fade | Reverse |
 * | [scaleAndFade] | Scale 92%→100% + fade | Reverse |
 * | [elevate] | Scale + slight upward drift + fade | Reverse |
 * | [none] | Instant cut | Instant cut |
 */
public data class TraverseTransitionSpec(
    /** Enter transition when a destination is pushed onto the stack. */
    val enterTransition: (() -> EnterTransition)? = null,
    /** Exit transition when a destination is replaced (outgoing while another pushes in). */
    val exitTransition: (() -> ExitTransition)? = null,
    /** Enter transition when returning to a destination (back navigation). */
    val popEnterTransition: (() -> EnterTransition)? = null,
    /** Exit transition when popping a destination off the stack. */
    val popExitTransition: (() -> ExitTransition)? = null,
) {
    public companion object {

        // ── Cross-fade ─────────────────────────────────────────────────────────

        /**
         * Cross-fade — destinations fade in/out.
         * Universal and subtle; works well as a host-level default alongside specific per-screen overrides.
         * @param durationMillis Duration of both the enter and exit fades.
         */
        public fun fade(durationMillis: Int = 300): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
            popEnterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
        )

        // ── Horizontal Slide ───────────────────────────────────────────────────

        /**
         * Horizontal slide — classic iOS/Android navigation feel.
         * Push: new screen slides in from the right; current screen exits to the left.
         * Pop: current screen slides out to the right; previous screen enters from the left.
         * @param durationMillis Duration of the slide animation.
         */
        public fun horizontalSlide(durationMillis: Int = 300): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { slideInHorizontally(animationSpec = tween(durationMillis)) { it } },
            exitTransition = { slideOutHorizontally(animationSpec = tween(durationMillis)) { -it } },
            popEnterTransition = { slideInHorizontally(animationSpec = tween(durationMillis)) { -it } },
            popExitTransition = { slideOutHorizontally(animationSpec = tween(durationMillis)) { it } },
        )

        // ── Vertical Slide ─────────────────────────────────────────────────────

        /**
         * Vertical slide — modal card style.
         * Push: new screen slides up from the bottom; current screen scales back slightly and fades.
         * Pop: top screen slides back down; previous screen scales up and fades in.
         *
         * Ideal for modal flows, photo viewers, or wizard steps that feel "stacked above" the caller.
         * @param durationMillis Duration of the slide animation.
         */
        public fun verticalSlide(durationMillis: Int = 350): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = {
                slideInVertically(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it }
            },
            exitTransition = {
                scaleOut(animationSpec = tween(durationMillis), targetScale = 0.95f) +
                    fadeOut(animationSpec = tween(durationMillis / 2))
            },
            popEnterTransition = {
                scaleIn(animationSpec = tween(durationMillis), initialScale = 0.95f) +
                    fadeIn(animationSpec = tween(durationMillis / 2))
            },
            popExitTransition = {
                slideOutVertically(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it }
            },
        )

        // ── Slide and Fade (Material Shared Axis X) ────────────────────────────

        /**
         * Slide and Fade — Material Motion "Shared Axis X" pattern.
         * Destinations slide a fraction (1/5 of width) horizontally while fading in/out.
         * This creates a subtle sense of directionality without the full-width slide of [horizontalSlide].
         *
         * Recommended as a default for most apps — polished, not distracting.
         * @param durationMillis Duration of the combined slide+fade.
         */
        public fun slideAndFade(durationMillis: Int = 350): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = {
                slideInHorizontally(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it / 5 } +
                    fadeIn(animationSpec = tween(durationMillis * 3 / 5))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { -it / 5 } +
                    fadeOut(animationSpec = tween(durationMillis * 2 / 5))
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { -it / 5 } +
                    fadeIn(animationSpec = tween(durationMillis * 3 / 5))
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it / 5 } +
                    fadeOut(animationSpec = tween(durationMillis * 2 / 5))
            },
        )

        // ── Scale and Fade ─────────────────────────────────────────────────────

        /**
         * Scale and Fade — Material 3 "Container Transform" feel.
         * Push: new screen grows from 92% scale while fading in; current screen expands to 108% and fades out.
         * Pop: reverse — creates a "zoom into" and "zoom out of" sensation.
         *
         * Great for drill-down navigation (list → detail) where the destination feels "zoomed into".
         * @param durationMillis Duration of the combined scale+fade.
         */
        public fun scaleAndFade(durationMillis: Int = 300): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = {
                scaleIn(animationSpec = tween(durationMillis), initialScale = 0.92f) +
                    fadeIn(animationSpec = tween(durationMillis))
            },
            exitTransition = {
                scaleOut(animationSpec = tween(durationMillis), targetScale = 1.08f) +
                    fadeOut(animationSpec = tween(durationMillis * 2 / 3))
            },
            popEnterTransition = {
                scaleIn(animationSpec = tween(durationMillis), initialScale = 1.08f) +
                    fadeIn(animationSpec = tween(durationMillis))
            },
            popExitTransition = {
                scaleOut(animationSpec = tween(durationMillis), targetScale = 0.92f) +
                    fadeOut(animationSpec = tween(durationMillis * 2 / 3))
            },
        )

        // ── Elevate ────────────────────────────────────────────────────────────

        /**
         * Elevate — depth-based card push (iOS sheet / Material elevation feel).
         * Push: new screen grows from 94% scale with a subtle upward drift and fade in;
         *       current screen scales back slightly and fades to suggest it "goes behind".
         * Pop: reverse — the dismissed screen drifts back down as the previous screen rises.
         *
         * Ideal for settings panels, profile overlays, or any screen that feels "on top" of another.
         * @param durationMillis Duration of the animation.
         */
        public fun elevate(durationMillis: Int = 350): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = {
                scaleIn(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing), initialScale = 0.94f) +
                    slideInVertically(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it / 8 } +
                    fadeIn(animationSpec = tween(durationMillis / 2))
            },
            exitTransition = {
                scaleOut(animationSpec = tween(durationMillis), targetScale = 0.96f) +
                    fadeOut(animationSpec = tween(durationMillis * 2 / 3))
            },
            popEnterTransition = {
                scaleIn(animationSpec = tween(durationMillis), initialScale = 0.96f) +
                    fadeIn(animationSpec = tween(durationMillis * 2 / 3))
            },
            popExitTransition = {
                scaleOut(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing), targetScale = 0.94f) +
                    slideOutVertically(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) { it / 8 } +
                    fadeOut(animationSpec = tween(durationMillis / 2))
            },
        )

        // ── None ───────────────────────────────────────────────────────────────

        /** No animation — instant cut between destinations. */
        public fun none(): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        )
    }
}

