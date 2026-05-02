package dev.teogor.traverse.compose.navigator

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.teogor.traverse.core.navigator.TraverseNavigator

/**
 * [ProvidableCompositionLocal] providing the [TraverseNavigator] for the current [TraverseHost] scope.
 *
 * Access inside any composable within a [TraverseHost]:
 * ```kotlin
 * @Composable
 * fun HomeScreen() {
 *     val navigator = LocalTraverseNavigator.current
 *     Button(onClick = { navigator.navigate(Profile(userId = "42")) }) { ... }
 * }
 * ```
 *
 * Throws [IllegalStateException] if accessed outside a [TraverseHost].
 */
public val LocalTraverseNavigator: ProvidableCompositionLocal<TraverseNavigator> =
    compositionLocalOf { error("LocalTraverseNavigator accessed outside TraverseHost") }

