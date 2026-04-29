package com.mdviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mdviewer.ui.theme.MDViewerTheme
import com.mdviewer.ui.screens.MainScreen
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get initial file from intent
        val initialFile = parseIntent(intent)

        setContent {
            MDViewerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(initialFile = initialFile)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle re-launch (singleTask / singleTop mode)
        setIntent(intent)
    }

    private fun parseIntent(intent: Intent?): Pair<String, String>? {
        val action = intent?.action ?: return null
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_SEND) return null

        val uri: Uri? = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.getParcelableExtra(Intent.EXTRA_STREAM)
            else -> null
        }

        uri ?: return null

        val fileName = getFileName(uri)
        val content = readFileContent(uri) ?: return null
        return Pair(fileName, content)
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex)
                }
            }
        }
        // Fallback: extract from path
        val path = uri.path ?: return "file.md"
        return path.substringAfterLast('/').ifEmpty { "file.md" }
    }

    private fun readFileContent(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
        } catch (e: Exception) {
            null
        }
    }
}
