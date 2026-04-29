package com.mdviewer.ui.screens

import android.graphics.Typeface
import android.view.ScaleGestureDetector
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

    var textSizeSp by remember { mutableFloatStateOf(16f) }
    val minSize = 10f
    val maxSize = 36f

    val markwon = remember {
        // Theme-aware: table cell padding, border, and row backgrounds
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

        // Zoom indicator
        if (kotlin.math.abs(textSizeSp - 16f) > 1f) {
            Text(
                text = "缩放: ${(textSizeSp / 16f * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    TextView(ctx).apply {
                        setBackgroundColor(bgColor.toArgb())
                        setTextColor(textColor.toArgb())
                        textSize = textSizeSp
                        // Enable vertical scrolling within the TextView
                        movementMethod = android.text.method.ScrollingMovementMethod.getInstance()
                        setVerticalScrollBarEnabled(true)

                        // Pinch-to-zoom using ScaleGestureDetector
                        val scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                val newSize = (textSizeSp * detector.scaleFactor)
                                    .coerceIn(minSize, maxSize)
                                textSizeSp = newSize
                                this@apply.textSize = newSize
                                return true
                            }
                        })
                        setOnTouchListener { view, event ->
                            scaleDetector.onTouchEvent(event)
                            // Don't consume, let TextView handle scrolling
                            false
                        }
                    }
                },
                update = { textView ->
                    textView.setTextColor(textColor.toArgb())
                    textView.setBackgroundColor(bgColor.toArgb())
                    textView.textSize = textSizeSp
                    markwon.setMarkdown(textView, content)
                }
            )
        }
    }
}
