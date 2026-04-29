package com.mdviewer.ui.screens

import android.view.View
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
import io.noties.markwon.ext.tables.TableTheme

@Composable
fun ViewerScreen(
    content: String,
    fileName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val markwon = remember {
        val tablePlugin = io.noties.markwon.ext.tables.TablePlugin.create(
            object : io.noties.markwon.ext.tables.TablePlugin.ThemeConfigure {
                override fun configureTheme(builder: TableTheme.Builder) {
                    builder
                        .tableCellPadding(8)
                        .tableBorderWidth(2)
                }
            }
        )
        Markwon.builder(context)
            .usePlugin(io.noties.markwon.ext.strikethrough.StrikethroughPlugin.create())
            .usePlugin(tablePlugin)
            .build()
    }

    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        // File info bar
        Text(
            text = fileName,
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
                        overScrollMode = View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                        clipToPadding = false

                        val textView = TextView(ctx).apply {
                            setBackgroundColor(bgColor.toArgb())
                            setTextColor(textColor.toArgb())
                            textSize = 16f
                        }
                        addView(textView)
                    }
                },
                update = { scrollView ->
                    val textView = scrollView.getChildAt(0) as TextView
                    textView.setTextColor(textColor.toArgb())
                    textView.setBackgroundColor(bgColor.toArgb())
                    markwon.setMarkdown(textView, content)
                }
            )
        }
    }
}
