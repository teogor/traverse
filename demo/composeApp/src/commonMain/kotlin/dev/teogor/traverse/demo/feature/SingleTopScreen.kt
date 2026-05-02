package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * `launchSingleTop` demo.
 *
 * Two buttons both navigate to [SingleTopDemo]:
 * - The first uses a normal `navigate()` — the stack grows each time.
 * - The second uses `{ launchSingleTop = true }` — no push if already on top.
 *
 * The [BackStackBar][dev.teogor.traverse.demo.ui.BackStackBar] at the bottom
 * makes the difference immediately visible.
 */
@Composable
fun SingleTopScreen(
    stackSize: Int,
    onNavigateNormal: () -> Unit,
    onNavigateSingleTop: () -> Unit,
) {
    ShowcaseScaffold(
        title = "Single Top",
        apiBadge = "launchSingleTop",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Current stack size: $stackSize",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "SingleTopDemo × $stackSize",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            Text(
                text = "Tap 'Normal' repeatedly — the stack grows and each press pushes a new copy. " +
                    "Tap 'Single Top' — nothing is pushed because SingleTopDemo is already on top.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CodeSnippet(
                code = """// Normal — always pushes:
nav.navigate(SingleTopDemo)

// Single top — skips push if top == SingleTopDemo:
nav.navigate(SingleTopDemo) {
    launchSingleTop = true
}""",
            )

            HorizontalDivider()

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onNavigateNormal,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Normal")
                }
                Button(
                    onClick = onNavigateSingleTop,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Single Top")
                }
            }
        }
    }
}

