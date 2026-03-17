package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.navigation.Screen
import org.example.project.ui.components.AppDropdownSelect
import org.example.project.ui.components.Tile
import org.example.project.ui.components.TileCard
import org.example.project.ui.screens.JsonViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val teamPlayers = remember {
        mapOf(
            "Drużyna A" to listOf("Robert Lewandowski", "Wojciech Szczęsny", "Piotr Zieliński"),
            "Drużyna B" to listOf("Cristiano Ronaldo", "Bernardo Silva", "Bruno Fernandes"),
            "Drużyna C" to listOf("Lionel Messi", "Angel Di Maria", "Julian Alvarez")
        )
    }

    val teams = remember { teamPlayers.keys.toList() }
    var selectedTeam by remember { mutableStateOf(teams[0]) }

    val players = teamPlayers[selectedTeam] ?: emptyList()
    var selectedPlayer by remember { mutableStateOf(players[0]) }
    
    var showJson by remember { mutableStateOf(false) }

    val tiles = remember {
        List(4) { Tile("Zależności $it") }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (val screen = currentScreen) {
                                is Screen.Home -> "Staty Apka"
                                is Screen.Details -> screen.title
                                is Screen.JsonViewer -> screen.moduleName
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    navigationIcon = {
                        if (currentScreen !is Screen.Home) {
                            IconButton(
                                onClick = { currentScreen = Screen.Home },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(
                                    text = "←",
                                    fontSize = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AppDropdownSelect(
                                label = "Wybierz drużynę",
                                options = teams,
                                selectedOption = selectedTeam,
                                onOptionSelected = { 
                                    selectedTeam = it
                                    selectedPlayer = teamPlayers[it]?.firstOrNull() ?: ""
                                }
                            )

                            AppDropdownSelect(
                                label = "Wybierz zawodnika",
                                options = players,
                                selectedOption = selectedPlayer,
                                onOptionSelected = { selectedPlayer = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showJson = !showJson },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Wyświetl JSON")
                                }
                                Button(
                                    onClick = { /* No action */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Druga akcja")
                                }
                            }

                            if (showJson) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = """{
  "team": "$selectedTeam",
  "player": "$selectedPlayer"
}""",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    is Screen.Details -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Standardowa strona dla: ${screen.title}")
                        }
                    }
                    is Screen.JsonViewer -> {
                        JsonViewerScreen(screen.path)
                    }
                }
            }
        }
    }
}
