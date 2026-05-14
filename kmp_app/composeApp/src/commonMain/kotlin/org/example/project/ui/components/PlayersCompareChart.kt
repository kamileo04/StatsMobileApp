package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.data.model.RadarStatItem
import org.example.project.ui.utils.toFormattedString
import org.example.project.ui.theme.AppDesign

@Composable
fun PlayersCompareChart(
    title: String,
    player1Name: String,
    player2Name: String,
    stats1: List<RadarStatItem>,
    stats2: List<RadarStatItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(AppDesign.SmallSpacing),
        shape = AppDesign.CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
    ) {
        Column(modifier = Modifier.padding(AppDesign.CardInnerPadding + 4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = AppDesign.ContentPadding)
            )

            val allLabels = (stats1.map { it.label } + stats2.map { it.label }).distinct()

            allLabels.forEach { label ->
                val stat1 = stats1.find { it.label == label }
                val stat2 = stats2.find { it.label == label }

                ComparePercentileBarRow(label, player1Name, player2Name, stat1, stat2)
                Spacer(modifier = Modifier.height(AppDesign.ContentPadding))
            }
        }
    }
}

@Composable
private fun ComparePercentileBarRow(
    label: String,
    player1Name: String,
    player2Name: String,
    stat1: RadarStatItem?,
    stat2: RadarStatItem?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(6.dp))

        // Gracz 1
        PlayerStatBar(playerName = player1Name, stat = stat1)
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Gracz 2
        PlayerStatBar(playerName = player2Name, stat = stat2)
    }
}

@Composable
private fun PlayerStatBar(playerName: String, stat: RadarStatItem?) {
    val percentile = stat?.percentile ?: 0
    val valueStr = stat?.value?.toFormattedString() ?: "Brak"

    val barColor = when {
        percentile >= 90 -> Color(0xFFD4AF37) // Złoty
        percentile >= 75 -> Color(0xFF4CAF50) // Zielony
        percentile >= 50 -> Color(0xFF8BC34A) // Jasnozielony
        percentile >= 25 -> Color(0xFFFFC107) // Żółty
        percentile > 0 -> Color(0xFFF44336) // Czerwony
        else -> Color.Gray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(100.dp),
            maxLines = 1
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(AppDesign.BarHeight)
                .clip(AppDesign.BarShape)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            if (percentile > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentile / 100f)
                        .fillMaxHeight()
                        .clip(AppDesign.BarShape)
                        .background(barColor)
                )
            }
        }
        
        Row(
            modifier = Modifier.width(84.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valueStr,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "$percentile",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = barColor,
                textAlign = TextAlign.End,
                modifier = Modifier.width(28.dp)
            )
        }
    }
}
