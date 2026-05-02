package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * Bottom-sheet demo host.
 *
 * Navigates to [OptionSheet][dev.teogor.traverse.demo.OptionSheet] and
 * collects the selected option string via result.
 */
@Composable
fun SheetDemoScreen(
    pickedOption: String?,
    onOpenSheet: () -> Unit,
) {
    ShowcaseScaffold(
        title = "Bottom Sheet Destination",
        apiBadge = "bottomSheet<T>",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "A bottomSheet<T> destination renders as a ModalBottomSheet overlay. " +
                    "It sits on top of the current screen. Dismissing it calls navigateUp().",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CodeSnippet(
                code = """// Register in TraverseHost:
bottomSheet<OptionSheet> {
    OptionSheetContent(
        onOptionSelected = { option ->
            nav.setResult(RESULT_OPTION, option)
            nav.navigateUp()
        },
        onDismiss = { nav.navigateUp() },
    )
}

// Navigate to it:
nav.navigate(OptionSheet)""",
            )

            HorizontalDivider()

            Text(
                text = "Selected option:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (pickedOption != null) "✓  $pickedOption" else "—  None yet",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (pickedOption != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onOpenSheet,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open Bottom Sheet  →")
            }
        }
    }
}

