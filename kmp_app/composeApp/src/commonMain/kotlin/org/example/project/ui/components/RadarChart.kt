package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.model.RadarStatItem
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun PlayerRadarChart(
    title: String,
    stats: List<RadarStatItem>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).padding(16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2f

                    val numPoints = stats.size
                    val angleStep = (2 * PI / numPoints).toFloat()

                    // Draw grid lines
                    val numRings = 5
                    for (i in 1..numRings) {
                        val r = radius * (i.toFloat() / numRings)
                        val path = Path()
                        for (j in 0 until numPoints) {
                            val angle = j * angleStep - kotlin.math.PI.toFloat() / 2f
                            val x = center.x + r * cos(angle)
                            val y = center.y + r * sin(angle)
                            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path,
                            color = Color.Gray.copy(alpha = 0.3f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw axes and labels
                    for (i in 0 until numPoints) {
                        val angle = i * angleStep - kotlin.math.PI.toFloat() / 2f
                        val x = center.x + radius * cos(angle)
                        val y = center.y + radius * sin(angle)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = center,
                            end = Offset(x, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Draw labels
                        val labelRadius = radius * 1.25f
                        val labelX = center.x + labelRadius * cos(angle)
                        val labelY = center.y + labelRadius * sin(angle)

                        val textLayoutResult = textMeasurer.measure(
                            text = stats[i].label,
                            style = TextStyle(color = onSurfaceColor, fontSize = 10.sp)
                        )

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = labelX - textLayoutResult.size.width / 2f,
                                y = labelY - textLayoutResult.size.height / 2f
                            )
                        )
                    }

                    // Draw data
                    val dataPath = Path()
                    for (i in 0 until numPoints) {
                        val stat = stats[i]
                        val percentile = stat.percentile.toFloat().coerceIn(0f, 100f) / 100f
                        val r = radius * percentile
                        val angle = i * angleStep - kotlin.math.PI.toFloat() / 2f
                        val x = center.x + r * cos(angle)
                        val y = center.y + r * sin(angle)

                        if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                    }
                    dataPath.close()

                    drawPath(dataPath, color = primaryColor.copy(alpha = 0.4f))
                    drawPath(dataPath, color = primaryColor, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }
    }
}
