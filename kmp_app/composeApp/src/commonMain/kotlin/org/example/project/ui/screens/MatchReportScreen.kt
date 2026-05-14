package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.async
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.model.MatchReportResponse
import org.example.project.data.model.SeasonStatsResponse
import org.example.project.data.repository.SofaRepository
import org.example.project.ui.utils.LOWER_IS_BETTER_STATS
import org.example.project.ui.utils.STATS_CATEGORIES
import org.example.project.ui.utils.mapStatKeyToPolish
import org.example.project.ui.utils.toFormattedString
import org.example.project.ui.theme.AppDesign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*

data class MatchComparisonRow(
    val key: String,
    val label: String,
    val matchValue: Double,
    val seasonAvg: Double
)

@Composable
fun MatchReportScreen(
    playerId: Int,
    matchId: String,
    repository: SofaRepository,
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var matchInfo by remember { mutableStateOf<MatchReportResponse?>(null) }
    var comparisonRows by remember { mutableStateOf<List<MatchComparisonRow>>(emptyList()) }

    LaunchedEffect(playerId, matchId) {
        isLoading = true
        errorMessage = null
        
        try {
            val matchDeferred = async { repository.getMatchReport(playerId, matchId).getOrThrow() }
            val seasonDeferred = async { repository.getSeasonStats(playerId).getOrThrow() }
            
            val matchData = matchDeferred.await()
            val seasonData = seasonDeferred.await()
            
            matchInfo = matchData
            
            val seasonTotalMins = seasonData.stats["totalSeasonMinutes_stats"]?.jsonPrimitive?.doubleOrNull 
                ?: seasonData.stats["minutesPlayed"]?.jsonPrimitive?.doubleOrNull ?: 1.0
                
            val matchMinsDouble = matchData.minutes.toDouble().coerceAtLeast(1.0)
                
            val rows = mutableListOf<MatchComparisonRow>()
            
            // Filtrujemy klucze które są tekstami i niechciane
            val ignored = setOf(
                "id", "team", "name", "position", "Calculated Position", "slug", "teamName", "appearances",
                "ownGoals", "ratingVersions_original", "savePercentage", "totalOffside", "outfielderBlock"
            )
            
            for ((key, element) in matchData.stats) {
                if (key in ignored) continue
                var matchVal = element.jsonPrimitive.doubleOrNull
                if (matchVal != null) {
                    val seasonElement = seasonData.stats[key]
                    var seasonVal = seasonElement?.jsonPrimitive?.doubleOrNull ?: 0.0
                    
                    // Normalizacja do p90, chyba że to procent, minuty lub ocena
                    if (key != "rating" && key != "minutesPlayed" && !key.contains("Percentage") && !key.contains("%")) {
                        val mins = if (seasonTotalMins > 0) seasonTotalMins else 1.0
                        seasonVal = (seasonVal / mins) * 90.0
                        matchVal = (matchVal / matchMinsDouble) * 90.0
                    } else if (key == "rating") {
                        // Rating sezonowy w bazie jest zwykle uśredniony
                        val apps = seasonData.stats["appearances"]?.jsonPrimitive?.doubleOrNull ?: 1.0
                        if (apps > 0) seasonVal /= apps
                    }
                    
                    rows.add(
                        MatchComparisonRow(
                            key = key,
                            label = mapStatKeyToPolish(key),
                            matchValue = matchVal,
                            seasonAvg = seasonVal
                        )
                    )
                }
            }
            
            comparisonRows = rows.sortedBy { it.label }
            
        } catch (e: Exception) {
            errorMessage = e.message ?: "Wystąpił nieznany błąd"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDesign.ScreenPadding)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(AppDesign.ItemSpacing))
                    Text(
                        "Ładowanie raportu...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    shape = AppDesign.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Błąd: $errorMessage",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(AppDesign.CardInnerPadding)
                    )
                }
            }
        } else if (matchInfo != null) {
            // Match title header
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = AppDesign.ItemSpacing),
                shape = AppDesign.CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
            ) {
                Text(
                    text = matchInfo!!.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(AppDesign.CardInnerPadding)
                )
            }

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = AppDesign.CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
            ) {
                Column(modifier = Modifier.padding(AppDesign.CardInnerPadding)) {
                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDesign.SmallSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Statystyka",
                            Modifier.weight(2f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Mecz",
                            Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Średnia (Sezon)",
                            Modifier.weight(1.2f),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        
                        STATS_CATEGORIES.forEach { (catName, keys) ->
                            val catRows = comparisonRows.filter { it.key in keys }
                            if (catRows.isNotEmpty()) {
                                item {
                                    MatchCategoryHeader(catName = catName)
                                    HorizontalDivider(
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    )
                                }
                                items(catRows) { row ->
                                    MatchRowUI(row)
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCategoryHeader(catName: String) {
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
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
fun MatchRowUI(row: MatchComparisonRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDesign.ItemSpacing, horizontal = AppDesign.TinySpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.label, 
            modifier = Modifier.weight(2f), 
            style = MaterialTheme.typography.bodySmall
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = row.matchValue.toFormattedString(),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            ComparisonBar(
                match = row.matchValue,
                season = row.seasonAvg,
                isLowerBetter = LOWER_IS_BETTER_STATS.contains(row.key),
                modifier = Modifier.width(44.dp)
            )
        }
        Text(
            text = row.seasonAvg.toFormattedString(),
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ComparisonBar(match: Double, season: Double, isLowerBetter: Boolean, modifier: Modifier = Modifier) {
    val maxVal = maxOf(match, season).coerceAtLeast(0.01)
    val ratio = (match / maxVal).coerceIn(0.0, 1.0).toFloat()
    
    val color = if (isLowerBetter) {
        if (match < season) Color(0xFF4CAF50) else if (match > season) Color(0xFFF44336) else Color.Gray
    } else {
        if (match > season) Color(0xFF4CAF50) else if (match < season) Color(0xFFF44336) else Color.Gray
    }
    
    Box(
        modifier = modifier
            .height(AppDesign.MiniBarHeight)
            .clip(AppDesign.BarShape)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(ratio)
                .clip(AppDesign.BarShape)
                .background(color)
        )
    }
}
