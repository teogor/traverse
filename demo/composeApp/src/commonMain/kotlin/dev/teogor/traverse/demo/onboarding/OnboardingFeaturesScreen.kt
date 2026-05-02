package dev.teogor.traverse.demo.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Step 2 of onboarding — highlights Traverse's key features.
 *
 * Demonstrates: sequential `navigate()` inside a nested graph.
 *
 * @param onNext Called when the user taps "Next".
 * @param onBack Called when the user taps "Back".
 */
@Composable
fun OnboardingFeaturesScreen(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "What you can do",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(24.dp))
        FeatureCard(emoji = "📖", title = "Capture entries", body = "Write journal entries with tags and notes.")
        Spacer(Modifier.height(12.dp))
        FeatureCard(emoji = "🗂️", title = "Organise with tags", body = "Pick tags from a bottom sheet and filter your entries.")
        Spacer(Modifier.height(12.dp))
        FeatureCard(emoji = "🔒", title = "Your data, your device", body = "Everything stays local.")

        Spacer(Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            OutlinedButton(onClick = onNext) { Text("Next") }
        }
    }
}

@Composable
private fun FeatureCard(emoji: String, title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

