package dev.teogor.traverse.compose.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Configures animated transitions between navigation destinations.
 *
 * Pass to [TraverseHost] via the `transitions` parameter:
 * ```kotlin
 * TraverseHost(
 *     startDestination = Home,
 *     transitions = TraverseTransitionSpec.horizontalSlide(),
 * ) { ... }
 * ```
 *
 * Per-screen overrides are set in `screen<T>(enterTransition = ..., exitTransition = ...)`.
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

        /**
         * Cross-fade transition.
         * @param durationMillis Duration of both the enter and exit fades.
         */
        public fun fade(durationMillis: Int = 300): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
            popEnterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
        )

        /**
         * Horizontal slide — destinations slide in from the right on push, from the left on pop.
         * @param durationMillis Duration of the slide animation.
         */
        public fun horizontalSlide(durationMillis: Int = 300): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { slideInHorizontally(animationSpec = tween(durationMillis)) { it } },
            exitTransition = { slideOutHorizontally(animationSpec = tween(durationMillis)) { -it } },
            popEnterTransition = { slideInHorizontally(animationSpec = tween(durationMillis)) { -it } },
            popExitTransition = { slideOutHorizontally(animationSpec = tween(durationMillis)) { it } },
        )

        /** No animation — instant cut between destinations. */
        public fun none(): TraverseTransitionSpec = TraverseTransitionSpec(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        )
    }
}

