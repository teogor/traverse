package dev.teogor.traverse.demo.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Step 3 of onboarding — "You're all set" confirmation.
 *
 * Tapping "Start journaling" calls [onGetStarted] which should use
 * `navigator.launchAsNewRoot<Onboarding>(Home)` — clearing the onboarding
 * graph entirely and establishing Home as the only back-stack entry.
 *
 * Demonstrates: `launchAsNewRoot`, clearing a nested graph on completion.
 *
 * @param onGetStarted Called when the user taps "Start journaling".
 * @param onBack       Called when the user taps "Back".
 */
@Composable
fun OnboardingReadyScreen(onGetStarted: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your journal is ready. Start writing your first entry.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start journaling")
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onBack) { Text("Back") }
        }
    }
}

