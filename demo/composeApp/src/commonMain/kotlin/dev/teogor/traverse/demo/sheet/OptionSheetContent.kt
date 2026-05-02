package dev.teogor.traverse.demo.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val OPTIONS = listOf(
    "🌍" to "Feature: Multiplatform",
    "🔀" to "Feature: Nested Graphs",
    "⚡" to "Feature: Zero Codegen",
    "🎯" to "Feature: Type-Safe Routes",
    "📬" to "Feature: Navigation Results",
    "🔧" to "Feature: Extensible",
)

/**
 * Bottom-sheet content for the `bottomSheet<T>` showcase.
 *
 * Rendered inside a [ModalBottomSheet][androidx.compose.material3.ModalBottomSheet] by
 * [TraverseHost][dev.teogor.traverse.compose.TraverseHost].
 */
@Composable
fun OptionSheetContent(
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = "Pick an option",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        HorizontalDivider()
        OPTIONS.forEach { (emoji, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(label) }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.padding(horizontal = 12.dp))
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
        }
        Spacer(Modifier.height(8.dp))
    }
}

