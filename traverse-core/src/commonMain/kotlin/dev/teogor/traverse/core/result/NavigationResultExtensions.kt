package dev.teogor.traverse.core.result

import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.TraverseNavigator

/**
 * Set a result identified by [key] with [value], then pop the top back-stack entry.
 *
 * The preceding destination should collect it via
 * [CollectTraverseResultOnce][dev.teogor.traverse.compose.result.CollectTraverseResultOnce].
 *
 * ```kotlin
 * // Producer (e.g. ColorPickerScreen):
 * navigator.setResultAndNavigateUp(key = "picked_color", value = "red")
 *
 * // Consumer (e.g. the screen that opened the picker):
 * CollectTraverseResultOnce<String>("picked_color") { color -> applyColor(color) }
 * ```
 *
 * @param key   Unique string identifier for this result. Must match the key used by the consumer.
 * @param value The result value. Must be serializable if it will cross process death boundaries.
 */
public fun <T> TraverseNavigator.setResultAndNavigateUp(key: String, value: T) {
    setResult(key, value)
    navigateUp()
}

/**
 * Set a result identified by [key] with [value], then pop the back stack to [destination].
 *
 * ```kotlin
 * navigator.setResultAndPopTo(
 *     key = "picked_color",
 *     value = "red",
 *     destination = Home,
 *     inclusive = false,
 * )
 * ```
 *
 * @param key         Unique string identifier for this result.
 * @param value       The result value.
 * @param destination The destination to pop back to.
 * @param inclusive   If `true`, [destination] itself is also popped.
 */
public fun <T> TraverseNavigator.setResultAndPopTo(
    key: String,
    value: T,
    destination: Destination,
    inclusive: Boolean = false,
) {
    setResult(key, value)
    popTo(destination, inclusive)
}

