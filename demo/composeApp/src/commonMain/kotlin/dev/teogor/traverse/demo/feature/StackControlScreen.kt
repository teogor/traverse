package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * Stack-control entry screen.
 *
 * Starts a chain A → B → C. Each level shows `popTo` options.
 */
@Composable
fun StackControlScreen(onNavigateToA: () -> Unit) {
    ShowcaseScaffold(
        title = "Stack Control",
        apiBadge = "popTo",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Build a deep back stack (A → B → C) then use popTo() to jump " +
                    "back to any level in one call.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CodeSnippet(
                code = """// From anywhere in the stack:
nav.popTo(Catalog, inclusive = false)
// ↳ removes everything above Catalog

nav.popTo(StackLevelA, inclusive = true)
// ↳ removes StackLevelA and everything above it""",
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onNavigateToA,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go to Level A  →")
            }
        }
    }
}

/**
 * Stack depth level A / B / C — shared screen used by all three levels.
 *
 * Shows popTo controls once the stack is deep enough to be interesting.
 */
@Composable
fun StackLevelScreen(
    level: String,
    onGoDeeper: (() -> Unit)?,
    onPopToStackControl: () -> Unit,
    onPopToCatalog: () -> Unit,
) {
    ShowcaseScaffold(
        title = "Stack Level $level",
        apiBadge = "popTo",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "You are at depth $level. Watch the Back Stack bar below.",
                style = MaterialTheme.typography.bodyLarge,
            )

            CodeSnippet(
                code = when (level) {
                    "A" -> "// Back stack: [Catalog, StackControl, StackLevelA]"
                    "B" -> "// Back stack: [Catalog, StackControl, StackLevelA, StackLevelB]"
                    else -> "// Back stack: [Catalog, StackControl, StackLevelA, StackLevelB, StackLevelC]"
                },
            )

            Spacer(Modifier.weight(1f))

            if (onGoDeeper != null) {
                Button(
                    onClick = onGoDeeper,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Go deeper  →")
                }
            }

            Button(
                onClick = onPopToStackControl,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("popTo(StackControl)")
            }

            Button(
                onClick = onPopToCatalog,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("popTo(Catalog)")
            }
        }
    }
}

