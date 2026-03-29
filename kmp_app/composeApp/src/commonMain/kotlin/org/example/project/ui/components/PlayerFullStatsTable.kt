package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.example.project.data.model.FullTableStatItem
import org.example.project.ui.utils.toFormattedString

@Composable
fun PlayerFullStatsTable(stats: List<FullTableStatItem>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Pełne Statystyki Sezonowe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            // Nagłówek tabeli
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Statystyka", Modifier.weight(2f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text("Suma", Modifier.weight(0.8f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text("Na 90m", Modifier.weight(0.8f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text("Percentyl", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            
            Divider()

            // Wiersze
            stats.forEach { stat ->
                FullStatTableRow(stat)
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun FullStatTableRow(stat: FullTableStatItem) {
    val barColor = when {
        stat.percentile >= 90 -> Color(0xFFD4AF37)
        stat.percentile >= 75 -> Color(0xFF4CAF50)
        stat.percentile >= 50 -> Color(0xFF8BC34A)
        stat.percentile >= 25 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stat.label,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stat.totalValue.toFormattedString(),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stat.p90Value.toFormattedString(),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall
        )
        
        // Percentyl z mikropaskiem
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .padding(end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(stat.percentile / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${stat.percentile}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
    }
}
