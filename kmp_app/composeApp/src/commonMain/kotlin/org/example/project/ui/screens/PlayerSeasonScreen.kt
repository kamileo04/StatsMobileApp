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
import org.example.project.ui.theme.AppDesign

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesign.ScreenPadding, vertical = AppDesign.SmallSpacing)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(AppDesign.ItemSpacing))
                    Text(
                        "Ładowanie danych...",
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
        } else if (percentilesData != null) {
            val data = percentilesData!!
            
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppDesign.SmallSpacing)
            ) {
                // Player info header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppDesign.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
                ) {
                    Column(
                        modifier = Modifier.padding(AppDesign.CardInnerPadding + 4.dp)
                    ) {
                        Text(
                            text = data.playerName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Pozycja: ${data.position}  •  Grupa: ${data.groupSize} graczy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = AppDesign.TinySpacing)
                        )
                    }
                }

                // Formularz filtrów
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppDesign.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
                ) {
                    Column(modifier = Modifier.padding(AppDesign.CardInnerPadding)) {
                        Text(
                            "Personalizacja porównania",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = AppDesign.SmallSpacing)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDesign.SmallSpacing)
                        ) {
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

                // Comparison section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppDesign.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(AppDesign.CardInnerPadding)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Porównanie z innym zawodnikiem",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(bottom = AppDesign.ItemSpacing)
                                .align(Alignment.Start)
                        )

                        if (comparisonPlayer == null) {
                            Button(
                                onClick = { showPlayerPicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = AppDesign.ButtonShape,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Text(
                                    "Porównaj z innym zawodnikiem",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Porównujesz z: ${comparisonPlayer!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = AppDesign.SmallSpacing)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDesign.SmallSpacing)
                            ) {
                                OutlinedButton(
                                    onClick = { showPlayerPicker = true },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = AppDesign.ButtonShape
                                ) {
                                    Text("Zmień zawodnika")
                                }
                                OutlinedButton(
                                    onClick = {
                                        comparisonPlayer = null
                                        comparisonData = null
                                        comparisonError = null
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = AppDesign.ButtonShape,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDesign.ContentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                            Text(
                                text = "Ładowanie danych porównania...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = AppDesign.SmallSpacing),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                comparisonError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppDesign.CardShapeSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Błąd porównania: $err",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(AppDesign.ContentPadding),
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
