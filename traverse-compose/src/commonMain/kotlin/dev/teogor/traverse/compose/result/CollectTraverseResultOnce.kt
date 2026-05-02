package dev.teogor.traverse.compose.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator

/**
 * Collects a navigation result exactly once.
 *
 * Observes the result store of the current navigator. When a result with [key] arrives,
 * [onResult] is invoked **once** and the result is immediately cleared to prevent
 * re-delivery on recomposition.
 *
 * Place this call in the **consumer** destination — the screen that opened the producer:
 * ```kotlin
 * screen<Home> {
 *     CollectTraverseResultOnce<String>(RESULT_NEW_ENTRY_TITLE) { title ->
 *         viewModel.addEntry(title)
 *     }
 *     HomeScreen(...)
 * }
 * ```
 *
 * @param T   Expected type of the result value.
 * @param key The result key — must match the key used by the producer.
 * @param onResult Called exactly once when a result arrives.
 */
@Composable
public inline fun <reified T> CollectTraverseResultOnce(
    key: String,
    crossinline onResult: (T) -> Unit,
) {
    val navigator = LocalTraverseNavigator.current
    LaunchedEffect(key) {
        navigator.observeResult<T>(key).collect { value ->
            onResult(value)
            navigator.clearResult(key)
        }
    }
}

