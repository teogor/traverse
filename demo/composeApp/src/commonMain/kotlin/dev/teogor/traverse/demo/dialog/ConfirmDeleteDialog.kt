package dev.teogor.traverse.demo.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Confirm-delete dialog — asks the user to confirm deleting a journal entry.
 *
 * Demonstrates `dialog<ConfirmDelete>` destinations:
 * - This composable is registered via `dialog<ConfirmDelete> { dest -> ConfirmDeleteDialog(...) }`.
 * - On confirm: `navigator.setResultAndNavigateUp(RESULT_DELETE_CONFIRMED, true)`
 * - On dismiss: `navigator.setResultAndNavigateUp(RESULT_DELETE_CONFIRMED, false)`
 *
 * Navigation wiring is added in M3.
 *
 * @param entryId    The ID of the entry to delete (shown in the message).
 * @param onConfirm  Called when the user taps "Delete" — sets result true + navigates up.
 * @param onDismiss  Called when the user taps "Cancel" or dismisses — sets result false + navigates up.
 */
@Composable
fun ConfirmDeleteDialog(
    entryId: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete entry?") },
        text = {
            Text("Entry #$entryId will be permanently deleted. This cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

