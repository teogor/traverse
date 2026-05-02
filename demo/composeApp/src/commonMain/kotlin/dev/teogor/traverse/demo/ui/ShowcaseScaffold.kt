package dev.teogor.traverse.demo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator

/**
 * Shared scaffold for all showcase screens.
 *
 * - TopAppBar with optional back button + [apiBadge] chip showing the Traverse API in use
 * - [BackStackBar] pinned to the bottom — live back-stack breadcrumbs
 * - [content] fills the space between them
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScaffold(
    title: String,
    apiBadge: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val navigator = LocalTraverseNavigator.current
    val resolvedBack: (() -> Unit)? = onBack ?: if (navigator.canNavigateUp) ({ navigator.navigateUp() }) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    if (resolvedBack != null) {
                        TextButton(onClick = resolvedBack) {
                            Text("← Back")
                        }
                    }
                },
                actions = {
                    if (apiBadge != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(end = 12.dp),
                        ) {
                            Text(
                                text = apiBadge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = { BackStackBar() },
    ) { innerPadding ->
        Box { content(innerPadding) }
    }
}

/** Small card that shows a Traverse API code snippet for context. */
@Composable
fun CodeSnippet(code: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

