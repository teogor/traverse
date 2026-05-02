package dev.teogor.traverse.demo.feature

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.annotations.ScreenInfo
import dev.teogor.traverse.annotations.ScreenRegistry
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

// ── Colour helpers ────────────────────────────────────────────────────────────

@Composable
private fun ScreenInfo.DestinationType.containerColor(): Color = when (this) {
    ScreenInfo.DestinationType.SCREEN       -> MaterialTheme.colorScheme.primaryContainer
    ScreenInfo.DestinationType.DIALOG       -> MaterialTheme.colorScheme.secondaryContainer
    ScreenInfo.DestinationType.BOTTOM_SHEET -> MaterialTheme.colorScheme.tertiaryContainer
}

@Composable
private fun ScreenInfo.DestinationType.onContainerColor(): Color = when (this) {
    ScreenInfo.DestinationType.SCREEN       -> MaterialTheme.colorScheme.onPrimaryContainer
    ScreenInfo.DestinationType.DIALOG       -> MaterialTheme.colorScheme.onSecondaryContainer
    ScreenInfo.DestinationType.BOTTOM_SHEET -> MaterialTheme.colorScheme.onTertiaryContainer
}

private fun ScreenInfo.DestinationType.label(): String = when (this) {
    ScreenInfo.DestinationType.SCREEN       -> "SCREEN"
    ScreenInfo.DestinationType.DIALOG       -> "DIALOG"
    ScreenInfo.DestinationType.BOTTOM_SHEET -> "SHEET"
}

private fun ScreenInfo.DestinationType.emoji(): String = when (this) {
    ScreenInfo.DestinationType.SCREEN       -> "🖥"
    ScreenInfo.DestinationType.DIALOG       -> "💬"
    ScreenInfo.DestinationType.BOTTOM_SHEET -> "📋"
}

// ── Filter state ──────────────────────────────────────────────────────────────

private enum class TypeFilter(val label: String) {
    ALL("All"),
    SCREEN("Screens"),
    DIALOG("Dialogs"),
    SHEET("Sheets"),
}

// ── Main screen ───────────────────────────────────────────────────────────────

