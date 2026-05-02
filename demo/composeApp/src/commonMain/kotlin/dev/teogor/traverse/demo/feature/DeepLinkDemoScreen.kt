package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.BackStackBar

private val EXAMPLE_URIS = listOf(
    "traverse://demo/target/hello-world" to "deep link with String id",
    "traverse://demo/target/42" to "deep link with numeric id",
    "https://traverse.teogor.dev/target/kmp" to "HTTPS scheme",
    "traverse://demo/feature/compose" to "navigate to FeatureDetail",
    "traverse://demo/target/no-match-test??bad" to "invalid URI — should fail gracefully",
)

/**
 * Demo screen for the [TraverseDeepLink] feature.
 *
 * Shows pre-built example URIs and a free-text input so you can explore
 * `navigator.navigateToDeepLink(uri)` interactively.
 *
 * @param onNavigateToUri  Called when the user triggers a deep-link navigation attempt.
 *   Returns `true` when matched (the navigator has already navigated), `false` when not.
 * @param onNavigateUp     Called by the back arrow / system back gesture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepLinkDemoScreen(
    onNavigateToUri: (String) -> Boolean,
    onNavigateUp: () -> Unit,
) {
    var customUri by remember { mutableStateOf("") }
    var lastResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep Links") },
                navigationIcon = {
                    FilledTonalButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) { Text("←") }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        bottomBar = { BackStackBar() },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "navigator.navigateToDeepLink(uri)",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap an example URI below or type your own. " +
                        "Matched URIs navigate to DeepLinkTarget(id=…) or FeatureDetail(featureId=…). " +
                        "Unmatched URIs return false — no crash.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Text("Example URIs", style = MaterialTheme.typography.labelLarge)
            }

            items(EXAMPLE_URIS, key = { it.first }) { (uri, label) ->
                ExampleUriCard(
                    uri = uri,
                    label = label,
                    onNavigate = { u ->
                        val matched = onNavigateToUri(u)
                        if (!matched) lastResult = "❌ No match for: $u"
                    },
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Custom URI", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customUri,
                    onValueChange = { customUri = it; lastResult = null },
                    label = { Text("Enter a URI") },
                    placeholder = { Text("traverse://demo/target/123") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val matched = onNavigateToUri(customUri.trim())
                        lastResult = if (matched) "✅ Navigated!" else "❌ No match for: $customUri"
                    },
                    enabled = customUri.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Navigate") }
                lastResult?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("✅"))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                    )
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Registered patterns", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "traverse://demo/target/{id}",
                            "https://traverse.teogor.dev/target/{id}",
                            "traverse://demo/feature/{featureId}",
                        ).forEach { pattern ->
                            Text(
                                text = "• $pattern",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleUriCard(uri: String, label: String, onNavigate: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = uri,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
            }
            Button(
                onClick = { onNavigate(uri) },
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) { Text("Go") }
        }
    }
}

