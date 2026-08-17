package com.apptime.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apptime.data.AppUsage
import com.apptime.ui.screens.formatMs

private val CHART_COLORS = listOf(
    Color(0xFF7B68EE),
    Color(0xFF00BCD4),
    Color(0xFFFF6B6B),
    Color(0xFF4CAF50),
    Color(0xFFFFB347),
    Color(0xFFDA70D6),
)

private data class ChartEntry(val label: String, val ms: Long, val color: Color)

@Composable
fun DonutChart(
    items: List<AppUsage>,
    totalMs: Long,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() || totalMs == 0L) return

    val top5 = items.take(5)
    val othersMs = items.drop(5).sumOf { it.totalTimeMs }

    val entries = buildList {
        top5.forEachIndexed { i, app ->
            add(ChartEntry(app.appName, app.totalTimeMs, CHART_COLORS[i % CHART_COLORS.size]))
        }
        if (othersMs > 0) add(ChartEntry("Others", othersMs, Color(0xFF888888)))
    }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 44.dp.toPx()
                val radius = (size.minDimension / 2f) - strokeWidth / 2f
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)
                val gap = 4f
                var startAngle = -90f

                drawArc(
                    color = surfaceVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = topLeft,
                    size = arcSize
                )

                entries.forEach { entry ->
                    val fraction = entry.ms.toFloat() / totalMs
                    val fullSweep = fraction * 360f
                    val sweep = ((fullSweep - gap).coerceAtLeast(0f)) * animatedProgress
                    drawArc(
                        color = entry.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize
                    )
                    startAngle += fullSweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatMs(totalMs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
                Text(
                    text = "total",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.55f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            entries.forEach { entry ->
                val pct = (entry.ms.toFloat() / totalMs * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(entry.color)
                    )
                    Text(
                        text = entry.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = onSurface
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text = formatMs(entry.ms),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = entry.color
                    )
                }
            }
        }
    }
}
