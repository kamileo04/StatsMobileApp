package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.navigation.Screen
import org.example.project.ui.components.Tile
import org.example.project.ui.components.TileCard
import org.example.project.ui.screens.JsonViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

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
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(140.dp),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(tiles) { tile ->
                                TileCard(
                                    tile = tile,
                                    onClick = { 
                                        if (tile.title.contains("0")) {
                                            currentScreen = Screen.JsonViewer(
                                                moduleName = "Analiza JSON",
                                                path = "C:/Projekty/statyApka/data.json"
                                            )
                                        } else {
                                            currentScreen = Screen.Details(tile.title) 
                                        }
                                    }
                                )
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
