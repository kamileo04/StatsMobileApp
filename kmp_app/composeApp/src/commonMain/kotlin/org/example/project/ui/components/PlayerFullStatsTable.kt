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
import org.example.project.data.model.FullTableStatItem
import org.example.project.ui.utils.STATS_CATEGORIES
import org.example.project.ui.utils.toFormattedString
import org.example.project.ui.theme.AppDesign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*

@Composable
fun PlayerFullStatsTable(stats: List<FullTableStatItem>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(AppDesign.SmallSpacing),
        shape = AppDesign.CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
    ) {
        Column(modifier = Modifier.padding(AppDesign.CardInnerPadding)) {
            Text(
                text = "Pełne Statystyki Sezonowe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = AppDesign.ItemSpacing)
            )

            // Nagłówek tabeli
            TableHeaderRow()

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Wiersze Pogrupowane wg Kategorii
            STATS_CATEGORIES.forEach { (catName, keys) ->
                val catStats = stats.filter { it.statKey in keys }
                if (catStats.isNotEmpty()) {
                    CategoryHeader(catName = catName)

                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                    
                    catStats.forEach { stat ->
                        FullStatTableRow(stat)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Inne statystyki
            val mappedKeys = STATS_CATEGORIES.values.flatten()
            val otherStats = stats.filter { it.statKey !in mappedKeys }
            if (otherStats.isNotEmpty()) {
                CategoryHeader(catName = "Inne")

                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )

                otherStats.forEach { stat ->
                    FullStatTableRow(stat)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesign.SmallSpacing, vertical = AppDesign.SmallSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Statystyka",
            Modifier.weight(2f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Suma",
            Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Na 90m",
            Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Percentyl",
            Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryHeader(catName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = AppDesign.SmallSpacing,
            top = AppDesign.SectionSpacing,
            bottom = AppDesign.SmallSpacing
        )
    ) {
        val icon = when (catName) {
            "Ogólne" -> Icons.Default.Info
            "Bramki i xG" -> Icons.Default.Star
            "Bramkarskie" -> Icons.Default.Lock
            "Strzały" -> Icons.AutoMirrored.Filled.Send
            "Podania i Kreacja" -> Icons.Default.Share
            "Drybling i Pojedynki" -> Icons.Default.Person
            "Defensywa" -> Icons.Default.Warning
            else -> Icons.AutoMirrored.Filled.List
        }

        Surface(
            shape = AppDesign.ChipShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.padding(end = AppDesign.SmallSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = catName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDesign.IconSizeSmall)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = catName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesign.SmallSpacing, vertical = 10.dp),
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stat.p90Value.toFormattedString(),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        
        // Percentyl z mikropaskiem
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(AppDesign.BarHeightSmall)
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${stat.percentile}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
    }
}
