package dev.teogor.traverse.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    /** URI received from an incoming deep-link Intent — consumed by [App] once composed. */
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        pendingDeepLink = intent?.data?.toString()
        setContent {
            App(pendingDeepLink = pendingDeepLink, onDeepLinkConsumed = { pendingDeepLink = null })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep links arriving while the app is already running.
        pendingDeepLink = intent.data?.toString()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}