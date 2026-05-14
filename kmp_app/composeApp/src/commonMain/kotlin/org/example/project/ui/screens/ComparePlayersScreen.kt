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
import org.example.project.ui.components.PlayersCompareChart

@Composable
fun ComparePlayersScreen(
    initialPlayerId: Int?,
    repository: SofaRepository,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Stany pobierania
    var isTeamsLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var teams by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Gracz 1
    var selectedTeam1 by remember { mutableStateOf<String?>(null) }
    var players1 by remember { mutableStateOf<List<Player>>(emptyList()) }
    var selectedPlayer1 by remember { mutableStateOf<Player?>(null) }
    var percentilesData1 by remember { mutableStateOf<PlayerPercentilesResponse?>(null) }
    var isPlayer1Loading by remember { mutableStateOf(false) }

    // Gracz 2
    var selectedTeam2 by remember { mutableStateOf<String?>(null) }
    var players2 by remember { mutableStateOf<List<Player>>(emptyList()) }
    var selectedPlayer2 by remember { mutableStateOf<Player?>(null) }
    var percentilesData2 by remember { mutableStateOf<PlayerPercentilesResponse?>(null) }
    var isPlayer2Loading by remember { mutableStateOf(false) }

    // Filtry
    var selectedTemplate by remember { mutableStateOf("Auto") }
    val templates = listOf("Auto", "ST", "W", "CAM", "RM/LM", "CM/CDM", "LB", "RB", "LB/RB", "CB", "GK")

    var selectedMinMinutes by remember { mutableStateOf("300") }
    val minutesOptions = listOf("0", "100", "300", "500", "900")

    // Inicjalizacja drużyn
    LaunchedEffect(Unit) {
        isTeamsLoading = true
        repository.getTeams().onSuccess { list ->
            teams = list
            if (initialPlayerId != null) {
                // Znajdź drużynę pierwszego gracza i samego gracza
                coroutineScope.launch {
                    for (team in list) {
                        repository.getPlayers(team).onSuccess { pList ->
                            val p = pList.find { it.id == initialPlayerId }
                            if (p != null) {
                                selectedTeam1 = team
                                players1 = pList
                                selectedPlayer1 = p
                            }
                        }
                    }
                }
            } else {
                selectedTeam1 = list.firstOrNull()
            }
            selectedTeam2 = list.firstOrNull()
        }.onFailure { 
            errorMessage = "Błąd pobierania drużyn"
        }
        isTeamsLoading = false
    }

    // Pobieranie zawodników dla Drużyny 1
    LaunchedEffect(selectedTeam1) {
        val team = selectedTeam1 ?: return@LaunchedEffect
        repository.getPlayers(team).onSuccess { list ->
            players1 = list
            if (selectedPlayer1 == null || !list.any { it.id == selectedPlayer1?.id }) {
                selectedPlayer1 = null
            }
        }.onFailure { errorMessage = "Błąd pobierania zawodników" }
    }

    // Pobieranie zawodników dla Drużyny 2
    LaunchedEffect(selectedTeam2) {
        val team = selectedTeam2 ?: return@LaunchedEffect
        repository.getPlayers(team).onSuccess { list ->
            players2 = list
            selectedPlayer2 = null // Pusty na starcie
        }.onFailure { errorMessage = "Błąd pobierania zawodników" }
    }

    // Pobieranie danych dla Gracza 1
    LaunchedEffect(selectedPlayer1, selectedTemplate, selectedMinMinutes) {
        val p = selectedPlayer1
        if (p != null) {
            isPlayer1Loading = true
            val minMinsInt = selectedMinMinutes.toIntOrNull() ?: 300
            repository.getPlayerPercentiles(p.id, selectedTemplate, minMinsInt).onSuccess { data ->
                percentilesData1 = data
            }
            isPlayer1Loading = false
        } else {
            percentilesData1 = null
        }
    }

    // Pobieranie danych dla Gracza 2
    LaunchedEffect(selectedPlayer2, selectedTemplate, selectedMinMinutes) {
        val p = selectedPlayer2
        if (p != null) {
            isPlayer2Loading = true
            val minMinsInt = selectedMinMinutes.toIntOrNull() ?: 300
            repository.getPlayerPercentiles(p.id, selectedTemplate, minMinsInt).onSuccess { data ->
                percentilesData2 = data
            }
            isPlayer2Loading = false
        } else {
            percentilesData2 = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Powrót")
        }

        if (isTeamsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                
                Text(
                    text = "Porównanie zawodników",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Filtry wspólne dla obu
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Personalizacja porównania", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Pozycja",
                                    options = templates,
                                    selectedOption = selectedTemplate,
                                    onOptionSelected = { selectedTemplate = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Min. Minuty",
                                    options = minutesOptions,
                                    selectedOption = selectedMinMinutes,
                                    onOptionSelected = { selectedMinMinutes = it }
                                )
                            }
                        }
                    }
                }

                // Gracz 1 Wybór
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Zawodnik 1", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Drużyna",
                                    options = teams,
                                    selectedOption = selectedTeam1 ?: "",
                                    onOptionSelected = { selectedTeam1 = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Zawodnik",
                                    options = players1.map { it.name },
                                    selectedOption = selectedPlayer1?.name ?: "Wybierz zawodnika",
                                    onOptionSelected = { name -> selectedPlayer1 = players1.find { it.name == name } }
                                )
                            }
                        }
                    }
                }

                // Gracz 2 Wybór
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Zawodnik 2", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Drużyna",
                                    options = teams,
                                    selectedOption = selectedTeam2 ?: "",
                                    onOptionSelected = { selectedTeam2 = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AppDropdownSelect(
                                    label = "Zawodnik",
                                    options = players2.map { it.name },
                                    selectedOption = selectedPlayer2?.name ?: "Wybierz zawodnika",
                                    onOptionSelected = { name -> selectedPlayer2 = players2.find { it.name == name } }
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(text = "Błąd: $errorMessage", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                }

                if (isPlayer1Loading || isPlayer2Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (percentilesData1 != null && percentilesData2 != null) {
                    val data1 = percentilesData1!!
                    val data2 = percentilesData2!!

                    PlayersCompareChart(
                        title = "Porównanie Profilu (${data1.position})",
                        player1Name = data1.playerName,
                        player2Name = data2.playerName,
                        stats1 = data1.radarChart,
                        stats2 = data2.radarChart
                    )
                } else if (percentilesData1 != null) {
                    Text(
                        text = "Wybierz drugiego zawodnika do porównania",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
