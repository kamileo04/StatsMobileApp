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
fun PlayerPercentileChart(title: String, stats: List<RadarStatItem>) {
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

            stats.forEach { stat ->
                PercentileBarRow(stat)
                Spacer(modifier = Modifier.height(AppDesign.ItemSpacing))
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
                modifier = Modifier.padding(horizontal = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stat.percentile}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = barColor,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Pasek postępu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDesign.BarHeight)
                .clip(AppDesign.BarShape)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.percentile / 100f)
                    .fillMaxHeight()
                    .clip(AppDesign.BarShape)
                    .background(barColor)
            )
        }
    }
}
