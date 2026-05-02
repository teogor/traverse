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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A simple model for a journal entry used only inside the demo.
 * Real apps would use a ViewModel with a proper data layer.
 */
data class JournalEntry(val id: String, val title: String, val preview: String)

/**
 * Home screen — shows the list of journal entries.
 *
 * Demonstrates:
 * - `navigate(EntryDetail(entryId))` — typed args
 * - `navigate(NewEntry)` + `CollectTraverseResultOnce("new_entry_title")` — result passing (consumer side)
 * - `navigate(Settings)` — simple forward navigation
 *
 * Navigation wiring is added in M3 when [LocalTraverseNavigator] is available.
 *
 * @param onOpenEntry   Called when the user taps a journal entry card.
 * @param onNewEntry    Called when the user taps the FAB to create a new entry.
 * @param onSettings    Called when the user taps the settings icon.
 * @param newEntryTitle When non-null, a new entry was just created via result passing — add it to the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenEntry: (entryId: String) -> Unit,
    onNewEntry: () -> Unit,
    onSettings: () -> Unit,
    newEntryTitle: String? = null,
) {
    // Local demo state — replaced by ViewModel in a real app
    val seedEntries = listOf(
        JournalEntry("1", "First day of spring", "The sun was out and the air finally felt warm…"),
        JournalEntry("2", "Project ideas", "Thinking about building something with Kotlin Multiplatform…"),
        JournalEntry("3", "Reading notes", "Finished the chapter on coroutines. A few things to revisit…"),
    )
    var entries by remember {
        mutableStateOf(
            if (newEntryTitle != null)
                seedEntries + JournalEntry(id = "new", title = newEntryTitle, preview = "")
            else seedEntries
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                actions = {
                    TextButton(onClick = onSettings) {
                        Text("Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Text("+")
            }
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("No entries yet.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap the + button to write your first journal entry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
            if (entry.preview.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

/**
 * Key used with [CollectTraverseResultOnce] to receive a newly created entry title from [NewEntryScreen].
 * Defined here so both screens reference the same constant.
 */
const val RESULT_NEW_ENTRY_TITLE = "new_entry_title"
