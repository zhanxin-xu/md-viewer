package com.mdviewer.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mdviewer.util.getFileName
import com.mdviewer.util.readFileContent

@Composable
fun FileListScreen(
    onFileSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        val fileName = getFileName(uri, context.contentResolver)
        if (!fileName.endsWith(".md", ignoreCase = true)) {
            Toast.makeText(context, "仅支持 .md 文件", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val content = readFileContent(uri, context.contentResolver)
        if (content == null) {
            Toast.makeText(context, "读取文件失败", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        onFileSelected(fileName, content)
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "选择一个 Markdown 文件",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "支持 .md 文件格式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                filePickerLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("打开文件", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v1.0 · by zhanxin-xu",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
