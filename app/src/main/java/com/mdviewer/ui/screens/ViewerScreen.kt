package com.mdviewer.ui.screens

import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

/**
 * Markdown content viewer using Markwon rendered into a native Android TextView.
 *
 * Key design decisions:
 * - ScrollView + TextView inside AndroidView (required because Markwon outputs
 *   Spanned for a TextView — no pure-Compose renderer available without a
 *   heavy WebView).
 * - Only calls setMarkdown() when [content] actually changes — not on every
 *   recomposition (theme toggle, rotation, etc.).
 * - Colors sync cheaply on every recomposition; scroll position is preserved
 *   across theme changes.
 */
@Composable
fun ViewerScreen(
    content: String,
    fileName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    // Build Markwon ONCE. Markwon is thread-safe to reuse across setMarkdown calls.
    val markwon = remember {
        val tablePlugin = TablePlugin.create(
            TablePlugin.ThemeConfigure { builder: TableTheme.Builder ->
                builder
                    .tableCellPadding(8)
                    .tableBorderWidth(2)
            }
        )
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(tablePlugin)
            .build()
    }

    // Track previous content to avoid redundant setMarkdown calls
    var lastContent by remember { mutableStateOf("") }
    // Preserve scroll position across recompositions (theme toggle, etc.)
    var savedScrollY by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        // File name bar
        Text(
            text = fileName.substringAfterLast('/'),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    ScrollView(ctx).apply {
                        overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                        clipToPadding = false

                        val textView = TextView(ctx).apply {
                            setBackgroundColor(bgColor.toArgb())
                            setTextColor(textColor.toArgb())
                            textSize = 16f
                            // Padding inside ScrollView for comfortable reading
                            setPadding(0, 0, 0, 32)
                        }
                        addView(textView)

                        // Initial render
                        markwon.setMarkdown(textView, content)
                        lastContent = content
                    }
                },
                update = { scrollView ->
                    val textView = scrollView.getChildAt(0) as? TextView
                        ?: return@AndroidView

                    // Cheap: always sync colors (theme toggle)
                    textView.setTextColor(textColor.toArgb())
                    textView.setBackgroundColor(bgColor.toArgb())

                    // Expensive: only re-render markdown when content changed
                    if (content != lastContent) {
                        val backup = scrollView.scrollY
                        markwon.setMarkdown(textView, content)
                        lastContent = content
                        // Restore scroll position after layout
                        textView.post {
                            scrollView.scrollTo(0, savedScrollY.coerceAtMost(scrollView.height))
                        }
                    }

                    // Track scroll position for theme-change restoration
                    scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                        savedScrollY = scrollY
                    }
                }
            )
        }
    }
}
