package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * Reusable step screen for the nested-graph demo flow.
 *
 * Demonstrates: `nested(startDestination = NestedStep1, graphKey = NestedFlowGraph) { ... }`
 */
@Composable
fun NestedStepScreen(
    step: Int,
    totalSteps: Int,
    description: String,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null,
    nextLabel: String = "Next  →",
) {
    ShowcaseScaffold(
        title = "Nested Graph — Step $step / $totalSteps",
        apiBadge = "nested()",
        onBack = onBack,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { step.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Step $step of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
            )

            when (step) {
                1 -> CodeSnippet(
                    code = """nested(
    startDestination = NestedStep1,
    graphKey = NestedFlowGraph,
) {
    screen<NestedStep1> { ... }
    screen<NestedStep2> { ... }
    screen<NestedStep3> { ... }
}""",
                )
                2 -> CodeSnippet(
                    code = """// Navigating to the graph key auto-redirects:
nav.navigate(NestedFlowGraph)
// ↳ back stack becomes: [Catalog, NestedStep1]""",
                )
                3 -> CodeSnippet(
                    code = """// Exit the entire nested flow in one call:
nav.popTo(Catalog, inclusive = false)
// ↳ back stack becomes: [Catalog]""",
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (onBack != null) Arrangement.SpaceBetween else Arrangement.End,
            ) {
                if (onBack != null) {
                    OutlinedButton(onClick = onBack) { Text("← Back") }
                }
                Button(onClick = onNext) { Text(nextLabel) }
            }
        }
    }
}

