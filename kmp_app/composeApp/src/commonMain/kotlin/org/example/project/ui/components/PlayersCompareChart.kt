package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PlayersCompareChart(
    title: String,
    player1Name: String,
    player2Name: String,
    stats1: List<RadarStatItem>,
    stats2: List<RadarStatItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val allLabels = (stats1.map { it.label } + stats2.map { it.label }).distinct()

            allLabels.forEach { label ->
                val stat1 = stats1.find { it.label == label }
                val stat2 = stats2.find { it.label == label }

                ComparePercentileBarRow(label, player1Name, player2Name, stat1, stat2)
                Spacer(modifier = Modifier.height(12.dp))
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
        
        Spacer(modifier = Modifier.height(4.dp))

        // Gracz 1
        PlayerStatBar(playerName = player1Name, stat = stat1)
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            if (percentile > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentile / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
        
        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valueStr,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = "$percentile",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = barColor,
                textAlign = TextAlign.End,
                modifier = Modifier.width(24.dp)
            )
        }
    }
}
