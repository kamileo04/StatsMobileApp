package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.model.Player
import org.example.project.data.repository.SofaRepository
import org.example.project.ui.theme.AppDesign


@Composable
fun PlayerPickerDialog(
    repository: SofaRepository,
    onPlayerSelected: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var teams by remember { mutableStateOf<List<String>>(emptyList()) }
    var teamsLoading by remember { mutableStateOf(true) }
    var selectedTeam by remember { mutableStateOf<String?>(null) }

    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var playersLoading by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        teamsLoading = true
        repository.getTeams()
            .onSuccess { list ->
                teams = list
                errorMessage = null
            }
            .onFailure { errorMessage = "Błąd ładowania drużyn: ${it.message}" }
        teamsLoading = false
    }

    LaunchedEffect(selectedTeam) {
        val team = selectedTeam ?: return@LaunchedEffect
        playersLoading = true
        selectedPlayer = null
        players = emptyList()
        repository.getPlayers(team)
            .onSuccess { list ->
                players = list
                errorMessage = null
            }
            .onFailure { errorMessage = "Błąd ładowania zawodników: ${it.message}" }
        playersLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDesign.CardShape,
        title = {
            Text(
                "Wybierz zawodnika do porównania",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
            ) {
                if (teamsLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDesign.ContentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                } else {
                    AppDropdownSelect(
                        label = "Drużyna",
                        options = teams,
                        selectedOption = selectedTeam ?: "",
                        modifier = Modifier.fillMaxWidth(),
                        onOptionSelected = { selectedTeam = it }
                    )

                    if (playersLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppDesign.SmallSpacing),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (selectedTeam != null && players.isNotEmpty()) {
                        AppDropdownSelect(
                            label = "Zawodnik",
                            options = players.map { it.name },
                            selectedOption = selectedPlayer?.name ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            onOptionSelected = { name ->
                                selectedPlayer = players.firstOrNull { it.name == name }
                            }
                        )
                    }

                    selectedPlayer?.let { player ->
                        Card(
                            shape = AppDesign.ChipShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "Wybrany: ${player.name} (ID: ${player.id})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(
                                    horizontal = AppDesign.ItemSpacing,
                                    vertical = AppDesign.SmallSpacing
                                )
                            )
                        }
                    }
                }

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlayer?.let { onPlayerSelected(it) }
                },
                enabled = selectedPlayer != null,
                shape = AppDesign.ButtonShape
            ) {
                Text("Wybierz", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = AppDesign.ButtonShape
            ) {
                Text("Anuluj")
            }
        }
    )
}
