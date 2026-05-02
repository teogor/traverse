package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.FEATURE_INFO
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

/**
 * Typed-arguments demo screen.
 *
 * Demonstrates that any `@Serializable data class` is a valid Destination.
 * The [featureId] runtime argument is carried directly on the destination — no
 * route strings, no argument holders.
 */
@Composable
fun TypedArgsScreen(
    featureId: String,
    onNavigateWithDifferentId: (String) -> Unit,
) {
    val (title, description) = FEATURE_INFO[featureId]
        ?: ("Unknown" to "No info for featureId = \"$featureId\"")

    ShowcaseScaffold(
        title = "Typed Arguments",
        apiBadge = "data class",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Current featureId:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "\"$featureId\"",
                style = MaterialTheme.typography.headlineSmall,
            )

            HorizontalDivider()

            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CodeSnippet(
                code = """// Destination definition:
@Serializable data class FeatureDetail(
    val featureId: String
) : Destination

// Navigate with type-safe arg:
nav.navigate(FeatureDetail("$featureId"))

// Receive in graph builder:
screen<FeatureDetail> { dest ->
    TypedArgsScreen(featureId = dest.featureId)
}""",
            )

            Spacer(Modifier.weight(1f))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Navigate to the same screen with a different arg:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val otherIds = FEATURE_INFO.keys.filter { it != featureId }
            otherIds.forEach { id ->
                Button(
                    onClick = { onNavigateWithDifferentId(id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("FeatureDetail(\"$id\")")
                }
            }
        }
    }
}

