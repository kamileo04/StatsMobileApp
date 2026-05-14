package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import org.example.project.data.model.Player
import org.example.project.data.repository.SofaRepository
import org.example.project.sensor.rememberSensorManager
import org.example.project.ui.components.AppDropdownSelect
import org.example.project.ui.components.PhysicsBall
import org.example.project.navigation.Screen
import org.example.project.navigation.BackHandler
import org.example.project.ui.screens.PlayerSeasonScreen
import org.example.project.ui.screens.MatchReportScreen
import org.example.project.ui.screens.FavoritesScreen
import org.example.project.data.repository.FavoritesRepository


val BaseColor = Color(0xFF0458A7)
val AccentColor = Color(0xFFE53935)
val BgColor = Color(0xFFFFFFFF)

val CustomColorScheme = lightColorScheme(
    primary = BaseColor,
    onPrimary = Color.White,
    primaryContainer = BaseColor,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.White,
    secondaryContainer = AccentColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = BgColor,
    onBackground = Color.Black,
    surface = BgColor,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.Black,
    error = AccentColor,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = BaseColor,
    onPrimary = Color.White,
    primaryContainer = BaseColor,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.White,
    secondaryContainer = AccentColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color.White,
    error = AccentColor,
    onError = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val repository = remember { SofaRepository() }
    val favoritesRepository = remember { FavoritesRepository() }
    val scope = rememberCoroutineScope()

    var isDarkMode by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val popScreen: () -> Unit = {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        }
    }

    val navigateTo: (Screen) -> Unit = { screen ->
        screenStack = screenStack + screen
    }

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

    var favoritePlayers by remember { mutableStateOf(favoritesRepository.getFavoritePlayers()) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

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

    val sensorManager = rememberSensorManager()

    BackHandler(isEnabled = screenStack.size > 1, onBack = popScreen)

    val isWebPlatform = remember { getPlatform().name.contains("Web") }

    MaterialTheme(colorScheme = if (isDarkMode) DarkColorScheme else CustomColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
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
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    actions = {
                        Box {
                            if (isWebPlatform) {
                                TextButton(onClick = { showSettingsMenu = true }) {
                                    Text("Ustawienia", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            } else {
                                IconButton(onClick = { showSettingsMenu = true }) {
                                    Text("⚙️", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isDarkMode) "Jasny motyw" else "Ciemny motyw") },
                                    onClick = {
                                        isDarkMode = !isDarkMode
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentModifier = if (isWebPlatform) {
                    Modifier.fillMaxHeight().widthIn(max = 800.dp)
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = contentModifier) {
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
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                                            else loginError = "Nieprawidłowe hasło"
                                        },
                                        onFailure = {
                                            loginError = if (it.message?.contains("401") == true) {
                                                "Nieprawidłowe hasło"
                                            } else {
                                                "Błąd połączenia: ${it.message}"
                                            }
                                        }
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
                    // ===== GŁÓWNY ROUTER NAWIGACJI =====
                    when(val screen = currentScreen) {
                        is Screen.Home -> {
                            Column(
                                modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    showFavoritesOnly = false 
                                    if (selectedPlayer != null && !players.any { it.id == selectedPlayer?.id }) {
                                        selectedPlayer = null
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!showFavoritesOnly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Wszyscy")
                            }
                            Button(
                                onClick = { 
                                    showFavoritesOnly = true 
                                    if (selectedPlayer != null && !favoritePlayers.any { it.id == selectedPlayer?.id }) {
                                        selectedPlayer = null
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (showFavoritesOnly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Ulubieni")
                            }
                        }

                        val displayedPlayers = if (showFavoritesOnly) favoritePlayers else players

                        // Dropdowns – aktywne tylko gdy dane są gotowe
                        if (!showFavoritesOnly) {
                            AppDropdownSelect(
                                label = "Drużyna",
                                options = teams,
                                selectedOption = selectedTeam ?: "",
                                onOptionSelected = { selectedTeam = it }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppDropdownSelect(
                                modifier = Modifier.weight(1f),
                                label = "Zawodnik",
                                options = displayedPlayers.map { it.name },
                                selectedOption = selectedPlayer?.name ?: "",
                                onOptionSelected = { name ->
                                    selectedPlayer = displayedPlayers.firstOrNull { it.name == name }
                                }
                            )

                            if (selectedPlayer != null) {
                                val player = selectedPlayer!!
                                val isFav = favoritePlayers.any { it.id == player.id }
                                val isWeb = getPlatform().name.contains("Web")
                                if (isWeb) {
                                    TextButton(
                                        onClick = {
                                            favoritesRepository.toggleFavorite(player)
                                            favoritePlayers = favoritesRepository.getFavoritePlayers()
                                        },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isFav) "Usuń z ulub." else "Do ulubionych",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            favoritesRepository.toggleFavorite(player)
                                            favoritePlayers = favoritesRepository.getFavoritePlayers()
                                        },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isFav) "★" else "☆",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(56.dp))
                            }
                        }

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
                        // Ostatnia strzelba
                        val canFetch = selectedPlayer != null && selectedMatchId != null && !isLoading
                        Button(
                            onClick = {
                                selectedPlayer?.let { player ->
                                    selectedMatchId?.let { match ->
                                        navigateTo(Screen.MatchReport(player.id, match))
                                    }
                                }
                            },
                            enabled = canFetch,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pokaż raport meczowy")
                        }
                        
                        // NOWY PRZYCISK - PODSUMOWANIE SEZONU
                        Button(
                            onClick = {
                                selectedPlayer?.let {
                                    navigateTo(Screen.PlayerSeason(it.id))
                                }
                            },
                            enabled = selectedPlayer != null && !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pokaż wykresy powiązane z pozycją gracza")
                        }

                        Button(
                            onClick = { navigateTo(Screen.Favorites) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Pokaż listę ulubionych zawodników")
                        }

                        statusMessage?.let {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(it, modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                        is Screen.PlayerSeason -> {
                            PlayerSeasonScreen(
                                playerId = screen.playerId,
                                repository = repository,
                                onBackClick = popScreen
                            )
                        }
                        is Screen.MatchReport -> {
                            MatchReportScreen(
                                playerId = screen.playerId,
                                matchId = screen.matchId,
                                repository = repository,
                                onBackClick = popScreen
                            )
                        }
                        is Screen.Favorites -> {
                            FavoritesScreen(
                                favoritePlayers = favoritePlayers,
                                onShowStats = { playerId ->
                                    navigateTo(Screen.PlayerSeason(playerId))
                                },
                                onBackClick = popScreen
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Ekran nie zaimplementowany")
                                Button(onClick = popScreen) { Text("Wróć") }
                            }
                        }
                    }
                }
                } // end content Box
            }
        } // end Scaffold

        // Physics ball overlay — topmost layer across all screens
        PhysicsBall(sensorManager = sensorManager)
        } // end Box
    } // end MaterialTheme
}
