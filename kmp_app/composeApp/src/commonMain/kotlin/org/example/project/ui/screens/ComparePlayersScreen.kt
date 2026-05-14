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
import org.example.project.ui.theme.AppDesign

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesign.ScreenPadding, vertical = AppDesign.SmallSpacing)
    ) {
        if (isTeamsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(AppDesign.ItemSpacing))
                    Text(
                        "Ładowanie drużyn...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
            ) {
                Text(
                    text = "Porównanie zawodników",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = AppDesign.TinySpacing)
                )

                // Filtry wspólne dla obu
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
                PlayerSelectionCard(
                    label = "Zawodnik 1",
                    teams = teams,
                    selectedTeam = selectedTeam1,
                    onTeamSelected = { selectedTeam1 = it },
                    players = players1,
                    selectedPlayer = selectedPlayer1,
                    onPlayerSelected = { name -> selectedPlayer1 = players1.find { it.name == name } }
                )

                // Gracz 2 Wybór
                PlayerSelectionCard(
                    label = "Zawodnik 2",
                    teams = teams,
                    selectedTeam = selectedTeam2,
                    onTeamSelected = { selectedTeam2 = it },
                    players = players2,
                    selectedPlayer = selectedPlayer2,
                    onPlayerSelected = { name -> selectedPlayer2 = players2.find { it.name == name } }
                )

                if (errorMessage != null) {
                    Card(
                        shape = AppDesign.CardShapeSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Błąd: $errorMessage",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(AppDesign.ContentPadding),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isPlayer1Loading || isPlayer2Loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(AppDesign.ContentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppDesign.CardShapeSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Wybierz drugiego zawodnika do porównania",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppDesign.ContentPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSelectionCard(
    label: String,
    teams: List<String>,
    selectedTeam: String?,
    onTeamSelected: (String) -> Unit,
    players: List<Player>,
    selectedPlayer: Player?,
    onPlayerSelected: (String) -> Unit
) {
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
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = AppDesign.TinySpacing)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppDesign.SmallSpacing)) {
                Box(modifier = Modifier.weight(1f)) {
                    AppDropdownSelect(
                        label = "Drużyna",
                        options = teams,
                        selectedOption = selectedTeam ?: "",
                        onOptionSelected = onTeamSelected
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppDropdownSelect(
                        label = "Zawodnik",
                        options = players.map { it.name },
                        selectedOption = selectedPlayer?.name ?: "Wybierz zawodnika",
                        onOptionSelected = onPlayerSelected
                    )
                }
            }
        }
    }
}
