package com.mdviewer.ui.screens

import android.view.MotionEvent
import android.view.ScaleGestureDetector
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

    var textSizeSp by remember { mutableFloatStateOf(16f) }

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
                    ScrollView(ctx).apply {
                        // Smooth fling scrolling
                        overScrollMode = View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                        clipToPadding = false

                        val textView = TextView(ctx).apply {
                            setBackgroundColor(bgColor.toArgb())
                            setTextColor(textColor.toArgb())
                            textSize = textSizeSp
                        }
                        addView(textView)

                        // Pinch-to-zoom — no size limits
                        val scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                textSizeSp *= detector.scaleFactor
                                textView.textSize = textSizeSp
                                return true
                            }
                        })

                        setOnTouchListener { _, event ->
                            scaleDetector.onTouchEvent(event)
                            // During pinch (2+ fingers), consume event to prevent scroll
                            // Single finger: let ScrollView handle smooth scrolling
                            if (scaleDetector.isInProgress || (event != null && event.pointerCount > 1)) {
                                true
                            } else {
                                false
                            }
                        }
                    }
                },
                update = { scrollView ->
                    val textView = scrollView.getChildAt(0) as TextView
                    textView.setTextColor(textColor.toArgb())
                    textView.setBackgroundColor(bgColor.toArgb())
                    textView.textSize = textSizeSp
                    markwon.setMarkdown(textView, content)
                }
            )
        }
    }
}
