package dev.teogor.traverse.demo.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
 * New entry screen — a form where the user writes a new journal entry.
 *
 * Demonstrates navigation results (producer side):
 * When the user taps "Save", this screen should call:
 * ```kotlin
 * navigator.setResultAndNavigateUp(RESULT_NEW_ENTRY_TITLE, title)
 * ```
 * and the Home screen collects it via `CollectTraverseResultOnce`.
 *
 * Navigation wiring is added in M3.
 *
 * @param onSave   Called with the entry title when the user saves. Triggers result + navigateUp.
 * @param onCancel Called when the user taps the back arrow without saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    onSave: (title: String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New entry") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                maxLines = 10,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (title.isNotBlank()) onSave(title.trim()) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save entry")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Saving will return the entry title to the Home screen via navigation results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
