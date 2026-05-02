package dev.teogor.traverse.demo.feature

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

// ── Data model ────────────────────────────────────────────────────────────────

private data class AnimEntry(
    val emoji: String,
    val name: String,
    /** The API call shown in the badge, e.g. `TraverseTransitionSpec.fade()`. */
    val apiCall: String,
    val pushDescription: String,
    val popDescription: String,
    val onNavigate: () -> Unit,
)

// ── Showcase list ─────────────────────────────────────────────────────────────

/**
 * Lists all built-in [TraverseTransitionSpec][dev.teogor.traverse.compose.transition.TraverseTransitionSpec]
 * presets as tappable cards. Tapping each navigates forward **using that preset's animation**, so
 * the user immediately sees the enter transition and — when returning — the pop transition.
 */
@Composable
fun AnimationShowcaseScreen(
    onFade: () -> Unit,
    onHorizontalSlide: () -> Unit,
    onVerticalSlide: () -> Unit,
    onSlideAndFade: () -> Unit,
    onScaleAndFade: () -> Unit,
    onElevate: () -> Unit,
    onNone: () -> Unit,
) {
    val entries = listOf(
        AnimEntry(
            emoji = "✨",
            name = "Slide and Fade",
            apiCall = "slideAndFade()",
            pushDescription = "Slides in from the right (1/5 width) + fade in",
            popDescription = "Slides back to the right (1/5 width) + fade out",
            onNavigate = onSlideAndFade,
        ),
        AnimEntry(
            emoji = "🎞",
            name = "Fade",
            apiCall = "fade()",
            pushDescription = "New screen fades in",
            popDescription = "Previous screen fades back in",
            onNavigate = onFade,
        ),
        AnimEntry(
            emoji = "↔️",
            name = "Horizontal Slide",
            apiCall = "horizontalSlide()",
            pushDescription = "Full-width slide in from the right",
            popDescription = "Full-width slide back to the right",
            onNavigate = onHorizontalSlide,
        ),
        AnimEntry(
            emoji = "⬆️",
            name = "Vertical Slide",
            apiCall = "verticalSlide()",
            pushDescription = "Slides up from the bottom (modal card feel)",
            popDescription = "Slides back down; caller scales back into view",
            onNavigate = onVerticalSlide,
        ),
        AnimEntry(
            emoji = "🔍",
            name = "Scale and Fade",
            apiCall = "scaleAndFade()",
            pushDescription = "Grows from 92% scale + fade in (zoom into detail)",
            popDescription = "Shrinks back to 92% + fade out (zoom back out)",
            onNavigate = onScaleAndFade,
        ),
        AnimEntry(
            emoji = "🃏",
            name = "Elevate",
            apiCall = "elevate()",
            pushDescription = "Rises from 94% scale with upward drift + fade in",
            popDescription = "Drifts back down + scale down + fade out",
            onNavigate = onElevate,
        ),
        AnimEntry(
            emoji = "⚡",
            name = "None",
            apiCall = "none()",
            pushDescription = "Instant cut — no animation",
            popDescription = "Instant cut — no animation",
            onNavigate = onNone,
        ),
    )

    ShowcaseScaffold(
        title = "Transitions",
        apiBadge = "TraverseTransitionSpec",
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
            item {
                Text(
                    text = "Tap any preset to navigate forward and experience both its enter " +
                        "and (on return) pop animation. Define your own by constructing a " +
                        "TraverseTransitionSpec with custom lambdas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(entries, key = { it.name }) { entry ->
                AnimationCard(entry = entry)
            }
        }
    }
}

@Composable
private fun AnimationCard(entry: AnimEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = entry.onNavigate),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = entry.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.size(40.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = entry.name, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = entry.apiCall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LabeledRow("Push →", entry.pushDescription)
            LabeledRow("Pop ←", entry.popDescription)
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Preview screen (shared by all presets) ────────────────────────────────────

/**
 * Preview screen shown after navigating to a specific animation preset.
 *
 * Contains the name, API call, and a description of what the user just saw. A "Go Back" button
 * triggers `navigateUp()` so the user also sees the **pop** transition.
 */
@Composable
fun AnimationPreviewScreen(
    name: String,
    apiCall: String,
    pushDescription: String,
    popDescription: String,
    onBack: () -> Unit,
) {
    ShowcaseScaffold(
        title = name,
        apiBadge = apiCall,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "You just saw the enter transition:",
                style = MaterialTheme.typography.titleSmall,
            )
            InfoCard(label = "Push →", text = pushDescription)

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Press Go Back (or swipe) to see the pop transition:",
                style = MaterialTheme.typography.titleSmall,
            )
            InfoCard(label = "Pop ←", text = popDescription)

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Apply this preset to the entire host:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CodeBlock("TraverseHost(\n    transitions = TraverseTransitionSpec.$apiCall\n) { ... }")

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Or per individual screen:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CodeBlock("screen<MyScreen>(\n    transitionSpec = TraverseTransitionSpec.$apiCall\n) { ... }")

            Spacer(Modifier.weight(1f))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go Back (see pop animation)")
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
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

