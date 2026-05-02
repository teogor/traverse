package dev.teogor.traverse.demo.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings screen — app preferences and navigation demos.
 *
 * Demonstrates:
 * - `popTo(Home, inclusive = false)` — "Go home" button.
 * - `navigateAndClearUpTo<Home>(Settings)` — back-stack manipulation.
 * - `navigateUp()` — back arrow.
 *
 * @param onBack   Called when the user taps the back arrow.
 * @param onGoHome Called when the user taps "Go to Home" — should use `popTo(Home)`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onGoHome: () -> Unit,
) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Preferences", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            SettingToggleRow(
                label = "Dark mode",
                description = "Switch between light and dark theme",
                checked = darkMode,
                onCheckedChange = { darkMode = it },
            )
            HorizontalDivider()
            SettingToggleRow(
                label = "Notifications",
                description = "Daily journaling reminders",
                checked = notifications,
                onCheckedChange = { notifications = it },
            )
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))
            Text("Navigation", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                "These buttons demonstrate Traverse's back-stack manipulation APIs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
                Text("popTo(Home) — go to Home, keep Home on stack")
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

