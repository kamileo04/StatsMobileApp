package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.model.RadarStatItem
import org.example.project.ui.theme.AppDesign
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun ComparisonRadarChart(
    title: String,
    playerAName: String,
    playerAStats: List<RadarStatItem>,
    playerBName: String,
    playerBStats: List<RadarStatItem>,
    modifier: Modifier = Modifier
) {
    if (playerAStats.isEmpty() || playerBStats.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary   
    val secondaryColor = MaterialTheme.colorScheme.secondary 
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()

    val labels = playerAStats.map { it.label }
    val numPoints = labels.size

    Card(
        modifier = modifier.fillMaxWidth().padding(AppDesign.SmallSpacing),
        shape = AppDesign.CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(AppDesign.CardInnerPadding + 4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = AppDesign.SmallSpacing)
                    .align(Alignment.Start)
            )

            // Legend row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppDesign.ItemSpacing),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = primaryColor, name = playerAName)
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = secondaryColor, name = playerBName)
            }

            // Wykres
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).padding(AppDesign.ContentPadding)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2f
                    val angleStep = (2 * kotlin.math.PI / numPoints).toFloat()

                    // Siatka (grid rings)
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
                            color = gridColor.copy(alpha = 0.5f),
                            style = Stroke(width = 0.8.dp.toPx())
                        )
                    }

                    for (i in 0 until numPoints) {
                        val angle = i * angleStep - kotlin.math.PI.toFloat() / 2f
                        val x = center.x + radius * cos(angle)
                        val y = center.y + radius * sin(angle)

                        // Oś
                        drawLine(
                            color = gridColor.copy(alpha = 0.6f),
                            start = center,
                            end = Offset(x, y),
                            strokeWidth = 0.8.dp.toPx()
                        )

                        // Etykieta
                        val labelRadius = radius * 1.25f
                        val labelX = center.x + labelRadius * cos(angle)
                        val labelY = center.y + labelRadius * sin(angle)

                        val textLayoutResult = textMeasurer.measure(
                            text = labels[i],
                            style = TextStyle(color = onSurfaceColor, fontSize = 9.sp)
                        )

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = labelX - textLayoutResult.size.width / 2f,
                                y = labelY - textLayoutResult.size.height / 2f
                            )
                        )
                    }

                    drawRadarShape(
                        stats = playerAStats,
                        center = center,
                        radius = radius,
                        angleStep = angleStep,
                        fillColor = primaryColor.copy(alpha = 0.25f),
                        strokeColor = primaryColor,
                        strokeWidth = 2.5.dp.toPx()
                    )

                    drawRadarShape(
                        stats = playerBStats,
                        center = center,
                        radius = radius,
                        angleStep = angleStep,
                        fillColor = secondaryColor.copy(alpha = 0.20f),
                        strokeColor = secondaryColor,
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // Data table below the chart
            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppDesign.SmallSpacing),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Statystyka",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.4f)
                    )
                    Text(
                        text = playerAName.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = playerBName.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                val bByKey = playerBStats.associateBy { it.statKey }
                playerAStats.forEach { aStat ->
                    val bStat = bByKey[aStat.statKey]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = aStat.label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = "${aStat.percentile}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${bStat?.percentile ?: "-"}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun DrawScope.drawRadarShape(
    stats: List<RadarStatItem>,
    center: Offset,
    radius: Float,
    angleStep: Float,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    val path = Path()
    for (i in stats.indices) {
        val percentile = stats[i].percentile.toFloat().coerceIn(0f, 100f) / 100f
        val r = radius * percentile
        val angle = i * angleStep - kotlin.math.PI.toFloat() / 2f
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path, color = fillColor)
    drawPath(path, color = strokeColor, style = Stroke(width = strokeWidth))
}
