package com.mdviewer.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Extract a human-readable file name from a content URI.
 * Uses DISPLAY_NAME from the content provider; falls back to path extraction.
 */
fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) return cursor.getString(idx)
        }
    }
    // Fallback
    val path = uri.path ?: return "file.md"
    return path.substringAfterLast('/').ifEmpty { "file.md" }
}

/**
 * Read the full text content from a content URI.
 * Returns null on any error.
 */
fun readFileContent(uri: Uri, contentResolver: ContentResolver): String? {
    return try {
        contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
    } catch (_: Exception) {
        null
    }
}
