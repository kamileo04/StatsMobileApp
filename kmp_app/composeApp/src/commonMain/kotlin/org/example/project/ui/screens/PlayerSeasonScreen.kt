package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.model.Player
import org.example.project.data.model.PlayerPercentilesResponse
import org.example.project.data.repository.SofaRepository
import org.example.project.ui.components.AppDropdownSelect
import org.example.project.ui.components.ComparisonRadarChart
import org.example.project.ui.components.PlayerFullStatsTable
import org.example.project.ui.components.PlayerPercentileChart
import org.example.project.ui.components.PlayerPickerDialog
import org.example.project.ui.components.PlayerRadarChart

@Composable
fun PlayerSeasonScreen(
    playerId: Int,
    repository: SofaRepository,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var percentilesData by remember { mutableStateOf<PlayerPercentilesResponse?>(null) }
    
    // Filtry
    var selectedTemplate by remember { mutableStateOf("Auto") }
    val templates = listOf("Auto", "ST", "W", "CAM", "RM/LM", "CM/CDM", "LB", "RB", "LB/RB", "CB", "GK")

    var selectedMinMinutes by remember { mutableStateOf("300") }
    val minutesOptions = listOf("0", "100", "300", "500", "900")

    var showPlayerPicker by remember { mutableStateOf(false) }
    var comparisonPlayer by remember { mutableStateOf<Player?>(null) }
    var comparisonData by remember { mutableStateOf<PlayerPercentilesResponse?>(null) }
    var isComparisonLoading by remember { mutableStateOf(false) }
    var comparisonError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(playerId, selectedTemplate, selectedMinMinutes) {
        isLoading = true
        val minMinsInt = selectedMinMinutes.toIntOrNull() ?: 300
        repository.getPlayerPercentiles(playerId, selectedTemplate, minMinsInt)
            .onSuccess { data ->
                percentilesData = data
                errorMessage = null
            }
            .onFailure { error ->
                errorMessage = error.message ?: "Wystąpił nieznany błąd"
            }
        isLoading = false
    }

    LaunchedEffect(comparisonPlayer, selectedTemplate, selectedMinMinutes) {
        val player = comparisonPlayer ?: run {
            comparisonData = null
            return@LaunchedEffect
        }
        isComparisonLoading = true
        comparisonError = null
        val minMinsInt = selectedMinMinutes.toIntOrNull() ?: 300
        repository.getPlayerPercentiles(player.id, selectedTemplate, minMinsInt)
            .onSuccess { data ->
                comparisonData = data
                comparisonError = null
            }
            .onFailure { error ->
                comparisonError = error.message ?: "Błąd pobierania danych porównania"
                comparisonData = null
            }
        isComparisonLoading = false
    }

    if (showPlayerPicker) {
        PlayerPickerDialog(
            repository = repository,
            onPlayerSelected = { player ->
                comparisonPlayer = player
                showPlayerPicker = false
            },
            onDismiss = { showPlayerPicker = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Powrót")
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Błąd: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else if (percentilesData != null) {
            val data = percentilesData!!
            
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Player: ${data.playerName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = "Pozycja: ${data.position} | Grupa: ${data.groupSize} graczy",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                )
                // Formularz filtrów
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Personalizacja porównania", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Pozycja (Wykres)",
                                    options = templates,
                                    selectedOption = selectedTemplate,
                                    onOptionSelected = { selectedTemplate = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Min. Minuty (%yl)",
                                    options = minutesOptions,
                                    selectedOption = selectedMinMinutes,
                                    onOptionSelected = { selectedMinMinutes = it }
                                )
                            }
                        }
                    }
                }
                
                PlayerPercentileChart(
                    title = "Profil (${data.position}) vs $selectedMinMinutes+ min", 
                    stats = data.radarChart
                )

                PlayerRadarChart(
                    title = "Radar Chart (${data.position})",
                    stats = data.radarChart
                )

                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Porównanie z innym zawodnikiem",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                        )

                        if (comparisonPlayer == null) {
                            Button(
                                onClick = { showPlayerPicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Porównaj z innym zawodnikiem")
                            }
                        } else {
                            Text(
                                text = "Porównujesz z: ${comparisonPlayer!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPlayerPicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Zmień zawodnika")
                                }
                                OutlinedButton(
                                    onClick = {
                                        comparisonPlayer = null
                                        comparisonData = null
                                        comparisonError = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Usuń porównanie")
                                }
                            }
                        }
                    }
                }

                if (isComparisonLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Text(
                                text = "Ładowanie danych porównania...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                comparisonError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Błąd porównania: $err",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (comparisonData != null && !isComparisonLoading) {
                    val cmpData = comparisonData!!
                    ComparisonRadarChart(
                        title = "Porównanie radarowe (${data.position})",
                        playerAName = data.playerName,
                        playerAStats = data.radarChart,
                        playerBName = cmpData.playerName,
                        playerBStats = cmpData.radarChart
                    )
                }
                
                PlayerFullStatsTable(stats = data.fullTable)
            }
        }
    }
}
