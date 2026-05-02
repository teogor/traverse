package dev.teogor.traverse.demo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Entry detail screen — shows the full content of a single journal entry.
 *
 * Demonstrates:
 * - Receiving a typed `entryId: String` arg from the destination data class.
 * - `dialog<ConfirmDelete>` — confirm before deleting.
 * - `bottomSheet<TagPicker>` + `CollectTraverseResultOnce("selected_tag")` — tag selection result.
 * - `navigateUp()` — back button.
 *
 * @param entryId         The ID of the journal entry to display.
 * @param onBack          Called when the user taps the back arrow.
 * @param onDeleteRequest Called when the user taps Delete — should navigate to ConfirmDeleteDialog.
 * @param onPickTag       Called when the user taps "Add tag" — should navigate to TagPickerSheet.
 * @param currentTag      Non-null when a tag was just selected via result passing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: String,
    onBack: () -> Unit,
    onDeleteRequest: () -> Unit,
    onPickTag: () -> Unit,
    currentTag: String? = null,
) {
    var tag by remember(currentTag) { mutableStateOf(currentTag) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry #$entryId") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(onClick = onDeleteRequest) {
                        Text("Delete entry")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Entry #$entryId",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This is the full content of entry $entryId. In a real app this would come from a local database via a ViewModel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            if (tag != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Tag: ", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPickTag) {
                    Text(if (tag == null) "Add tag" else "Change tag")
                }
            }
        }
    }
}

/**
 * Key used with [CollectTraverseResultOnce] to receive the selected tag from [TagPickerSheet].
 */
const val RESULT_SELECTED_TAG = "selected_tag"

/**
 * Key used with [CollectTraverseResultOnce] to receive the delete confirmation result
 * from [ConfirmDeleteDialog].
 */
const val RESULT_DELETE_CONFIRMED = "delete_confirmed"

