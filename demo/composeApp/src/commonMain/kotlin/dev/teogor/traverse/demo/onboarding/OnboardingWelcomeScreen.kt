package dev.teogor.traverse.demo.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Step 1 of onboarding — welcome message.
 *
 * Navigates forward to [OnboardingFeaturesScreen] on "Next".
 * Demonstrates: `nested()` graph entry, simple `navigate()` forward.
 *
 * @param onNext Called when the user taps "Next".
 */
@Composable
fun OnboardingWelcomeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "✍️",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Welcome to Journal",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your personal space to capture thoughts, ideas, and memories.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onNext) {
            Text("Get started")
        }
    }
}

