package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
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
import org.example.project.ui.screens.ComparePlayersScreen
import org.example.project.data.repository.FavoritesRepository
import org.example.project.ui.theme.AppDesign


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
    secondaryContainer = AccentColor.copy(alpha = 0.12f),
    onSecondaryContainer = AccentColor,
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C20),
    surface = BgColor,
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFF0F2F8),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFFD0D3DC),
    outlineVariant = Color(0xFFE0E2EB),
    error = AccentColor,
    onError = Color.White,
    errorContainer = AccentColor.copy(alpha = 0.1f),
    onErrorContainer = AccentColor,
    surfaceTint = BaseColor
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6EABF5),
    onPrimary = Color(0xFF003063),
    primaryContainer = BaseColor,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFF6F61),
    onSecondary = Color.White,
    secondaryContainer = AccentColor.copy(alpha = 0.20f),
    onSecondaryContainer = Color(0xFFFF8A80),
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF1A1C22),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF252830),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF3E4149),
    outlineVariant = Color(0xFF2E3038),
    error = Color(0xFFFF6F61),
    onError = Color.White,
    errorContainer = AccentColor.copy(alpha = 0.15f),
    onErrorContainer = Color(0xFFFF8A80),
    surfaceTint = Color(0xFF6EABF5)
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
                selectedPlayer = null
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
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isLoggedIn) "SofaMobile" else "SofaMobile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (screenStack.size > 1) {
                                IconButton(onClick = popScreen) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Wróć",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        actions = {
                            Box {
                                IconButton(onClick = { showSettingsMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ustawienia",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
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
                        Modifier.fillMaxHeight().widthIn(max = AppDesign.MaxContentWidth)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(modifier = contentModifier) {
                        if (!isLoggedIn) {
                            // ===== EKRAN LOGOWANIA =====
                            LoginScreen(
                                loginPassword = loginPassword,
                                onPasswordChange = { loginPassword = it; loginError = null },
                                loginError = loginError,
                                loginLoading = loginLoading,
                                onLogin = {
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
                                }
                            )
                        } else {
                            // ===== GŁÓWNY ROUTER NAWIGACJI =====
                            when(val screen = currentScreen) {
                                is Screen.Home -> {
                                    HomeScreen(
                                        showFavoritesOnly = showFavoritesOnly,
                                        onShowAll = {
                                            showFavoritesOnly = false
                                            if (selectedPlayer != null && !players.any { it.id == selectedPlayer?.id }) {
                                                selectedPlayer = null
                                            }
                                        },
                                        onShowFavorites = {
                                            showFavoritesOnly = true
                                            if (selectedPlayer != null && !favoritePlayers.any { it.id == selectedPlayer?.id }) {
                                                selectedPlayer = null
                                            }
                                        },
                                        teams = teams,
                                        selectedTeam = selectedTeam,
                                        onTeamSelected = { selectedTeam = it },
                                        displayedPlayers = if (showFavoritesOnly) favoritePlayers else players,
                                        selectedPlayer = selectedPlayer,
                                        onPlayerSelected = { name, displayedPlayers ->
                                            selectedPlayer = displayedPlayers.firstOrNull { it.name == name }
                                        },
                                        favoritePlayers = favoritePlayers,
                                        onToggleFavorite = { player ->
                                            favoritesRepository.toggleFavorite(player)
                                            favoritePlayers = favoritesRepository.getFavoritePlayers()
                                        },
                                        matchLabels = matchLabels,
                                        selectedMatchId = selectedMatchId,
                                        onMatchSelected = { label ->
                                            selectedMatchId = matchLabels.firstOrNull { it.first == label }?.second
                                        },
                                        isLoading = isLoading,
                                        errorMessage = errorMessage,
                                        statusMessage = statusMessage,
                                        onShowMatchReport = {
                                            selectedPlayer?.let { player ->
                                                selectedMatchId?.let { match ->
                                                    navigateTo(Screen.MatchReport(player.id, match))
                                                }
                                            }
                                        },
                                        onShowPlayerSeason = {
                                            selectedPlayer?.let {
                                                navigateTo(Screen.PlayerSeason(it.id))
                                            }
                                        }
                                    )
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
                                is Screen.ComparePlayers -> {
                                    ComparePlayersScreen(
                                        initialPlayerId = screen.initialPlayerId,
                                        repository = repository,
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

// ===== EXTRACTED COMPOSABLE: LOGIN SCREEN =====
@Composable
private fun LoginScreen(
    loginPassword: String,
    onPasswordChange: (String) -> Unit,
    loginError: String?,
    loginLoading: Boolean,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDesign.ScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Branded header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = AppDesign.CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDesign.CardElevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "SofaMobile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Zaloguj się, aby kontynuować",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Login form card
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
                    .fillMaxWidth()
                    .padding(AppDesign.CardInnerPadding + 8.dp)
            ) {
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("Hasło") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    isError = loginError != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = AppDesign.InputShape,
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginError != null) {
                    Text(
                        loginError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onLogin,
                    enabled = loginPassword.isNotBlank() && !loginLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = AppDesign.ButtonShape,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (loginLoading) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Zaloguj",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ===== EXTRACTED COMPOSABLE: HOME SCREEN =====
@Composable
private fun HomeScreen(
    showFavoritesOnly: Boolean,
    onShowAll: () -> Unit,
    onShowFavorites: () -> Unit,
    teams: List<String>,
    selectedTeam: String?,
    onTeamSelected: (String) -> Unit,
    displayedPlayers: List<Player>,
    selectedPlayer: Player?,
    onPlayerSelected: (String, List<Player>) -> Unit,
    favoritePlayers: List<Player>,
    onToggleFavorite: (Player) -> Unit,
    matchLabels: List<Pair<String, String>>,
    selectedMatchId: String?,
    onMatchSelected: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    statusMessage: String?,
    onShowMatchReport: () -> Unit,
    onShowPlayerSeason: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDesign.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
    ) {
        // --- Segmented toggle: Wszyscy / Ulubieni ---
        Card(
            shape = AppDesign.CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDesign.TinySpacing),
                horizontalArrangement = Arrangement.spacedBy(AppDesign.TinySpacing)
            ) {
                SegmentButton(
                    text = "Wszyscy",
                    isSelected = !showFavoritesOnly,
                    onClick = onShowAll,
                    modifier = Modifier.weight(1f)
                )
                SegmentButton(
                    text = "Ulubieni",
                    isSelected = showFavoritesOnly,
                    onClick = onShowFavorites,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Selection Section ---
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
                    .fillMaxWidth()
                    .padding(AppDesign.CardInnerPadding),
                verticalArrangement = Arrangement.spacedBy(AppDesign.SmallSpacing)
            ) {
                Text(
                    "Wybór zawodnika",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = AppDesign.TinySpacing)
                )

                // Dropdown – Team
                if (!showFavoritesOnly) {
                    AppDropdownSelect(
                        label = "Drużyna",
                        options = teams,
                        selectedOption = selectedTeam ?: "",
                        onOptionSelected = onTeamSelected
                    )
                }

                // Dropdown – Player + Favorite toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppDropdownSelect(
                        modifier = Modifier.weight(1f),
                        label = "Zawodnik",
                        options = displayedPlayers.map { it.name },
                        selectedOption = selectedPlayer?.name ?: "Wybierz zawodnika",
                        onOptionSelected = { name ->
                            onPlayerSelected(name, displayedPlayers)
                        }
                    )

                    if (selectedPlayer != null) {
                        val player = selectedPlayer
                        val isFav = favoritePlayers.any { it.id == player.id }
                        IconButton(
                            onClick = { onToggleFavorite(player) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = if (isFav) "Usuń z ulubionych" else "Dodaj do ulubionych",
                                tint = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(56.dp))
                    }
                }

                // Dropdown – Match
                AppDropdownSelect(
                    label = "Mecz",
                    options = matchLabels.map { it.first },
                    selectedOption = matchLabels.firstOrNull { it.second == selectedMatchId }?.first ?: "",
                    onOptionSelected = onMatchSelected
                )
            }
        }

        // --- Loading indicator ---
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // --- Error message ---
        errorMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = AppDesign.CardShapeSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(AppDesign.ContentPadding),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // --- Action buttons ---
        val canFetch = selectedPlayer != null && selectedMatchId != null && !isLoading
        Button(
            onClick = onShowMatchReport,
            enabled = canFetch,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = AppDesign.ButtonShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Pokaż raport meczowy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // NOWY PRZYCISK - PODSUMOWANIE SEZONU
        OutlinedButton(
            onClick = onShowPlayerSeason,
            enabled = selectedPlayer != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = AppDesign.ButtonShape,
            border = BorderStroke(
                width = 1.5.dp,
                color = if (selectedPlayer != null && !isLoading)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                "Pokaż wykresy powiązane z pozycją gracza",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // --- Status message ---
        statusMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppDesign.CardShapeSmall,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    it,
                    modifier = Modifier.padding(AppDesign.ContentPadding),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===== REUSABLE: Segmented button =====
@Composable
private fun SegmentButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = AppDesign.CardShapeSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.Transparent,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
