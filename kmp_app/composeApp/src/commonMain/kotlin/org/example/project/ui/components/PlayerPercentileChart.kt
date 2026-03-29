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
import androidx.compose.ui.unit.sp
import org.example.project.data.model.RadarStatItem
import org.example.project.ui.utils.toFormattedString

@Composable
fun PlayerPercentileChart(title: String, stats: List<RadarStatItem>) {
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

            stats.forEach { stat ->
                PercentileBarRow(stat)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PercentileBarRow(stat: RadarStatItem) {
    // Kolor w zależności od percentyla (jak w Sofastreamlit)
    val barColor = when {
        stat.percentile >= 90 -> Color(0xFFD4AF37) // Złoty
        stat.percentile >= 75 -> Color(0xFF4CAF50) // Zielony
        stat.percentile >= 50 -> Color(0xFF8BC34A) // Jasnozielony
        stat.percentile >= 25 -> Color(0xFFFFC107) // Żółty
        else -> Color(0xFFF44336) // Czerwony
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stat.value.toFormattedString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "${stat.percentile}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = barColor,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Pasek postępu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.percentile / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
