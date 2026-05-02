package dev.teogor.traverse.demo.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator
import dev.teogor.traverse.core.Destination

/**
 * Persistent footer that shows the live back stack as breadcrumb chips.
 *
 * Reads [LocalTraverseNavigator.current.backStack] — since the underlying
 * implementation is a [SnapshotStateList][androidx.compose.runtime.snapshots.SnapshotStateList],
 * this composable recomposes automatically on every push/pop.
 */
@Composable
fun BackStackBar() {
    val navigator = LocalTraverseNavigator.current
    val stack = navigator.backStack

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Back Stack  (${stack.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                stack.forEachIndexed { idx, dest ->
                    val isTop = idx == stack.lastIndex
                    StackEntryChip(label = dest.displayName(), isTop = isTop)
                    if (idx < stack.lastIndex) {
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StackEntryChip(label: String, isTop: Boolean) {
    val containerColor = if (isTop)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isTop)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 1,
        )
    }
}

/** Returns the simple class name used in back-stack breadcrumbs. */
fun Destination.displayName(): String = this::class.simpleName ?: "?"

