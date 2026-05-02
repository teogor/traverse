package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * Navigation-results demo host.
 *
 * Navigates to [ColorPickerScreen] which returns a color string via `setResult`.
 * Demonstrates `CollectTraverseResultOnce` on the consumer side.
 */
@Composable
fun ResultDemoScreen(
    pickedColor: String?,
    onPickColor: () -> Unit,
) {
    ShowcaseScaffold(
        title = "Navigation Results",
        apiBadge = "setResult",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Result received:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (pickedColor != null) "🎨  $pickedColor" else "—  none yet",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (pickedColor != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CodeSnippet(
                code = """// Consumer (this screen):
var picked by remember { mutableStateOf<String?>(null) }
CollectTraverseResultOnce<String>(RESULT_COLOR) {
    picked = it
}

// Producer (ColorPicker screen):
nav.setResult(RESULT_COLOR, colorName)
nav.navigateUp()""",
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPickColor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pick a color  →")
            }

            if (pickedColor != null) {
                Text(
                    text = "✓  Result delivered and cleared automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

