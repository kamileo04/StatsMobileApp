package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.model.Player
import org.example.project.data.repository.SofaRepository
import org.example.project.ui.components.AppDropdownSelect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val repository = remember { SofaRepository() }
    val scope = rememberCoroutineScope()

    // --- Stan autoryzacji ---
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }

    // --- Stan danych ---
    var teams by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTeam by remember { mutableStateOf<String?>(null) }

    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    var matchLabels by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (label, match_id)
    var selectedMatchId by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // --- Pobieranie drużyn po zalogowaniu ---
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        isLoading = true
        errorMessage = null
        repository.getTeams().fold(
            onSuccess = { list ->
                teams = list
                selectedTeam = list.firstOrNull()
            },
            onFailure = { errorMessage = "Błąd pobierania drużyn: ${it.message}" }
        )
        isLoading = false
    }

    // --- Pobieranie zawodników po wyborze drużyny ---
    LaunchedEffect(selectedTeam) {
        val team = selectedTeam ?: return@LaunchedEffect
        isLoading = true
        errorMessage = null
        players = emptyList()
        selectedPlayer = null
        repository.getPlayers(team).fold(
            onSuccess = { list ->
                players = list
                selectedPlayer = list.firstOrNull()
            },
            onFailure = { errorMessage = "Błąd pobierania zawodników: ${it.message}" }
        )
        isLoading = false
    }

    // --- Pobieranie meczów po wyborze zawodnika ---
    LaunchedEffect(selectedPlayer) {
        val player = selectedPlayer ?: return@LaunchedEffect
        isLoading = true
        errorMessage = null
        matchLabels = emptyList()
        selectedMatchId = null
        repository.getMatchHistory(player.id).fold(
            onSuccess = { list ->
                matchLabels = list.map { it.label to it.matchId }
                selectedMatchId = list.firstOrNull()?.matchId
            },
            onFailure = { errorMessage = "Błąd pobierania historii meczów: ${it.message}" }
        )
        isLoading = false
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isLoggedIn) "SofaMobile" else "SofaMobile – Logowanie",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                if (!isLoggedIn) {
                    // ===== EKRAN LOGOWANIA =====
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Zaloguj się", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it; loginError = null },
                            label = { Text("Hasło") },
                            singleLine = true,
                            isError = loginError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (loginError != null) {
                            Text(loginError!!, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    loginLoading = true
                                    loginError = null
                                    repository.login(loginPassword).fold(
                                        onSuccess = { resp ->
                                            if (resp.success) isLoggedIn = true
                                            else loginError = resp.message
                                        },
                                        onFailure = { loginError = "Błąd połączenia: ${it.message}" }
                                    )
                                    loginLoading = false
                                }
                            },
                            enabled = loginPassword.isNotBlank() && !loginLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loginLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Text("Zaloguj")
                        }
                    }

                } else {
                    // ===== EKRAN GŁÓWNY =====
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Dropdowns – aktywne tylko gdy dane są gotowe
                        AppDropdownSelect(
                            label = "Drużyna",
                            options = teams,
                            selectedOption = selectedTeam ?: "",
                            onOptionSelected = { selectedTeam = it }
                        )

                        AppDropdownSelect(
                            label = "Zawodnik",
                            options = players.map { it.name },
                            selectedOption = selectedPlayer?.name ?: "",
                            onOptionSelected = { name ->
                                selectedPlayer = players.firstOrNull { it.name == name }
                            }
                        )

                        AppDropdownSelect(
                            label = "Mecz",
                            options = matchLabels.map { it.first },
                            selectedOption = matchLabels.firstOrNull { it.second == selectedMatchId }?.first ?: "",
                            onOptionSelected = { label ->
                                selectedMatchId = matchLabels.firstOrNull { it.first == label }?.second
                            }
                        )

                        // Wskaźnik ładowania
                        if (isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        // Błędy
                        errorMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Akcja – pobranie raportu
                        val canFetch = selectedPlayer != null && selectedMatchId != null && !isLoading
                        Button(
                            onClick = {
                                statusMessage = "player_id=${selectedPlayer?.id}, match_id=$selectedMatchId"
                            },
                            enabled = canFetch,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pokaż raport meczowy")
                        }

                        statusMessage?.let {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(it, modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
