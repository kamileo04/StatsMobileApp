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
        title = {
            Text(
                "Wybierz zawodnika do porównania",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (teamsLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                        Text(
                            text = "Wybrany: ${player.name} (ID: ${player.id})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
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
                enabled = selectedPlayer != null
            ) {
                Text("Wybierz")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
