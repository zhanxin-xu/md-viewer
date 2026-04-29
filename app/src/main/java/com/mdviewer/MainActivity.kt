package com.mdviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mdviewer.ui.theme.MDViewerTheme
import com.mdviewer.ui.screens.MainScreen
import com.mdviewer.util.getFileName
import com.mdviewer.util.readFileContent

class MainActivity : ComponentActivity() {

    // Hoisted state — changes trigger Compose recomposition,
    // which lets onNewIntent update the UI reactively.
    private var fileState by mutableStateOf<Pair<String, String>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fileState = parseIntent(intent)

        setContent {
            MDViewerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(initialFile = fileState)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Update hoisted state → triggers recomposition in setContent
        fileState = parseIntent(intent)
    }

    private fun parseIntent(intent: Intent?): Pair<String, String>? {
        val action = intent?.action ?: return null
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_SEND) return null

        val uri = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
            else -> null
        } ?: return null

        val fileName = getFileName(uri, contentResolver)
        val content = readFileContent(uri, contentResolver) ?: return null
        return Pair(fileName, content)
    }
}
