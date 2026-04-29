package com.mdviewer.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Root screen — toggles between file-list and markdown-viewer modes.
 *
 * @param initialFile  Passed from MainActivity; when it changes (e.g. via
 *                     onNewIntent), LaunchedEffect syncs it into the internal
 *                     viewer state so the UI reacts even when the app is
 *                     already running.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(initialFile: Pair<String, String>? = null) {
    var currentFile by remember { mutableStateOf<String?>(null) }
    var currentContent by remember { mutableStateOf<String?>(null) }

    // React to external intents — including onNewIntent while app is running
    LaunchedEffect(initialFile) {
        initialFile?.let { (name, content) ->
            currentFile = name
            currentContent = content
        }
    }

    val isViewer = currentFile != null && currentContent != null

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (isViewer) {
                        currentFile!!.substringAfterLast('/')
                    } else {
                        "Markdown Viewer"
                    },
                    maxLines = 1
                )
            },
            navigationIcon = {
                if (isViewer) {
                    IconButton(onClick = {
                        currentFile = null
                        currentContent = null
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (isViewer) {
            ViewerScreen(
                content = currentContent!!,
                fileName = currentFile!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            )
        } else {
            FileListScreen(
                onFileSelected = { name, content ->
                    currentFile = name
                    currentContent = content
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            )
        }
    }
}
