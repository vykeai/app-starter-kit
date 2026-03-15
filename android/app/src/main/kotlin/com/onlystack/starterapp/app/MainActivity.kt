package com.onlystack.starterapp.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.onlystack.starterapp.core.deeplink.DeepLinkViewModel
import com.onlystack.starterapp.ui.StarterAppRoot
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point.
 *
 * Deep links are handled in both [onCreate] (cold start) and [onNewIntent]
 * (warm start, e.g. the app is already running and the user taps a magic-link
 * email on the same device). Both paths delegate to [DeepLinkViewModel] which
 * exposes a [StateFlow] consumed by the Compose nav tree.
 *
 * Supported deep link patterns (also declared in AndroidManifest.xml):
 *  - https://yourapp.com/auth/verify?email=user@example.com&code=XXXXXXXX&linkToken=...
 *  - appstarterkit://auth/verify?email=user@example.com&code=XXXXXXXX&linkToken=...
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deepLinkViewModel: DeepLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle a deep link that launched the app from a cold/warm start.
        deepLinkViewModel.handleUri(intent?.data)

        setContent {
            StarterAppRoot()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle a deep link delivered while the activity is already running
        // (e.g. singleTop or singleTask launch mode, or foreground app).
        deepLinkViewModel.handleUri(intent.data)
    }
}
