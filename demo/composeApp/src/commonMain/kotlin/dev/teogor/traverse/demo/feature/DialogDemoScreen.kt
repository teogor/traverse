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
 * Dialog demo host.
 *
 * Navigates to [ShowcaseDialog][dev.teogor.traverse.demo.ShowcaseDialog] and
 * collects a Boolean result — confirmed or dismissed.
 */
@Composable
fun DialogDemoScreen(
    lastResult: Boolean?,
    onOpenDialog: () -> Unit,
) {
    ShowcaseScaffold(
        title = "Dialog Destination",
        apiBadge = "dialog<T>",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "A dialog<T> destination renders as a composable Dialog overlay " +
                    "on top of the current screen, without replacing it in AnimatedContent.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CodeSnippet(
                code = """// Register in TraverseHost:
dialog<ShowcaseDialog> { dest ->
    ShowcaseDialogContent(
        message = dest.message,
        onConfirm = {
            nav.setResult(RESULT_DIALOG_CONFIRMED, true)
            nav.navigateUp()
        },
        onDismiss = {
            nav.setResult(RESULT_DIALOG_CONFIRMED, false)
            nav.navigateUp()
        },
    )
}

// Navigate to it:
nav.navigate(ShowcaseDialog("Ready to confirm?"))""",
            )

            HorizontalDivider()

            Text(
                text = "Last result:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = when (lastResult) {
                        true -> "✅  Confirmed"
                        false -> "❌  Dismissed"
                        null -> "—  No result yet"
                    },
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (lastResult) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onOpenDialog,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open Dialog  →")
            }
        }
    }
}

