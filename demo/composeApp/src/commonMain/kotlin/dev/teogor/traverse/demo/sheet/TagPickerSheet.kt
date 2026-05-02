package dev.teogor.traverse.demo.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val availableTags = listOf("personal", "work", "ideas", "gratitude", "travel", "health", "goals")

/**
 * Tag-picker bottom sheet — lets the user pick a tag for a journal entry.
 *
 * Demonstrates `bottomSheet<TagPicker>` destinations:
 * - Registered via `bottomSheet<TagPicker> { TagPickerSheet(...) }`.
 * - On tag selected: `navigator.setResultAndNavigateUp(RESULT_SELECTED_TAG, tag)`
 * - On dismiss: `navigator.navigateUp()`
 *
 * Navigation wiring is added in M3.
 *
 * @param onTagSelected Called with the selected tag string — triggers result + navigateUp.
 * @param onDismiss     Called when the sheet is dismissed without selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPickerSheet(
    onTagSelected: (tag: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Pick a tag",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            availableTags.forEach { tag ->
                TagRow(tag = tag, onClick = { onTagSelected(tag) })
            }
        }
    }
}

@Composable
private fun TagRow(tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

