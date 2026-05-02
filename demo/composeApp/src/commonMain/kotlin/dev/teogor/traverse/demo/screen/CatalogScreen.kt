package dev.teogor.traverse.demo.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.BackStackBar

private data class FeatureEntry(
    val emoji: String,
    val title: String,
    val description: String,
    val badge: String,
)

private val FEATURES = listOf(
    FeatureEntry("🔀", "Nested Graph", "Group destinations into a sub-flow with a graph key that auto-redirects.", "nested()"),
    FeatureEntry("📦", "Typed Arguments", "Pass runtime data via data class destinations — no route strings.", "data class"),
    FeatureEntry("📬", "Navigation Results", "Return a value from a destination to its caller.", "setResult"),
    FeatureEntry("💬", "Dialog", "A destination rendered as a composable Dialog overlay.", "dialog<T>"),
    FeatureEntry("📋", "Bottom Sheet", "A destination rendered as a ModalBottomSheet overlay.", "bottomSheet<T>"),
    FeatureEntry("📌", "Stack Control", "Build a deep stack then pop to a specific destination.", "popTo"),
    FeatureEntry("🔝", "Single Top", "Prevent duplicate destinations at the top of the back stack.", "launchSingleTop"),
    FeatureEntry("🔗", "Deep Links", "Navigate to destinations via URI patterns on any platform.", "deepLink()"),
    FeatureEntry("🎬", "Transitions", "Explore built-in animation presets and define your own.", "TraverseTransitionSpec"),
    FeatureEntry("🏷️", "Annotations + KSP", "Declare screens with @TraverseScreen, @DeepLink, @Transition — KSP generates typed helpers.", "@TraverseScreen"),
    FeatureEntry("🗂️", "Screen Registry", "Live ScreenRegistry browser — filter by group/type, search, inspect deep links.", "ScreenRegistry"),
)

/**
 * Feature catalog — the persistent root of the Traverse Explorer.
 *
 * Each card navigates into a live demo section for that Traverse feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onNestedGraph: () -> Unit,
    onTypedArgs: () -> Unit,
    onResults: () -> Unit,
    onDialog: () -> Unit,
    onSheet: () -> Unit,
    onStackControl: () -> Unit,
    onSingleTop: () -> Unit,
    onDeepLinks: () -> Unit,
    onAnimations: () -> Unit,
    onAnnotations: () -> Unit,
    onScreenRegistry: () -> Unit,
) {
    val callbacks = listOf(
        onNestedGraph, onTypedArgs, onResults, onDialog, onSheet,
        onStackControl, onSingleTop, onDeepLinks, onAnimations, onAnnotations, onScreenRegistry,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traverse Explorer") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(FEATURES.zip(callbacks), key = { it.first.title }) { (feature, onClick) ->
                FeatureCard(feature = feature, onClick = onClick)
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = feature.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(40.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = feature.title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    text = feature.badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