/**
 * Live, interactive [ScreenRegistry] browser.
 *
 * Uses [ScreenRegistry] to display every annotated destination registered by
 * `initTraverseScreenRegistry()` — called once in App.kt.
 *
 * Features:
 * - Stats row: live counts for screens / dialogs / sheets / deep-linked entries
 * - Search bar: fuzzy-filter across name, description, group, and class simple name
 * - Group chips: scroll through all 9 groups (+ "All")
 * - Type toggle chips: All / Screen / Dialog / Sheet
 * - Destination cards: name, type badge, description, group, deep links, root indicator
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScreenRegistryDemoScreen(
    onNavigateUp: () -> Unit,
) {
    val allEntries = remember { ScreenRegistry.all }
    val allGroups  = remember { listOf("All") + allEntries.mapNotNull { it.group.takeIf { g -> g.isNotBlank() } }.distinct().sorted() }

    var searchQuery   by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("All") }
    var typeFilter    by remember { mutableStateOf(TypeFilter.ALL) }

    val filtered by remember(searchQuery, selectedGroup, typeFilter) {
        derivedStateOf {
            allEntries.filter { entry ->
                val matchesType = when (typeFilter) {
                    TypeFilter.ALL    -> true
                    TypeFilter.SCREEN -> entry.destinationType == ScreenInfo.DestinationType.SCREEN
                    TypeFilter.DIALOG -> entry.destinationType == ScreenInfo.DestinationType.DIALOG
                    TypeFilter.SHEET  -> entry.destinationType == ScreenInfo.DestinationType.BOTTOM_SHEET
                }
                val matchesGroup = selectedGroup == "All" || entry.group == selectedGroup
                val q = searchQuery.trim().lowercase()
                val matchesQuery = q.isEmpty() ||
                    entry.name.lowercase().contains(q) ||
                    entry.description.lowercase().contains(q) ||
                    entry.group.lowercase().contains(q) ||
                    entry.klass.simpleName?.lowercase()?.contains(q) == true ||
                    entry.deepLinkPatterns.any { it.lowercase().contains(q) }
                matchesType && matchesGroup && matchesQuery
            }
        }
    }

    ShowcaseScaffold(
        title = "Screen Registry",
        apiBadge = "ScreenRegistry",
        onBack = onNavigateUp,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Stats row ──────────────────────────────────────────────────────
            item(key = "stats") {
                StatsRow(all = allEntries)
            }

            // ── Search bar ─────────────────────────────────────────────────────
            item(key = "search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, group, URI pattern…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* dismiss */ }),
                )
            }

            // ── Type toggle ────────────────────────────────────────────────────
            item(key = "type-filter") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TypeFilter.entries.forEach { tf ->
                        FilterChip(
                            selected = typeFilter == tf,
                            onClick = { typeFilter = tf },
                            label = { Text(tf.label) },
                        )
                    }
                }
            }

            // ── Group chips ────────────────────────────────────────────────────
            item(key = "group-filter") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                    items(allGroups, key = { it }) { group ->
                        val count = allEntries.count { it.group == group || group == "All" }
                        FilterChip(
                            selected = selectedGroup == group,
                            onClick = { selectedGroup = group },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(group)
                                    if (group != "All") {
                                        Spacer(Modifier.width(4.dp))
                                        Badge(
                                            containerColor = if (selectedGroup == group)
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = if (selectedGroup == group)
                                                MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.onSecondaryContainer,
                                        ) {
                                            Text(
                                                "${allEntries.count { it.group == group }}",
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }

            // ── Result header ──────────────────────────────────────────────────
            item(key = "result-header") {
                ResultHeader(shown = filtered.size, total = allEntries.size)
            }

            // ── Destination cards ──────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No destinations match your filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(filtered, key = { it.klass.simpleName ?: it.name }) { entry ->
                    DestinationCard(entry = entry)
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsRow(all: List<ScreenInfo>) {
    val screens    = remember(all) { all.count { it.destinationType == ScreenInfo.DestinationType.SCREEN } }
    val dialogs    = remember(all) { all.count { it.destinationType == ScreenInfo.DestinationType.DIALOG } }
    val sheets     = remember(all) { all.count { it.destinationType == ScreenInfo.DestinationType.BOTTOM_SHEET } }
    val deepLinks  = remember(all) { all.count { it.deepLinkPatterns.isNotEmpty() } }
    val roots      = remember(all) { all.count { it.isRoot } }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatChip(emoji = "🖥", label = "Screens",    count = screens,   containerColor = MaterialTheme.colorScheme.primaryContainer,   contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        StatChip(emoji = "💬", label = "Dialogs",    count = dialogs,   containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        StatChip(emoji = "📋", label = "Sheets",     count = sheets,    containerColor = MaterialTheme.colorScheme.tertiaryContainer,  contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
        StatChip(emoji = "🔗", label = "Deep links", count = deepLinks, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
        StatChip(emoji = "⭐", label = "Roots",      count = roots,     containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatChip(
    emoji: String,
    label: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Column {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(shown: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (shown == total) "All $total destinations" else "$shown of $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        HorizontalDivider(modifier = Modifier.weight(2f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DestinationCard(entry: ScreenInfo) {
    var expanded by remember { mutableStateOf(false) }
    val hasDeepLinks = entry.deepLinkPatterns.isNotEmpty()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)) {

            // ── Name row: name + type badge ────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${entry.destinationType.emoji()} ${entry.name}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (entry.isRoot) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            "⭐ root",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Surface(
                    color = entry.destinationType.containerColor(),
                    contentColor = entry.destinationType.onContainerColor(),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text = entry.destinationType.label(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // ── Class name + group ─────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.klass.simpleName ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (entry.group.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = entry.group,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // ── Description ────────────────────────────────────────────────────
            if (entry.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Deep link toggle ───────────────────────────────────────────────
            if (hasDeepLinks) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = if (expanded) "▲ Hide deep links (${entry.deepLinkPatterns.size})"
                               else "▼ Show deep links (${entry.deepLinkPatterns.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        entry.deepLinkPatterns.forEach { pattern ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "🔗 $pattern",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

