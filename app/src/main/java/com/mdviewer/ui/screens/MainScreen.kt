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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(initialFile: Pair<String, String>? = null) {
    var currentFile by remember { mutableStateOf(initialFile?.first) }
    var currentContent by remember { mutableStateOf(initialFile?.second) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (currentFile != null && currentContent != null) {
            // Viewer mode
            TopAppBar(
                title = {
                    Text(
                        text = currentFile?.substringAfterLast('/') ?: "Markdown",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        currentFile = null
                        currentContent = null
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            ViewerScreen(
                content = currentContent!!,
                fileName = currentFile ?: "",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            )
        } else {
            // File list mode
            TopAppBar(
                title = { Text("Markdown Viewer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

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
